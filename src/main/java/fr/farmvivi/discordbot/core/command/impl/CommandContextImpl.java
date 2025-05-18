package fr.farmvivi.discordbot.core.command.impl;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandContext;
import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.command.i18n.CommandI18n;
import fr.farmvivi.discordbot.core.command.i18n.CommandTranslationKeys;
import fr.farmvivi.discordbot.core.util.DiscordColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of CommandContext that provides the context for command execution.
 */
public class CommandContextImpl implements CommandContext {
    
    /**
     * The original event that triggered this command.
     */
    private final Event originalEvent;
    
    /**
     * The command being executed.
     */
    private final Command command;
    
    /**
     * The command internationalization utility.
     */
    private final CommandI18n commandI18n;
    
    /**
     * The map of option values.
     */
    private final Map<String, Object> optionValues = new HashMap<>();
    
    /**
     * Whether the reply has been deferred.
     */
    private boolean deferred = false;
    
    /**
     * Whether the reply should be ephemeral.
     */
    private boolean ephemeral = false;
    
    /**
     * Creates a new command context for a slash command.
     *
     * @param event       the slash command event
     * @param command     the command being executed
     * @param commandI18n the command internationalization utility
     */
    public CommandContextImpl(SlashCommandInteractionEvent event, Command command, CommandI18n commandI18n) {
        this.originalEvent = event;
        this.command = command;
        this.commandI18n = commandI18n;
        
        // Extract option values
        for (OptionMapping optionMapping : event.getOptions()) {
            String name = optionMapping.getName();
            Object value = null;
            
            switch (optionMapping.getType()) {
                case STRING -> value = optionMapping.getAsString();
                case INTEGER -> value = optionMapping.getAsInt();
                case BOOLEAN -> value = optionMapping.getAsBoolean();
                case USER -> value = optionMapping.getAsUser();
                case CHANNEL -> value = optionMapping.getAsChannel();
                case ROLE -> value = optionMapping.getAsRole();
                case MENTIONABLE -> value = optionMapping.getAsMentionable();
                case NUMBER -> value = optionMapping.getAsDouble();
                case ATTACHMENT -> value = optionMapping.getAsAttachment();
            }
            
            if (value != null) {
                optionValues.put(name, value);
            }
        }
    }
    
    /**
     * Creates a new command context for a text command.
     *
     * @param event       the message event
     * @param command     the command being executed
     * @param commandI18n the command internationalization utility
     * @param args        the parsed command arguments
     */
    public CommandContextImpl(MessageReceivedEvent event, Command command, CommandI18n commandI18n, Map<String, Object> args) {
        this.originalEvent = event;
        this.command = command;
        this.commandI18n = commandI18n;
        
        // Add the arguments
        optionValues.putAll(args);
    }
    
    @Override
    public Event getOriginalEvent() {
        return originalEvent;
    }
    
    @Override
    public Command getCommand() {
        return command;
    }
    
    @Override
    public User getUser() {
        if (originalEvent instanceof SlashCommandInteractionEvent slashEvent) {
            return slashEvent.getUser();
        } else if (originalEvent instanceof MessageReceivedEvent messageEvent) {
            return messageEvent.getAuthor();
        }
        
        throw new IllegalStateException("Unknown event type: " + originalEvent.getClass().getName());
    }
    
    @Override
    public Optional<Guild> getGuild() {
        if (originalEvent instanceof SlashCommandInteractionEvent slashEvent) {
            return Optional.ofNullable(slashEvent.getGuild());
        } else if (originalEvent instanceof MessageReceivedEvent messageEvent) {
            return Optional.ofNullable(messageEvent.getGuild());
        }
        
        return Optional.empty();
    }
    
    @Override
    public MessageChannel getChannel() {
        if (originalEvent instanceof SlashCommandInteractionEvent slashEvent) {
            return slashEvent.getChannel();
        } else if (originalEvent instanceof MessageReceivedEvent messageEvent) {
            return messageEvent.getChannel();
        }
        
        throw new IllegalStateException("Unknown event type: " + originalEvent.getClass().getName());
    }
    
    @Override
    public Locale getLocale() {
        if (originalEvent instanceof SlashCommandInteractionEvent slashEvent) {
            return slashEvent.getUserLocale();
        }
        
        // Default to English if not a slash command
        return Locale.ENGLISH;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getOption(String name) {
        return Optional.ofNullable((T) optionValues.get(name));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(String name, T defaultValue) {
        return (T) optionValues.getOrDefault(name, defaultValue);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getRequiredOption(String name) {
        Object value = optionValues.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Option " + name + " is required but not provided");
        }
        
        return (T) value;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<CommandOption<T>> getOptionDefinition(String name) {
        for (CommandOption<?> option : command.getOptions()) {
            if (option.getName().equals(name)) {
                return Optional.of((CommandOption<T>) option);
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public boolean hasOption(String name) {
        return optionValues.containsKey(name);
    }
    
    @Override
    public void reply(String message) {
        reply(message, Collections.emptyList());
    }
    
    @Override
    public void reply(String message, Collection<LayoutComponent> components) {
        MessageCreateData messageData = new MessageCreateBuilder()
                .setContent(message)
                .setComponents(components)
                .build();
        
        if (originalEvent instanceof SlashCommandInteractionEvent slashEvent) {
            if (deferred) {
                slashEvent.getHook().editOriginal(messageData).queue();
            } else {
                slashEvent.reply(messageData).setEphemeral(ephemeral).queue();
                deferred = true;
            }
        } else if (originalEvent instanceof MessageReceivedEvent messageEvent) {
            MessageCreateAction action = messageEvent.getMessage().reply(messageData);
            
            if (ephemeral) {
                action.delay(1, TimeUnit.MINUTES).flatMap(Message::delete).queue();
            } else {
                action.queue();
            }
        }
    }
    
    @Override
    public void replyEmbed(EmbedBuilder embed) {
        replyEmbed(embed, Collections.emptyList());
    }
    
    @Override
    public void replyEmbed(EmbedBuilder embed, Collection<LayoutComponent> components) {
        MessageCreateData messageData = new MessageCreateBuilder()
                .setEmbeds(embed.build())
                .setComponents(components)
                .build();
        
        if (originalEvent instanceof SlashCommandInteractionEvent slashEvent) {
            if (deferred) {
                slashEvent.getHook().editOriginal(messageData).queue();
            } else {
                slashEvent.reply(messageData).setEphemeral(ephemeral).queue();
                deferred = true;
            }
        } else if (originalEvent instanceof MessageReceivedEvent messageEvent) {
            MessageCreateAction action = messageEvent.getMessage().reply(messageData);
            
            if (ephemeral) {
                action.delay(1, TimeUnit.MINUTES).flatMap(Message::delete).queue();
            } else {
                action.queue();
            }
        }
    }
    
    @Override
    public void replySuccess(String message) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(DiscordColor.GREEN.getColor())
                .setTitle(commandI18n.getMessage(CommandTranslationKeys.Success.EXECUTED, command.getName()))
                .setDescription(message);
        
        replyEmbed(embed);
    }
    
    @Override
    public void replyInfo(String message) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(DiscordColor.BLURPLE.getColor())
                .setTitle("Information")
                .setDescription(message);
        
        replyEmbed(embed);
    }
    
    @Override
    public void replyWarning(String message) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(DiscordColor.ORANGE.getColor())
                .setTitle("Warning")
                .setDescription(message);
        
        replyEmbed(embed);
    }
    
    @Override
    public void replyError(String message) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(DiscordColor.DARK_RED.getColor())
                .setTitle("Error")
                .setDescription(message);
        
        replyEmbed(embed);
    }
    
    @Override
    public void deferReply() {
        deferReply(false);
    }
    
    @Override
    public void deferReply(boolean ephemeral) {
        if (deferred) {
            return;
        }
        
        this.ephemeral = ephemeral;
        
        if (originalEvent instanceof SlashCommandInteractionEvent slashEvent) {
            slashEvent.deferReply(ephemeral).queue();
            deferred = true;
        }
        // No defer for MessageReceivedEvent
    }
    
    @Override
    public boolean isDeferred() {
        return deferred;
    }
    
    @Override
    public boolean isEphemeral() {
        return ephemeral;
    }
    
    @Override
    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }
    
    /**
     * Gets the interaction hook for a slash command.
     *
     * @return the interaction hook, or null if not a slash command
     */
    public InteractionHook getHook() {
        if (originalEvent instanceof SlashCommandInteractionEvent slashEvent) {
            return slashEvent.getHook();
        }
        
        return null;
    }
    
    /**
     * Checks if this context is for a slash command.
     *
     * @return true if this context is for a slash command
     */
    public boolean isSlashCommand() {
        return originalEvent instanceof SlashCommandInteractionEvent;
    }
    
    /**
     * Checks if this context is for a text command.
     *
     * @return true if this context is for a text command
     */
    public boolean isTextCommand() {
        return originalEvent instanceof MessageReceivedEvent;
    }
    
    /**
     * Gets the message for a text command.
     *
     * @return the message, or null if not a text command
     */
    public Message getMessage() {
        if (originalEvent instanceof MessageReceivedEvent messageEvent) {
            return messageEvent.getMessage();
        }
        
        return null;
    }
}

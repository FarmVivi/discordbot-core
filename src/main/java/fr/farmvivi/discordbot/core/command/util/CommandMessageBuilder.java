package fr.farmvivi.discordbot.core.command.util;

import fr.farmvivi.discordbot.core.util.DiscordColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Utility for building and sending command responses.
 * Handles different types of events and provides methods for common response patterns.
 */
public class CommandMessageBuilder {
    
    private final Event event;
    private final MessageCreateBuilder messageBuilder;
    
    private boolean deferred = false;
    private boolean ephemeral = false;
    private boolean autoDeleteAfter1Minute = false;
    
    /**
     * Creates a new command message builder.
     *
     * @param event the event that triggered the command
     */
    public CommandMessageBuilder(Event event) {
        this.event = event;
        this.messageBuilder = new MessageCreateBuilder();
    }
    
    /**
     * Gets whether the response has been deferred.
     *
     * @return true if the response has been deferred
     */
    public boolean isDeferred() {
        return deferred;
    }
    
    /**
     * Sets whether the response has been deferred.
     *
     * @param deferred true to mark the response as deferred
     */
    public void setDeferred(boolean deferred) {
        this.deferred = deferred;
    }
    
    /**
     * Gets whether the response should be ephemeral.
     *
     * @return true if the response should be ephemeral
     */
    public boolean isEphemeral() {
        return ephemeral;
    }
    
    /**
     * Sets whether the response should be ephemeral.
     *
     * @param ephemeral true to make the response ephemeral
     */
    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }
    
    /**
     * Sets whether to auto-delete the response after 1 minute.
     * This is useful for text commands where ephemeral responses are not available.
     *
     * @param autoDelete true to auto-delete the response
     */
    public void setAutoDeleteAfter1Minute(boolean autoDelete) {
        this.autoDeleteAfter1Minute = autoDelete;
    }
    
    /**
     * Gets whether the response should be auto-deleted after 1 minute.
     *
     * @return true if the response should be auto-deleted
     */
    public boolean isAutoDeleteAfter1Minute() {
        return autoDeleteAfter1Minute;
    }
    
    /**
     * Checks if the message builder is empty.
     *
     * @return true if the message builder is empty
     */
    public boolean isEmpty() {
        return messageBuilder.isEmpty();
    }
    
    /**
     * Sets the content of the message.
     *
     * @param content the content
     * @return this builder
     */
    public CommandMessageBuilder setContent(String content) {
        // Limit message length
        if (content.length() > Message.MAX_CONTENT_LENGTH) {
            content = content.substring(0, Message.MAX_CONTENT_LENGTH - 3) + "...";
        }
        messageBuilder.setContent(content);
        return this;
    }
    
    /**
     * Adds content to the message.
     *
     * @param content the content to add
     * @return this builder
     */
    @NotNull
    public CommandMessageBuilder addContent(String content) {
        // Limit message length
        int availableSpace = Message.MAX_CONTENT_LENGTH - (messageBuilder.getContent() != null ? messageBuilder.getContent().length() : 0);
        if (content.length() > availableSpace) {
            content = content.substring(0, availableSpace - 3) + "...";
        }
        if (availableSpace > 0) {
            messageBuilder.addContent(content);
        }
        return this;
    }
    
    /**
     * Sets the embeds of the message.
     *
     * @param embeds the embeds
     * @return this builder
     */
    public CommandMessageBuilder setEmbeds(Collection<? extends MessageEmbed> embeds) {
        List<MessageEmbed> limitedEmbeds = limitEmbeds(embeds);
        messageBuilder.setEmbeds(limitedEmbeds);
        return this;
    }
    
    /**
     * Sets the embeds of the message.
     *
     * @param embeds the embeds
     * @return this builder
     */
    public CommandMessageBuilder setEmbeds(MessageEmbed... embeds) {
        List<MessageEmbed> limitedEmbeds = limitEmbeds(List.of(embeds));
        messageBuilder.setEmbeds(limitedEmbeds);
        return this;
    }
    
    /**
     * Adds embeds to the message.
     *
     * @param embeds the embeds to add
     * @return this builder
     */
    @NotNull
    public CommandMessageBuilder addEmbeds(Collection<? extends MessageEmbed> embeds) {
        // Limit embeds count
        int currentEmbedCount = messageBuilder.getEmbeds().size();
        int availableSpace = Message.MAX_EMBED_COUNT - currentEmbedCount;
        
        if (availableSpace > 0) {
            List<MessageEmbed> limitedEmbeds = new ArrayList<>(embeds);
            if (limitedEmbeds.size() > availableSpace) {
                limitedEmbeds = limitedEmbeds.subList(0, availableSpace);
            }
            messageBuilder.addEmbeds(limitedEmbeds);
        }
        return this;
    }
    
    /**
     * Adds embeds to the message.
     *
     * @param embeds the embeds to add
     * @return this builder
     */
    public CommandMessageBuilder addEmbeds(MessageEmbed... embeds) {
        return addEmbeds(List.of(embeds));
    }
    
    /**
     * Sets the components of the message.
     *
     * @param components the components
     * @return this builder
     */
    public CommandMessageBuilder setComponents(Collection<? extends LayoutComponent> components) {
        List<LayoutComponent> limitedComponents = limitComponents(components);
        messageBuilder.setComponents(limitedComponents);
        return this;
    }
    
    /**
     * Sets the components of the message.
     *
     * @param components the components
     * @return this builder
     */
    public CommandMessageBuilder setComponents(LayoutComponent... components) {
        List<LayoutComponent> limitedComponents = limitComponents(List.of(components));
        messageBuilder.setComponents(limitedComponents);
        return this;
    }
    
    /**
     * Adds components to the message.
     *
     * @param components the components to add
     * @return this builder
     */
    @NotNull
    public CommandMessageBuilder addComponents(@NotNull Collection<? extends LayoutComponent> components) {
        // Limit components count
        int currentComponentCount = messageBuilder.getComponents().size();
        int availableSpace = Message.MAX_COMPONENT_COUNT - currentComponentCount;
        
        if (availableSpace > 0) {
            List<LayoutComponent> limitedComponents = new ArrayList<>(components);
            if (limitedComponents.size() > availableSpace) {
                limitedComponents = limitedComponents.subList(0, availableSpace);
            }
            messageBuilder.addComponents(limitedComponents);
        }
        return this;
    }
    
    /**
     * Adds components to the message.
     *
     * @param components the components to add
     * @return this builder
     */
    public CommandMessageBuilder addComponents(LayoutComponent... components) {
        return addComponents(List.of(components));
    }
    
    /**
     * Creates an info embed with default color.
     *
     * @return the embed builder
     */
    public EmbedBuilder createInfoEmbed() {
        return new EmbedBuilder().setColor(DiscordColor.BLURPLE.getColor());
    }
    
    /**
     * Creates a success embed with default color.
     *
     * @return the embed builder
     */
    public EmbedBuilder createSuccessEmbed() {
        return new EmbedBuilder().setColor(DiscordColor.GREEN.getColor());
    }
    
    /**
     * Creates a warning embed with default color.
     *
     * @return the embed builder
     */
    public EmbedBuilder createWarningEmbed() {
        return new EmbedBuilder().setColor(DiscordColor.ORANGE.getColor());
    }
    
    /**
     * Creates an error embed with default color.
     *
     * @return the embed builder
     */
    public EmbedBuilder createErrorEmbed() {
        return new EmbedBuilder().setColor(DiscordColor.DARK_RED.getColor());
    }
    
    /**
     * Adds an info embed with the given description.
     *
     * @param description the description
     * @return this builder
     */
    public CommandMessageBuilder info(String description) {
        return info(null, description);
    }
    
    /**
     * Adds an info embed with the given title and description.
     *
     * @param title the title
     * @param description the description
     * @return this builder
     */
    public CommandMessageBuilder info(String title, String description) {
        EmbedBuilder embed = createInfoEmbed();
        
        if (title != null && !title.isEmpty()) {
            embed.setTitle(title);
        } else {
            embed.setTitle("Information");
        }
        
        if (description != null && !description.isEmpty()) {
            embed.setDescription(description);
        }
        
        addEmbeds(embed.build());
        return this;
    }
    
    /**
     * Adds a success embed with the given description.
     *
     * @param description the description
     * @return this builder
     */
    public CommandMessageBuilder success(String description) {
        return success(null, description);
    }
    
    /**
     * Adds a success embed with the given title and description.
     *
     * @param title the title
     * @param description the description
     * @return this builder
     */
    public CommandMessageBuilder success(String title, String description) {
        EmbedBuilder embed = createSuccessEmbed();
        
        if (title != null && !title.isEmpty()) {
            embed.setTitle(title);
        } else {
            embed.setTitle("Success");
        }
        
        if (description != null && !description.isEmpty()) {
            embed.setDescription(description);
        }
        
        addEmbeds(embed.build());
        return this;
    }
    
    /**
     * Adds a warning embed with the given description.
     *
     * @param description the description
     * @return this builder
     */
    public CommandMessageBuilder warning(String description) {
        return warning(null, description);
    }
    
    /**
     * Adds a warning embed with the given title and description.
     *
     * @param title the title
     * @param description the description
     * @return this builder
     */
    public CommandMessageBuilder warning(String title, String description) {
        EmbedBuilder embed = createWarningEmbed();
        
        if (title != null && !title.isEmpty()) {
            embed.setTitle(title);
        } else {
            embed.setTitle("Warning");
        }
        
        if (description != null && !description.isEmpty()) {
            embed.setDescription(description);
        }
        
        addEmbeds(embed.build());
        return this;
    }
    
    /**
     * Adds an error embed with the given description.
     *
     * @param description the description
     * @return this builder
     */
    public CommandMessageBuilder error(String description) {
        return error(null, description);
    }
    
    /**
     * Adds an error embed with the given title and description.
     *
     * @param title the title
     * @param description the description
     * @return this builder
     */
    public CommandMessageBuilder error(String title, String description) {
        EmbedBuilder embed = createErrorEmbed();
        
        if (title != null && !title.isEmpty()) {
            embed.setTitle(title);
        } else {
            embed.setTitle("Error");
        }
        
        if (description != null && !description.isEmpty()) {
            embed.setDescription(description);
        }
        
        addEmbeds(embed.build());
        return this;
    }
    
    /**
     * Builds the message.
     *
     * @return the message data
     */
    public MessageCreateData build() {
        return messageBuilder.build();
    }
    
    /**
     * Sends the message.
     * This method handles different event types and respects deferred status.
     */
    public void sendNow() {
        if (isEmpty()) {
            return;
        }
        
        if (event instanceof SlashCommandInteractionEvent slashEvent) {
            if (deferred) {
                // Edit the original response
                slashEvent.getHook().editOriginal(build()).queue();
            } else {
                // Send a new response
                slashEvent.reply(build()).setEphemeral(ephemeral).queue();
                deferred = true;
            }
        } else if (event instanceof MessageReceivedEvent messageEvent) {
            // Send a new message
            MessageCreateAction action = messageEvent.getMessage().reply(build());
            
            if (ephemeral || autoDeleteAfter1Minute) {
                action.delay(1, TimeUnit.MINUTES).flatMap(Message::delete).queue();
                if (!messageEvent.isFromPrivate() && messageEvent.getGuild() != null &&
                    messageEvent.getGuild().getSelfMember().hasPermission(messageEvent.getGuildChannel(), 
                                                         net.dv8tion.jda.api.Permission.MESSAGE_MANAGE)) {
                    messageEvent.getMessage().delete().queueAfter(1, TimeUnit.MINUTES);
                }
            } else {
                action.queue();
            }
        } else if (event instanceof IReplyCallback callback) {
            if (deferred) {
                // Edit the original response
                callback.getHook().editOriginal(build()).queue();
            } else {
                // Send a new response
                callback.reply(build()).setEphemeral(ephemeral).queue();
                deferred = true;
            }
        }
    }
    
    /**
     * Defers the response.
     * This is useful for commands that take a long time to execute.
     */
    public void deferReply() {
        if (deferred) {
            return;
        }
        
        if (event instanceof IReplyCallback callback) {
            callback.deferReply(ephemeral).queue();
            deferred = true;
        }
        // No defer for MessageReceivedEvent
    }
    
    /**
     * Gets the webhook hook for a slash command.
     *
     * @return the webhook hook, or null if not a slash command
     */
    public InteractionHook getHook() {
        if (event instanceof IReplyCallback callback) {
            return callback.getHook();
        }
        return null;
    }
    
    /**
     * Limits the number of embeds to the maximum allowed by Discord.
     *
     * @param embeds the embeds to limit
     * @return the limited embeds
     */
    private List<MessageEmbed> limitEmbeds(Collection<? extends MessageEmbed> embeds) {
        if (embeds.size() > Message.MAX_EMBED_COUNT) {
            List<MessageEmbed> limited = new ArrayList<>(embeds);
            return limited.subList(0, Message.MAX_EMBED_COUNT);
        }
        return new ArrayList<>(embeds);
    }
    
    /**
     * Limits the number of components to the maximum allowed by Discord.
     *
     * @param components the components to limit
     * @return the limited components
     */
    private List<LayoutComponent> limitComponents(Collection<? extends LayoutComponent> components) {
        if (components.size() > Message.MAX_COMPONENT_COUNT) {
            List<LayoutComponent> limited = new ArrayList<>(components);
            return limited.subList(0, Message.MAX_COMPONENT_COUNT);
        }
        return new ArrayList<>(components);
    }
}

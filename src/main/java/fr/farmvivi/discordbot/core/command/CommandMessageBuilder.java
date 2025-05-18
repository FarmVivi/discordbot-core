package fr.farmvivi.discordbot.core.command;

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

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Builder for creating command response messages.
 * This builder extends MessageCreateBuilder and adds utility methods for
 * easily creating information, success, warning, and error messages.
 */
public class CommandMessageBuilder extends MessageCreateBuilder {
    
    // Standard Discord colors
    private static final Color COLOR_INFO = new Color(88, 101, 242); // Discord blurple
    private static final Color COLOR_SUCCESS = new Color(87, 242, 135); // Discord green
    private static final Color COLOR_WARNING = new Color(254, 231, 92); // Discord yellow
    private static final Color COLOR_ERROR = new Color(237, 66, 69); // Discord red
    
    private final Event event;
    private boolean differ = false;
    private boolean ephemeral = false;

    /**
     * Creates a new command message builder.
     *
     * @param event the JDA event that triggered the command
     */
    public CommandMessageBuilder(Event event) {
        super();
        this.event = event;
    }
    
    /**
     * Sets the content of the message.
     * This overrides any existing content.
     *
     * @param content the content to set
     * @return this builder
     */
    public CommandMessageBuilder setContent(String content) {
        clear();
        if (content != null && !content.isEmpty()) {
            setContent(limitStringLength(content, Message.MAX_CONTENT_LENGTH));
        }
        return this;
    }
    
    /**
     * Sets the components of the message.
     * This overrides any existing components.
     *
     * @param components the components to set
     * @return this builder
     */
    public CommandMessageBuilder setComponents(Collection<? extends LayoutComponent> components) {
        this.components.clear();
        if (components != null && !components.isEmpty()) {
            int count = Math.min(components.size(), Message.MAX_COMPONENT_COUNT);
            addComponents(components.stream().limit(count).toList());
        }
        return this;
    }
    
    /**
     * Sets the embeds of the message.
     * This overrides any existing embeds.
     *
     * @param embeds the embeds to set
     * @return this builder
     */
    public CommandMessageBuilder setEmbeds(Collection<? extends MessageEmbed> embeds) {
        this.embeds.clear();
        if (embeds != null && !embeds.isEmpty()) {
            int count = Math.min(embeds.size(), Message.MAX_EMBED_COUNT);
            addEmbeds(embeds.stream().limit(count).toList());
        }
        return this;
    }

    /**
     * Gets whether the reply is deferred.
     *
     * @return true if the reply is deferred
     */
    public boolean isDiffer() {
        return differ;
    }

    /**
     * Sets whether the reply is deferred.
     *
     * @param differ true to defer the reply
     */
    public void setDiffer(boolean differ) {
        this.differ = differ;
    }

    /**
     * Gets whether the reply is ephemeral.
     *
     * @return true if the reply is ephemeral
     */
    public boolean isEphemeral() {
        return ephemeral;
    }

    /**
     * Sets whether the reply is ephemeral.
     *
     * @param ephemeral true to make the reply ephemeral
     */
    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    /**
     * Sends the reply immediately.
     * This method is called automatically when a command is executed.
     */
    public void replyNow() {
        if (differ) {
            differ = false;
            
            // Handle deferred slash commands
            if (event instanceof IReplyCallback callback && callback.isAcknowledged()) {
                InteractionHook hook = callback.getHook();
                
                if (isEmpty()) {
                    hook.deleteOriginal().queue();
                } else {
                    // Edit content
                    WebhookMessageEditAction<Message> messageWebhookMessageEditAction = hook.editOriginal(getContent());
                    
                    // Edit embeds
                    if (!getEmbeds().isEmpty()) {
                        messageWebhookMessageEditAction.setEmbeds(getEmbeds());
                    }
                    
                    // Edit components
                    if (!getComponents().isEmpty()) {
                        messageWebhookMessageEditAction.setComponents(getComponents());
                    }

                    // Commit edit
                    if (isEphemeral()) {
                        messageWebhookMessageEditAction
                                .delay(1, TimeUnit.MINUTES)
                                .flatMap(Predicate.not(Message::isEphemeral), Message::delete)
                                .queue();
                    } else {
                        messageWebhookMessageEditAction.queue();
                    }
                }
            } 
            // Handle deferred text commands
            else if (event instanceof MessageReceivedEvent messageReceivedEvent) {
                if (!isEmpty()) {
                    Message originalMessage = messageReceivedEvent.getMessage();
                    MessageCreateAction messageCreateAction = originalMessage.reply(build());
                    
                    if (isEphemeral()) {
                        messageCreateAction
                                .delay(1, TimeUnit.MINUTES)
                                .flatMap(Message::delete)
                                .queue();
                        
                        // Delete original message after delay if we have permission
                        if (messageReceivedEvent.isFromGuild() && 
                                messageReceivedEvent.getGuild().getSelfMember()
                                        .hasPermission(messageReceivedEvent.getGuildChannel(), 
                                                net.dv8tion.jda.api.Permission.MESSAGE_MANAGE)) {
                            originalMessage.delete().queueAfter(1, TimeUnit.MINUTES);
                        }
                    } else {
                        messageCreateAction.queue();
                    }
                } else if (isEphemeral()) {
                    Message originalMessage = messageReceivedEvent.getMessage();
                    
                    // Delete original message after delay if we have permission
                    if (messageReceivedEvent.isFromGuild() && 
                            messageReceivedEvent.getGuild().getSelfMember()
                                    .hasPermission(messageReceivedEvent.getGuildChannel(), 
                                            net.dv8tion.jda.api.Permission.MESSAGE_MANAGE)) {
                        originalMessage.delete().queueAfter(1, TimeUnit.MINUTES);
                    }
                }
            }
        }
        // Handle direct replies for slash commands
        else if (event instanceof SlashCommandInteractionEvent slashCommand && !slashCommand.isAcknowledged()) {
            if (isEmpty()) {
                slashCommand.reply("OK")
                        .flatMap(InteractionHook::deleteOriginal)
                        .queue();
            } else {
                slashCommand.reply(build())
                        .setEphemeral(isEphemeral())
                        .queue();
            }
        }
    }

    /**
     * Creates a basic embed builder.
     *
     * @return a new embed builder
     */
    private EmbedBuilder createEmbed() {
        return new EmbedBuilder();
    }

    /**
     * Creates an info embed.
     *
     * @return the embed builder
     */
    public EmbedBuilder createInfoEmbed() {
        return createEmbed().setColor(COLOR_INFO);
    }

    /**
     * Creates a success embed.
     *
     * @return the embed builder
     */
    public EmbedBuilder createSuccessEmbed() {
        return createEmbed().setColor(COLOR_SUCCESS);
    }

    /**
     * Creates a warning embed.
     *
     * @return the embed builder
     */
    public EmbedBuilder createWarningEmbed() {
        return createEmbed().setColor(COLOR_WARNING);
    }

    /**
     * Creates an error embed.
     *
     * @return the embed builder
     */
    public EmbedBuilder createErrorEmbed() {
        return createEmbed().setColor(COLOR_ERROR);
    }

    /**
     * Adds an info message to the response.
     *
     * @param description the message description
     */
    public void info(String description) {
        info(null, description);
    }

    /**
     * Adds an info message to the response.
     *
     * @param title the message title
     * @param description the message description
     */
    public void info(String title, String description) {
        // Create embed
        EmbedBuilder embedBuilder = createInfoEmbed();

        // Set title
        if (title == null || title.isEmpty()) {
            embedBuilder.setTitle("Information");
        } else {
            embedBuilder.setTitle(title);
        }

        // Set description
        if (description != null && !description.isEmpty()) {
            embedBuilder.setDescription(description);
        }

        // Add embed
        addEmbeds(embedBuilder.build());
    }

    /**
     * Adds a success message to the response.
     *
     * @param description the message description
     */
    public void success(String description) {
        success(null, description);
    }

    /**
     * Adds a success message to the response.
     *
     * @param title the message title
     * @param description the message description
     */
    public void success(String title, String description) {
        // Create embed
        EmbedBuilder embedBuilder = createSuccessEmbed();

        // Set title
        if (title == null || title.isEmpty()) {
            embedBuilder.setTitle("Command executed successfully");
        } else {
            embedBuilder.setTitle(title);
        }

        // Set description
        if (description != null && !description.isEmpty()) {
            embedBuilder.setDescription(description);
        }

        // Add embed
        addEmbeds(embedBuilder.build());
    }

    /**
     * Adds a warning message to the response.
     *
     * @param description the message description
     */
    public void warning(String description) {
        warning(null, description);
    }

    /**
     * Adds a warning message to the response.
     *
     * @param title the message title
     * @param description the message description
     */
    public void warning(String title, String description) {
        // Create embed
        EmbedBuilder embedBuilder = createWarningEmbed();

        // Set title
        if (title == null || title.isEmpty()) {
            embedBuilder.setTitle("Warning");
        } else {
            embedBuilder.setTitle(title);
        }

        // Set description
        if (description != null && !description.isEmpty()) {
            embedBuilder.setDescription(description);
        }

        // Add embed
        addEmbeds(embedBuilder.build());
    }

    /**
     * Adds an error message to the response.
     *
     * @param description the message description
     */
    public void error(String description) {
        error(null, description);
    }

    /**
     * Adds an error message to the response.
     *
     * @param title the message title
     * @param description the message description
     */
    public void error(String title, String description) {
        // Create embed
        EmbedBuilder embedBuilder = createErrorEmbed();

        // Set title
        if (title == null || title.isEmpty()) {
            embedBuilder.setTitle("An error occurred");
        } else {
            embedBuilder.setTitle(title);
        }

        // Set description
        if (description != null && !description.isEmpty()) {
            embedBuilder.setDescription(description);
        }

        // Add embed
        addEmbeds(embedBuilder.build());
    }

    @NotNull
    @Override
    public MessageCreateBuilder addContent(String content) {
        // Limit message length
        content = limitStringLength(content, Message.MAX_CONTENT_LENGTH);
        return super.addContent(content);
    }

    @NotNull
    @Override
    public MessageCreateBuilder addEmbeds(Collection<? extends MessageEmbed> embeds) {
        // Limit embeds count
        if (embeds.size() > Message.MAX_EMBED_COUNT) {
            embeds = embeds.stream().limit(Message.MAX_EMBED_COUNT).toList();
        }

        return super.addEmbeds(embeds);
    }

    @NotNull
    @Override
    public MessageCreateBuilder addComponents(@NotNull Collection<? extends LayoutComponent> components) {
        // Limit components count
        if (components.size() > Message.MAX_COMPONENT_COUNT) {
            components = components.stream().limit(Message.MAX_COMPONENT_COUNT).toList();
        }

        return super.addComponents(components);
    }
    
    /**
     * Limits a string to a maximum length.
     *
     * @param content the string to limit
     * @param maxLength the maximum length
     * @return the limited string
     */
    private String limitStringLength(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        
        if (content.length() > maxLength) {
            return content.substring(0, maxLength - 3) + "...";
        }
        
        return content;
    }
    
    /**
     * Builds the message and returns the data.
     * This is used for sending the message.
     *
     * @return the message create data
     */
    public MessageCreateData build() {
        return super.build();
    }
}

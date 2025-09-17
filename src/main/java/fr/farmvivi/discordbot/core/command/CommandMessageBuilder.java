package fr.farmvivi.discordbot.core.command;

import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.command.parser.event.ConsoleCommandEvent;
import fr.farmvivi.discordbot.core.util.DiscordColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Builder for creating command response messages.
 * This builder extends MessageCreateBuilder and adds utility methods for
 * easily creating information, success, warning, and error messages.
 */
public class CommandMessageBuilder extends MessageCreateBuilder {
    private static final Logger logger = LoggerFactory.getLogger(CommandMessageBuilder.class);

    private final Event event;
    private final LanguageManager languageManager;
    private final Locale locale;
    private boolean differ = false;
    private boolean ephemeral = false;

    /**
     * Creates a new command message builder.
     *
     * @param event           the JDA event that triggered the command
     * @param languageManager the language manager for translations
     * @param locale          the locale to use for translations
     */
    public CommandMessageBuilder(Event event, LanguageManager languageManager, Locale locale) {
        super();
        this.event = event;
        this.languageManager = languageManager;
        this.locale = locale;
    }

    /**
     * Creates a new command message builder.
     * This constructor is for backward compatibility and does not use translations.
     *
     * @param event the JDA event that triggered the command
     */
    public CommandMessageBuilder(Event event) {
        super();
        this.event = event;
        this.languageManager = null;
        this.locale = Locale.US;
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
            super.setContent(limitStringLength(content, Message.MAX_CONTENT_LENGTH));
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
    public CommandMessageBuilder setComponents(Collection<? extends MessageTopLevelComponent> components) {
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
        logger.debug("replyNow called with differ (deferred flag): {}", differ);

        if (event instanceof IReplyCallback callback) {
            handleInteractionCallback(callback);
            return;
        }

        if (event instanceof MessageReceivedEvent messageReceivedEvent) {
            handleTextMessage(messageReceivedEvent);
            return;
        }

        if (event instanceof ConsoleCommandEvent) {
            handleConsoleOutput();
        }
    }

    /**
     * Handles replies for interaction based events (slash commands, buttons, etc.).
     */
    private void handleInteractionCallback(IReplyCallback callback) {
        boolean acknowledged = callback.isAcknowledged();
        logger.debug("handleInteractionCallback: acknowledged={}, deferredFlag={}, ephemeralRequested={}", acknowledged, differ, ephemeral);

        // If we expect to edit a deferred reply but it's not acknowledged yet, perform deferral now
        if (differ && !acknowledged) {
            logger.debug("Deferred flag set but interaction not yet acknowledged -> deferring now (ephemeral={}).", ephemeral);
            callback.deferReply(ephemeral).queue();
            return; // Actual content will be sent on a subsequent call when ready
        }

        // Editing an already deferred (acknowledged) interaction
        if (acknowledged) {
            InteractionHook hook = callback.getHook();
            if (isEmpty()) {
                logger.debug("No content provided after deferral -> deleting original.");
                hook.deleteOriginal().queue();
            } else {
                logger.debug("Editing original deferred interaction message.");
                String content = getContent();
                if (content == null)
                    content = ""; // JDA requires non-null string for content edit
                WebhookMessageEditAction<Message> editAction = hook.editOriginal(content);
                if (!getEmbeds().isEmpty()) {
                    editAction.setEmbeds(getEmbeds());
                } else {
                    editAction.setEmbeds(); // clear embeds if none now
                }
                if (!getComponents().isEmpty()) {
                    editAction.setComponents(getComponents());
                } else {
                    editAction.setComponents(); // clear components
                }
                editAction.queue();
            }
            differ = false; // Reset internal flag
            return;
        }

        // Direct initial reply (not deferred yet)
        long start = System.currentTimeMillis();
        if (isEmpty()) {
            logger.debug("Empty content for initial interaction reply -> sending placeholder then deleting (ephemeral={}).", ephemeral);
            callback.reply("\u200B") // zero-width to avoid visible 'OK'
                    .setEphemeral(ephemeral)
                    .flatMap(InteractionHook::deleteOriginal)
                    .queue();
            return;
        }

        // Build reply in a granular way to avoid any potential JDA bug with
        // MessageCreateData + ephemeral
        String rawContent = getContent();
        if (rawContent == null)
            rawContent = "";
        final String content = rawContent;

        var initialAction = callback.reply(content).setEphemeral(ephemeral);
        if (!getEmbeds().isEmpty()) {
            initialAction.addEmbeds(getEmbeds());
        }
        if (!getComponents().isEmpty()) {
            initialAction.addComponents(getComponents());
        }

        long elapsed = System.currentTimeMillis() - start; // minimal now
        if (elapsed > 2500) {
            // Safety fallback: if building took unexpectedly long, defer instead (rare)
            logger.warn("Building initial reply took {}ms (>2500). Falling back to defer+edit.", elapsed);
            callback.deferReply(ephemeral).queue(h -> {
                InteractionHook hook = callback.getHook();
                WebhookMessageEditAction<Message> editAction = hook.editOriginal(content);
                if (!getEmbeds().isEmpty())
                    editAction.setEmbeds(getEmbeds());
                else
                    editAction.setEmbeds();
                if (!getComponents().isEmpty())
                    editAction.setComponents(getComponents());
                else
                    editAction.setComponents();
                editAction.queue();
            });
            return;
        }

        logger.debug("Sending initial interaction reply (ephemeral={}) with {} embeds and {} components.", ephemeral, getEmbeds().size(), getComponents().size());
        initialAction.queue();
    }

    /**
     * Handles replies for classic text commands (MessageReceivedEvent).
     */
    private void handleTextMessage(MessageReceivedEvent messageReceivedEvent) {
        boolean willDelete = ephemeral; // emulate ephemeral via deletion
        if (differ) {
            logger.debug("handleTextMessage: treating as deferred edit simulation (no actual edit possible).");
            differ = false; // reset
        }

        if (!isEmpty()) {
            Message originalMessage = messageReceivedEvent.getMessage();
            MessageCreateAction action = originalMessage.reply(build());
            if (willDelete) {
                action.queue(sent -> {
                    sent.delete().queueAfter(1, TimeUnit.MINUTES);
                    deleteOriginalAfterDelay(messageReceivedEvent);
                });
            } else {
                action.queue();
            }
        } else if (willDelete) {
            // No content but we still emulate ephemeral by deleting original
            deleteOriginalAfterDelay(messageReceivedEvent);
        }
    }

    private void deleteOriginalAfterDelay(MessageReceivedEvent event) {
        if (event.isFromGuild() && event.getGuild().getSelfMember().hasPermission(event.getGuildChannel(), net.dv8tion.jda.api.Permission.MESSAGE_MANAGE)) {
            logger.debug("Scheduling deletion of triggering message for ephemeral emulation.");
            event.getMessage().delete().queueAfter(1, TimeUnit.MINUTES);
        }
    }

    /**
     * Outputs message content to console for console command events.
     */
    private void handleConsoleOutput() {
        if (!isEmpty()) {
            String output = formatForConsole();
            System.out.println(output);
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
        return createEmbed().setColor(DiscordColor.DISCORD_BLURPLE.getColor());
    }

    /**
     * Creates a success embed.
     *
     * @return the embed builder
     */
    public EmbedBuilder createSuccessEmbed() {
        return createEmbed().setColor(DiscordColor.DISCORD_GREEN.getColor());
    }

    /**
     * Creates a warning embed.
     *
     * @return the embed builder
     */
    public EmbedBuilder createWarningEmbed() {
        return createEmbed().setColor(DiscordColor.DISCORD_YELLOW.getColor());
    }

    /**
     * Creates an error embed.
     *
     * @return the embed builder
     */
    public EmbedBuilder createErrorEmbed() {
        return createEmbed().setColor(DiscordColor.DISCORD_RED.getColor());
    }

    /**
     * Formats the message for console output.
     * Converts embeds and other Discord-specific elements to plain text.
     *
     * @return formatted console output
     */
    private String formatForConsole() {
        StringBuilder output = new StringBuilder();

        // Add content if present
        String content = getContent();
        if (content != null && !content.trim().isEmpty()) {
            output.append("[CONSOLE] ").append(content);
        }

        // Format embeds for console
        if (!getEmbeds().isEmpty()) {
            for (MessageEmbed embed : getEmbeds()) {
                if (output.length() > 0) {
                    output.append("\n");
                }

                output.append("[CONSOLE] ");

                // Add title
                if (embed.getTitle() != null) {
                    output.append("=== ").append(embed.getTitle()).append(" ===\n[CONSOLE] ");
                }

                // Add description
                if (embed.getDescription() != null) {
                    output.append(embed.getDescription()).append("\n[CONSOLE] ");
                }

                // Add fields
                for (MessageEmbed.Field field : embed.getFields()) {
                    if (field.getName() != null) {
                        output.append(field.getName()).append(": ");
                    }
                    if (field.getValue() != null) {
                        output.append(field.getValue());
                    }
                    output.append("\n[CONSOLE] ");
                }

                // Remove trailing "[CONSOLE] "
                if (output.toString().endsWith("[CONSOLE] ")) {
                    output.setLength(output.length() - 10);
                }
            }
        }

        return output.toString();
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
     * @param title       the message title
     * @param description the message description
     */
    public void info(String title, String description) {
        // Create embed
        EmbedBuilder embedBuilder = createInfoEmbed();

        // Set title
        if (title == null || title.isEmpty()) {
            embedBuilder.setTitle(languageManager.getString(locale, "commands.titles.info"));
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
     * @param title       the message title
     * @param description the message description
     */
    public void success(String title, String description) {
        // Create embed
        EmbedBuilder embedBuilder = createSuccessEmbed();

        // Set title
        if (title == null || title.isEmpty()) {
            embedBuilder.setTitle(languageManager.getString(locale, "commands.titles.success"));
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
     * @param title       the message title
     * @param description the message description
     */
    public void warning(String title, String description) {
        // Create embed
        EmbedBuilder embedBuilder = createWarningEmbed();

        // Set title
        if (title == null || title.isEmpty()) {
            embedBuilder.setTitle(languageManager.getString(locale, "commands.titles.warning"));
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
     * @param title       the message title
     * @param description the message description
     */
    public void error(String title, String description) {
        // Create embed
        EmbedBuilder embedBuilder = createErrorEmbed();

        // Set title
        if (title == null || title.isEmpty()) {
            embedBuilder.setTitle(languageManager.getString(locale, "commands.titles.error"));
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
    public MessageCreateBuilder addComponents(@NotNull Collection<? extends MessageTopLevelComponent> components) {
        // Limit components count
        if (components.size() > Message.MAX_COMPONENT_COUNT) {
            components = components.stream().limit(Message.MAX_COMPONENT_COUNT).toList();
        }

        return super.addComponents(components);
    }

    /**
     * Limits a string to a maximum length.
     *
     * @param content   the string to limit
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

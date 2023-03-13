package fr.farmvivi.discordbot.module.commands;

import fr.farmvivi.discordbot.DiscordColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class CommandMessageBuilder extends MessageCreateBuilder {
    private final Event event;

    private boolean differ = false;
    private boolean ephemeral = false;

    public CommandMessageBuilder(Event event) {
        super();

        this.event = event;
    }

    public boolean isDiffer() {
        return differ;
    }

    public void setDiffer(boolean differ) {
        this.differ = differ;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    public void replyNow() {
        if (differ) {
            differ = false;
            if (event instanceof IDeferrableCallback deferrableCallback) {
                if (isEmpty()) {
                    deferrableCallback.getHook().deleteOriginal().queue();
                } else {
                    // Edit content
                    WebhookMessageEditAction<Message> messageWebhookMessageEditAction = deferrableCallback.getHook().editOriginal(this.getContent());
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
                        messageWebhookMessageEditAction.delay(1, TimeUnit.MINUTES).flatMap(Predicate.not(Message::isEphemeral), Message::delete).queue();
                    } else {
                        messageWebhookMessageEditAction.queue();
                    }
                }
            } else if (event instanceof MessageReceivedEvent messageReceivedEvent) {
                if (!isEmpty()) {
                    Message originalMessage = messageReceivedEvent.getMessage();
                    MessageCreateAction messageCreateAction = originalMessage.reply(this.build());
                    if (isEphemeral()) {
                        messageCreateAction.delay(1, TimeUnit.MINUTES).flatMap(Message::delete).queue();
                        originalMessage.delete().queueAfter(1, TimeUnit.MINUTES);
                    } else {
                        messageCreateAction.queue();
                    }
                } else if (isEphemeral()) {
                    Message originalMessage = messageReceivedEvent.getMessage();
                    originalMessage.delete().queueAfter(1, TimeUnit.MINUTES);
                }
            }
        }
    }

    private EmbedBuilder createEmbed() {
        return new EmbedBuilder();
    }

    public EmbedBuilder createInfoEmbed() {
        // Create embed
        EmbedBuilder embedBuilder = createEmbed();

        // Set blue color
        embedBuilder.setColor(DiscordColor.BLURPLE.getColor());

        return embedBuilder;
    }

    public EmbedBuilder createSuccessEmbed() {
        // Create embed
        EmbedBuilder embedBuilder = createEmbed();

        // Set green color
        embedBuilder.setColor(DiscordColor.GREEN.getColor());

        return embedBuilder;
    }

    public EmbedBuilder createWarningEmbed() {
        // Create embed
        EmbedBuilder embedBuilder = createEmbed();

        // Set yellow color
        embedBuilder.setColor(DiscordColor.ORANGE.getColor());

        return embedBuilder;
    }

    public EmbedBuilder createErrorEmbed() {
        // Create embed
        EmbedBuilder embedBuilder = createEmbed();

        // Set red color
        embedBuilder.setColor(DiscordColor.DARK_RED.getColor());

        return embedBuilder;
    }

    public void info(String description) {
        info(null, description);
    }

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

    public void success(String description) {
        success(null, description);
    }

    public void success(String title, String description) {
        // Create embed
        EmbedBuilder embedBuilder = createSuccessEmbed();

        // Set title
        if (title == null || title.isEmpty()) {
            embedBuilder.setTitle("Commande exécutée avec succès");
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

    public void warning(String description) {
        warning(null, description);
    }

    public void warning(String title, String description) {
        // Create embed
        EmbedBuilder embedBuilder = createWarningEmbed();

        // Set title
        if (title == null || title.isEmpty()) {
            embedBuilder.setTitle("Attention");
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

    public void error(String description) {
        error(null, description);
    }

    public void error(String title, String description) {
        // Create embed
        EmbedBuilder embedBuilder = createErrorEmbed();

        // Set title
        if (title == null || title.isEmpty()) {
            embedBuilder.setTitle("Une erreur est survenue");
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
        if (content.length() > Message.MAX_CONTENT_LENGTH) {
            content = content.substring(0, Message.MAX_CONTENT_LENGTH - 3) + "...";
        }

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
}

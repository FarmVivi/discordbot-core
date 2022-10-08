package fr.farmvivi.discordbot.module.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

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
            if (event instanceof SlashCommandInteractionEvent slashCommandInteractionEvent) {
                if (isEmpty()) {
                    slashCommandInteractionEvent.getHook().deleteOriginal().queue();
                } else {
                    WebhookMessageEditAction<Message> messageWebhookMessageEditAction = slashCommandInteractionEvent.getHook().editOriginal(this.getContent());
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
}

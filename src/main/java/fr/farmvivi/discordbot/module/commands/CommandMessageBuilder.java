package fr.farmvivi.discordbot.module.commands;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class CommandMessageBuilder extends MessageBuilder {
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
                WebhookMessageUpdateAction<Message> messageWebhookMessageUpdateAction = slashCommandInteractionEvent.getHook().editOriginal(this.build());
                if (isEphemeral()) {
                    messageWebhookMessageUpdateAction.delay(1, TimeUnit.MINUTES).flatMap(Predicate.not(Message::isEphemeral), Message::delete).queue();
                } else {
                    messageWebhookMessageUpdateAction.queue();
                }
            } else if (event instanceof MessageReceivedEvent messageReceivedEvent) {
                Message originalMessage = messageReceivedEvent.getMessage();
                MessageAction messageAction = originalMessage.reply(this.build());
                if (isEphemeral()) {
                    messageAction.delay(1, TimeUnit.MINUTES).flatMap(Message::delete).queue();
                    originalMessage.delete().queueAfter(1, TimeUnit.MINUTES);
                } else {
                    messageAction.queue();
                }
            }
        }
    }
}

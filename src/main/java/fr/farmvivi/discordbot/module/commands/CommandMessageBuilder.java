package fr.farmvivi.discordbot.module.commands;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandMessageBuilder extends MessageBuilder {
    private final Event event;
    private boolean differ = false;

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

    public void replyNow() {
        if (differ) {
            differ = false;
            if (event instanceof SlashCommandInteractionEvent slashCommandInteractionEvent) {
                slashCommandInteractionEvent.getHook().editOriginal(this.build()).queue();
            } else if (event instanceof MessageReceivedEvent messageReceivedEvent) {
                messageReceivedEvent.getMessage().reply(this.build()).queue();
            }
        }
    }
}

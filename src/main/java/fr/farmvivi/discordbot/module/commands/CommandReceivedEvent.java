package fr.farmvivi.discordbot.module.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandReceivedEvent {
    private final Event event;
    private final String command;

    public CommandReceivedEvent(Event event, String command) {
        // Check if event is a MessageReceivedEvent or a SlashCommandInteractionEvent
        if (!(event instanceof MessageReceivedEvent) && !(event instanceof SlashCommandInteractionEvent)) {
            throw new IllegalArgumentException("Event must be a MessageReceivedEvent or a SlashCommandInteractionEvent");
        }

        this.event = event;
        this.command = command;
    }

    public Event getOriginalEvent() {
        return event;
    }

    public String getCommand() {
        return command;
    }

    public Guild getGuild() {
        if (event instanceof SlashCommandInteractionEvent slashCommandInteractionEvent)
            return slashCommandInteractionEvent.getGuild();
        else if (event instanceof MessageReceivedEvent messageReceivedEvent)
            return messageReceivedEvent.getGuild();
        else
            return null;
    }

    public MessageChannelUnion getChannel() {
        if (event instanceof SlashCommandInteractionEvent slashCommandInteractionEvent)
            return slashCommandInteractionEvent.getChannel();
        else if (event instanceof MessageReceivedEvent messageReceivedEvent)
            return messageReceivedEvent.getChannel();
        else
            return null;
    }

    public ChannelType getChannelType() {
        if (event instanceof SlashCommandInteractionEvent slashCommandInteractionEvent)
            return slashCommandInteractionEvent.getChannelType();
        else if (event instanceof MessageReceivedEvent messageReceivedEvent)
            return messageReceivedEvent.getChannelType();
        else
            return null;
    }

    public User getAuthor() {
        if (event instanceof SlashCommandInteractionEvent slashCommandInteractionEvent)
            return slashCommandInteractionEvent.getUser();
        else if (event instanceof MessageReceivedEvent messageReceivedEvent)
            return messageReceivedEvent.getAuthor();
        else
            return null;
    }

    public boolean isFromGuild() {
        if (event instanceof SlashCommandInteractionEvent slashCommandInteractionEvent)
            return slashCommandInteractionEvent.isFromGuild();
        else if (event instanceof MessageReceivedEvent messageReceivedEvent)
            return messageReceivedEvent.isFromGuild();
        else
            return false;
    }
}

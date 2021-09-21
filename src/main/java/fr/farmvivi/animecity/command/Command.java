package fr.farmvivi.animecity.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {
    protected String name;

    protected String[] aliases = new String[0];

    protected String args;

    protected void execute(MessageReceivedEvent event, String content) {
        
    };
}

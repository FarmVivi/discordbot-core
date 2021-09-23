package fr.farmvivi.animecity.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {
    protected String name;

    protected String[] aliases = new String[0];

    protected String args;

    protected boolean adminOnly;

    protected boolean execute(MessageReceivedEvent event, String content) {
        if (adminOnly && !CommandsManager.ADMINS.contains(event.getAuthor().getIdLong())) {
            event.getChannel().sendMessage("Vous n'avez pas la permission d'exécuter cette commande.").queue();
            return false;
        }
        return true;
    };
}

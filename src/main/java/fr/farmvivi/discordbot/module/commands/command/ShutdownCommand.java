package fr.farmvivi.discordbot.module.commands.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ShutdownCommand extends Command {
    public ShutdownCommand() {
        this.name = "shutdown";
        this.category = CommandCategory.OTHER;
        this.description = "Éteint le bot";
        this.guildOnly = false;
        this.adminOnly = true;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        System.exit(0);

        return true;
    }
}

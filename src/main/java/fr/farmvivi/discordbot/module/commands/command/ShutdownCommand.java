package fr.farmvivi.discordbot.module.commands.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class ShutdownCommand extends Command {
    public ShutdownCommand() {
        super("shutdown", CommandCategory.OTHER, "Éteint le bot");

        this.setGuildOnly(false);
        this.setAdminOnly(true);
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        System.exit(0);

        return true;
    }
}

package fr.farmvivi.discordbot.module.goulag;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.Configuration;
import fr.farmvivi.discordbot.module.Module;
import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.commands.CommandsModule;
import fr.farmvivi.discordbot.module.goulag.command.GoulagCommand;

public class GoulagModule extends Module {
    private final Bot bot;

    public GoulagModule(Bot bot) {
        super(Modules.GOULAG);

        this.bot = bot;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        try {
            long respondentRoleId = -1;
            try {
                respondentRoleId = Long.parseLong(bot.getConfiguration().getValue("GOULAG_POLL_RESPONDENT_ROLE"));
            } catch (Configuration.ValueNotFoundException ignored) {
            }
            long goulagRoleId = Long.parseLong(bot.getConfiguration().getValue("GOULAG_ROLE"));
            long pollTimeout = Long.parseLong(bot.getConfiguration().getValue("GOULAG_POLL_TIMEOUT"));
            int minimumVotes = Integer.parseInt(bot.getConfiguration().getValue("GOULAG_POLL_MINIMUM_VOTES"));

            CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);

            commandsModule.registerCommand(module, new GoulagCommand(respondentRoleId, goulagRoleId, pollTimeout, minimumVotes));
        } catch (Configuration.ValueNotFoundException e) {
            logger.error("Failed to load configuration property", e);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);

        commandsModule.unregisterCommands(module);
    }
}

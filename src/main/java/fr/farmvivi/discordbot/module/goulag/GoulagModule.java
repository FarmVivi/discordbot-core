package fr.farmvivi.discordbot.module.goulag;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.module.Module;
import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.commands.CommandsModule;

public class GoulagModule extends Module {
    private final Bot bot;

    public GoulagModule(Bot bot) {
        super(Modules.GOULAG);

        this.bot = bot;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);

        //commandsModule.registerCommand(module, new PollCommand(this));
    }

    @Override
    public void onDisable() {
        super.onDisable();

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);

        commandsModule.unregisterCommands(module);
    }
}

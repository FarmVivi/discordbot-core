package fr.farmvivi.discordbot.module.test;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.module.Module;
import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.commands.CommandsModule;
import fr.farmvivi.discordbot.module.test.command.TestCommand;

public class TestModule extends Module {
    private final Modules module;
    private final Bot bot;

    public TestModule(Modules module, Bot bot) {
        super(module);

        this.module = module;
        this.bot = bot;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);

        commandsModule.registerCommand(module, new TestCommand(this));
    }

    @Override
    public void onDisable() {
        super.onDisable();

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);

        commandsModule.unregisterCommands(module);
    }
}

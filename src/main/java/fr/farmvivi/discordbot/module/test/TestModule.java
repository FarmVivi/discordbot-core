package fr.farmvivi.discordbot.module.test;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.module.Module;
import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.commands.CommandsModule;
import fr.farmvivi.discordbot.module.test.command.TestCommand;

public class TestModule extends Module {
    private final Bot bot;

    public TestModule(Modules module, Bot bot) {
        super(module);

        this.bot = bot;
    }

    @Override
    public void enable() {
        super.enable();

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager()
                .getModule(Modules.COMMANDS);

        commandsModule.registerCommand(new TestCommand(this));
    }

    @Override
    public void disable() {
        super.disable();

        // TODO Unreister commands
    }
}

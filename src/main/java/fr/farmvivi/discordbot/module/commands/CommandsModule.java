package fr.farmvivi.discordbot.module.commands;

import java.util.ArrayList;
import java.util.List;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.jda.JDAManager;
import fr.farmvivi.discordbot.module.Module;
import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.commands.command.HelpCommand;
import fr.farmvivi.discordbot.module.commands.command.ShutdownCommand;
import fr.farmvivi.discordbot.module.commands.command.VersionCommand;

public class CommandsModule extends Module {
    private final Bot bot;
    private final CommandsListener commandsListener;

    private final List<Command> commands = new ArrayList<>();

    public CommandsModule(Modules module, Bot bot) {
        super(module);

        this.bot = bot;
        this.commandsListener = new CommandsListener(this, bot.getConfiguration());
    }

    @Override
    public void enable() {
        super.enable();

        registerCommand(new HelpCommand(this, bot.getConfiguration()));
        registerCommand(new VersionCommand());
        registerCommand(new ShutdownCommand());

        JDAManager.getShardManager().addEventListener(commandsListener);
    }

    @Override
    public void disable() {
        super.disable();

        // TODO Unregister commands

        JDAManager.getShardManager().removeEventListener(commandsListener);
    }

    public void registerCommand(Command command) {
        logger.info("Registering command " + command.getName() + "...");

        commands.add(command);
    }

    public List<Command> getCommands() {
        return commands;
    }
}

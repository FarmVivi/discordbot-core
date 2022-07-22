package fr.farmvivi.discordbot.module.commands;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.jda.JDAManager;
import fr.farmvivi.discordbot.module.Module;
import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.commands.command.HelpCommand;
import fr.farmvivi.discordbot.module.commands.command.ShutdownCommand;
import fr.farmvivi.discordbot.module.commands.command.VersionCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandsModule extends Module {
    private final Modules module;
    private final Bot bot;
    private final CommandsListener commandsListener;

    private final Map<Modules, List<Command>> commands = new HashMap<>();

    public CommandsModule(Modules module, Bot bot) {
        super(module);

        this.module = module;
        this.bot = bot;
        this.commandsListener = new CommandsListener(this, bot.getConfiguration());
    }

    @Override
    public void enable() {
        registerCommand(module, new HelpCommand(this, bot.getConfiguration()));
        registerCommand(module, new VersionCommand());
        registerCommand(module, new ShutdownCommand());

        JDAManager.getShardManager().addEventListener(commandsListener);
    }

    @Override
    public void disable() {
        unregisterCommands(module);

        JDAManager.getShardManager().removeEventListener(commandsListener);
    }

    public void registerCommand(Modules module, Command command) {
        logger.info("Registering command " + command.getName() + "...");

        if (!commands.containsKey(module))
            commands.put(module, new ArrayList<>());

        List<Command> moduleCommands = commands.get(module);

        moduleCommands.add(command);
    }

    public void unregisterCommands(Modules module) {
        commands.remove(module);
    }

    public List<Command> getCommands(Modules module) {
        return commands.get(module);
    }

    public List<Command> getCommands() {
        List<Command> commands = new ArrayList<>();

        for (List<Command> moduleCommands : this.commands.values()) {
            commands.addAll(moduleCommands);
        }

        return commands;
    }
}

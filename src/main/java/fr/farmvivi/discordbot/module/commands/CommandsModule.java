package fr.farmvivi.discordbot.module.commands;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.jda.JDAManager;
import fr.farmvivi.discordbot.module.Module;
import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.commands.command.HelpCommand;
import fr.farmvivi.discordbot.module.commands.command.ShutdownCommand;
import fr.farmvivi.discordbot.module.commands.command.VersionCommand;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

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
    public void onEnable() {
        super.onEnable();

        registerCommand(module, new HelpCommand(this));
        registerCommand(module, new VersionCommand());
        registerCommand(module, new ShutdownCommand());

        JDAManager.getJDA().addEventListener(commandsListener);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        unregisterCommands(module);

        JDAManager.getJDA().removeEventListener(commandsListener);
    }

    @Override
    public void onPostEnable() {
        super.onPostEnable();

        logger.info("Registering commands to Discord API...");

        CommandListUpdateAction commandListUpdateAction = JDAManager.getJDA().updateCommands();

        for (Command command : getCommands()) {
            // Registering command to Discord API
            SlashCommandData commandData = Commands.slash(command.getName(), command.getDescription());
            if (command.getArgs().length > 0) {
                commandData.addOptions(command.getArgs());
            }
            commandData.setGuildOnly(command.isGuildOnly());
            logger.info("Registering " + command.getName() + " commands to Discord API...");
            commandListUpdateAction.addCommands(commandData);
        }

        commandListUpdateAction.queue();
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

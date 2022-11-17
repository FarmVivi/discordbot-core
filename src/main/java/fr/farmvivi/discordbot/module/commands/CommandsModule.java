package fr.farmvivi.discordbot.module.commands;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.jda.JDAManager;
import fr.farmvivi.discordbot.module.Module;
import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.commands.command.HelpCommand;
import fr.farmvivi.discordbot.module.commands.command.ShutdownCommand;
import fr.farmvivi.discordbot.module.commands.command.VersionCommand;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandsModule extends Module {
    private final Bot bot;
    private final CommandsEventHandler commandsEventHandler;

    private final Map<Modules, List<Command>> commands = new HashMap<>();

    public CommandsModule(Bot bot) {
        super(Modules.COMMANDS);

        this.bot = bot;
        this.commandsEventHandler = new CommandsEventHandler(this, bot.getConfiguration());
    }

    @Override
    public void onEnable() {
        super.onEnable();

        registerCommand(module, new HelpCommand(this));
        registerCommand(module, new VersionCommand());
        registerCommand(module, new ShutdownCommand());
    }

    @Override
    public void onPostEnable() {
        super.onPostEnable();

        logger.info("Registering commands to Discord API...");

        CommandListUpdateAction commandListUpdateAction = JDAManager.getJDA().updateCommands();

        for (Command command : getCommands()) {
            // Registering command to Discord API
            SlashCommandData commandData = Commands.slash(command.getName(), command.getDescription());

            // Adding options
            if (command.getArgs().length > 0) {
                int requiredIndex = 0;
                List<OptionData> options = new ArrayList<>();
                for (OptionData option : command.getArgs()) {
                    if (option.isRequired()) {
                        options.add(requiredIndex, option);
                        requiredIndex++;
                    } else {
                        options.add(option);
                    }
                }
                commandData.addOptions(options);
            }

            // Attributes
            commandData.setGuildOnly(command.isGuildOnly());

            // Adding command to Discord API
            logger.info("Registering " + command.getName() + " commands to Discord API...");
            commandListUpdateAction = commandListUpdateAction.addCommands(commandData);
        }

        commandListUpdateAction.queue();

        logger.info("Registering event listener...");

        JDAManager.getJDA().addEventListener(commandsEventHandler);
    }

    @Override
    public void onPreDisable() {
        super.onPreDisable();

        logger.info("Unregistering event listener...");

        JDAManager.getJDA().removeEventListener(commandsEventHandler);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        unregisterCommands(module);
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

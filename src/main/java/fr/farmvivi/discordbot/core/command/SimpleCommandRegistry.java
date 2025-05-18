package fr.farmvivi.discordbot.core.command;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandRegistry;
import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of the CommandRegistry interface.
 * This class manages all registered commands and their associations with plugins.
 */
public class SimpleCommandRegistry implements CommandRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleCommandRegistry.class);
    
    // Maps command name to command
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    
    // Maps plugin to its commands
    private final Map<Plugin, List<Command>> pluginCommands = new ConcurrentHashMap<>();
    
    // Maps command alias to command
    private final Map<String, Command> aliasMap = new ConcurrentHashMap<>();
    
    // Maps command to its owning plugin
    private final Map<Command, Plugin> commandPluginMap = new ConcurrentHashMap<>();
    
    // List of system commands (not associated with a plugin)
    private final List<Command> systemCommands = new ArrayList<>();
    
    @Override
    public boolean register(Command command, Plugin plugin) {
        Objects.requireNonNull(command, "Command cannot be null");
        // Plugin can be null for system commands
        
        String name = command.getName().toLowerCase();
        
        // Check if the command name is already taken
        if (commands.containsKey(name)) {
            logger.warn("Command '{}' is already registered", name);
            return false;
        }
        
        // Register the command
        commands.put(name, command);
        
        // If plugin is provided, associate the command with the plugin
        if (plugin != null) {
            pluginCommands.computeIfAbsent(plugin, k -> new ArrayList<>()).add(command);
            commandPluginMap.put(command, plugin);
            
            logger.debug("Registered command '{}' from plugin '{}'", name, plugin.getName());
        } else {
            // This is a system command
            systemCommands.add(command);
            logger.debug("Registered system command '{}'", name);
        }
        
        // Register aliases
        for (String alias : command.getAliases()) {
            String lowerAlias = alias.toLowerCase();
            if (aliasMap.containsKey(lowerAlias)) {
                logger.warn("Alias '{}' for command '{}' is already taken by command '{}'", 
                        lowerAlias, name, aliasMap.get(lowerAlias).getName());
                continue;
            }
            aliasMap.put(lowerAlias, command);
        }
        
        return true;
    }

    @Override
    public boolean unregister(String name) {
        name = name.toLowerCase();
        
        Command command = commands.remove(name);
        if (command == null) {
            return false;
        }
        
        // Remove from system commands if it's a system command
        systemCommands.remove(command);
        
        // Remove the command from the plugin's command list
        Plugin plugin = commandPluginMap.remove(command);
        if (plugin != null) {
            List<Command> pluginCommandList = pluginCommands.get(plugin);
            if (pluginCommandList != null) {
                pluginCommandList.remove(command);
                if (pluginCommandList.isEmpty()) {
                    pluginCommands.remove(plugin);
                }
            }
        }
        
        // Remove aliases
        for (String alias : command.getAliases()) {
            aliasMap.remove(alias.toLowerCase());
        }
        
        logger.debug("Unregistered command '{}'", name);
        return true;
    }

    @Override
    public int unregisterAll(Plugin plugin) {
        if (plugin == null) {
            // Unregister all system commands
            int count = systemCommands.size();
            
            for (Command command : new ArrayList<>(systemCommands)) {
                commands.remove(command.getName().toLowerCase());
                
                // Remove aliases
                for (String alias : command.getAliases()) {
                    aliasMap.remove(alias.toLowerCase());
                }
            }
            
            systemCommands.clear();
            logger.debug("Unregistered {} system commands", count);
            return count;
        }
        
        List<Command> commands = pluginCommands.remove(plugin);
        if (commands == null || commands.isEmpty()) {
            return 0;
        }
        
        int count = commands.size();
        
        // Remove all commands associated with the plugin
        for (Command command : commands) {
            this.commands.remove(command.getName().toLowerCase());
            commandPluginMap.remove(command);
            
            // Remove aliases
            for (String alias : command.getAliases()) {
                aliasMap.remove(alias.toLowerCase());
            }
        }
        
        logger.debug("Unregistered {} commands from plugin '{}'", count, plugin.getName());
        return count;
    }

    @Override
    public Optional<Command> getCommand(String name) {
        return Optional.ofNullable(commands.get(name.toLowerCase()));
    }

    @Override
    public Optional<Command> getCommandByAlias(String alias) {
        return Optional.ofNullable(aliasMap.get(alias.toLowerCase()));
    }

    @Override
    public Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }

    @Override
    public List<Command> getCommands(Plugin plugin) {
        if (plugin == null) {
            // Return system commands
            return Collections.unmodifiableList(systemCommands);
        }
        return pluginCommands.getOrDefault(plugin, List.of()).stream()
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Optional<Plugin> getPlugin(Command command) {
        return Optional.ofNullable(commandPluginMap.get(command));
    }

    @Override
    public Optional<Plugin> getPlugin(String commandName) {
        Command command = commands.get(commandName.toLowerCase());
        if (command == null) {
            return Optional.empty();
        }
        return getPlugin(command);
    }

    @Override
    public Collection<String> getCommandNames() {
        return Collections.unmodifiableSet(commands.keySet());
    }

    @Override
    public Collection<String> getCommandAliases() {
        return Collections.unmodifiableSet(aliasMap.keySet());
    }

    @Override
    public List<Command> getCommandsByCategory(String category) {
        return commands.values().stream()
                .filter(cmd -> category.equalsIgnoreCase(cmd.getCategory()))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<String> getCategories() {
        return commands.values().stream()
                .map(Command::getCategory)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean enableCommand(String name) {
        Optional<Command> commandOpt = getCommand(name);
        if (commandOpt.isEmpty() || commandOpt.get().isEnabled()) {
            return false;
        }
        
        Command command = commandOpt.get();
        
        // Since Command is immutable, we need to recreate it with the new enabled state
        if (command instanceof SimpleCommand simpleCommand) {
            SimpleCommand newCommand = new SimpleCommand(
                    simpleCommand.name(),
                    simpleCommand.description(),
                    simpleCommand.category(),
                    simpleCommand.options(),
                    simpleCommand.subcommands(),
                    simpleCommand.group(),
                    simpleCommand.permission(),
                    simpleCommand.translationKey(),
                    simpleCommand.aliases(),
                    simpleCommand.isGuildOnly(),
                    simpleCommand.guildIds(),
                    simpleCommand.isSubcommand(),
                    simpleCommand.getParent(),
                    true, // Set enabled to true
                    simpleCommand.getCooldown(),
                    simpleCommand.executor()
            );
            
            // Replace the command in all maps
            Plugin plugin = commandPluginMap.get(command);
            unregister(name);
            register(newCommand, plugin);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean disableCommand(String name) {
        Optional<Command> commandOpt = getCommand(name);
        if (commandOpt.isEmpty() || !commandOpt.get().isEnabled()) {
            return false;
        }
        
        Command command = commandOpt.get();
        
        // Since Command is immutable, we need to recreate it with the new enabled state
        if (command instanceof SimpleCommand simpleCommand) {
            SimpleCommand newCommand = new SimpleCommand(
                    simpleCommand.name(),
                    simpleCommand.description(),
                    simpleCommand.category(),
                    simpleCommand.options(),
                    simpleCommand.subcommands(),
                    simpleCommand.group(),
                    simpleCommand.permission(),
                    simpleCommand.translationKey(),
                    simpleCommand.aliases(),
                    simpleCommand.isGuildOnly(),
                    simpleCommand.guildIds(),
                    simpleCommand.isSubcommand(),
                    simpleCommand.getParent(),
                    false, // Set enabled to false
                    simpleCommand.getCooldown(),
                    simpleCommand.executor()
            );
            
            // Replace the command in all maps
            Plugin plugin = commandPluginMap.get(command);
            unregister(name);
            register(newCommand, plugin);
            return true;
        }
        
        return false;
    }
}

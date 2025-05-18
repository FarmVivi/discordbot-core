package fr.farmvivi.discordbot.core.command.impl;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of CommandRegistry that stores all registered commands.
 */
public class CommandRegistryImpl implements CommandRegistry {

    private static final Logger logger = LoggerFactory.getLogger(CommandRegistryImpl.class);
    
    /**
     * Map of command names to commands.
     */
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    
    /**
     * Map of command aliases to commands.
     */
    private final Map<String, Command> aliases = new ConcurrentHashMap<>();
    
    /**
     * Map of commands to their registering plugins.
     */
    private final Map<Command, Plugin> commandPlugins = new ConcurrentHashMap<>();
    
    /**
     * Map of plugins to their registered commands.
     */
    private final Map<Plugin, List<Command>> pluginCommands = new ConcurrentHashMap<>();
    
    /**
     * Map of command names to their enabled status.
     */
    private final Map<String, Boolean> commandStatus = new ConcurrentHashMap<>();
    
    @Override
    public boolean register(Command command, Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        
        // Check if the command name is already taken
        if (commands.containsKey(command.getName())) {
            logger.warn("Command {} already exists, cannot register from plugin {}", 
                    command.getName(), plugin.getName());
            return false;
        }
        
        // Check if any of the aliases are already taken
        for (String alias : command.getAliases()) {
            if (aliases.containsKey(alias)) {
                logger.warn("Alias {} for command {} is already used by command {}, cannot register", 
                        alias, command.getName(), aliases.get(alias).getName());
                return false;
            }
        }
        
        // Register the command
        commands.put(command.getName(), command);
        
        // Register the command's aliases
        for (String alias : command.getAliases()) {
            aliases.put(alias, command);
        }
        
        // Associate the command with the plugin
        commandPlugins.put(command, plugin);
        
        // Add the command to the plugin's commands
        pluginCommands.computeIfAbsent(plugin, k -> new ArrayList<>()).add(command);
        
        // Set the command's enabled status
        commandStatus.put(command.getName(), command.isEnabled());
        
        logger.debug("Registered command {} from plugin {}", command.getName(), plugin.getName());
        
        return true;
    }

    @Override
    public boolean unregister(String name) {
        // Get the command
        Command command = commands.get(name);
        if (command == null) {
            logger.warn("Command {} does not exist, cannot unregister", name);
            return false;
        }
        
        // Get the plugin
        Plugin plugin = commandPlugins.get(command);
        if (plugin == null) {
            logger.warn("Command {} has no associated plugin, cannot unregister", name);
            return false;
        }
        
        // Remove the command from the maps
        commands.remove(name);
        
        // Remove the command's aliases
        for (String alias : command.getAliases()) {
            aliases.remove(alias);
        }
        
        // Remove the command from the plugin's commands
        List<Command> pluginCommandList = pluginCommands.get(plugin);
        if (pluginCommandList != null) {
            pluginCommandList.remove(command);
            if (pluginCommandList.isEmpty()) {
                pluginCommands.remove(plugin);
            }
        }
        
        // Remove the command from the plugins map
        commandPlugins.remove(command);
        
        // Remove the command's enabled status
        commandStatus.remove(name);
        
        logger.debug("Unregistered command {} from plugin {}", name, plugin.getName());
        
        return true;
    }

    @Override
    public int unregisterAll(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        
        // Get the plugin's commands
        List<Command> pluginCommandList = pluginCommands.get(plugin);
        if (pluginCommandList == null || pluginCommandList.isEmpty()) {
            return 0;
        }
        
        // Make a copy of the list to avoid concurrent modification
        List<Command> commands = new ArrayList<>(pluginCommandList);
        int count = 0;
        
        // Unregister each command
        for (Command command : commands) {
            if (unregister(command.getName())) {
                count++;
            }
        }
        
        logger.debug("Unregistered {} commands from plugin {}", count, plugin.getName());
        
        return count;
    }

    @Override
    public Optional<Command> getCommand(String name) {
        return Optional.ofNullable(commands.get(name));
    }

    @Override
    public Optional<Command> getCommandByAlias(String alias) {
        return Optional.ofNullable(aliases.get(alias));
    }

    @Override
    public Collection<Command> getCommands() {
        // Return a copy of the commands to prevent modification
        return new ArrayList<>(commands.values());
    }

    @Override
    public List<Command> getCommands(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        
        // Get the plugin's commands
        List<Command> pluginCommandList = pluginCommands.get(plugin);
        if (pluginCommandList == null) {
            return Collections.emptyList();
        }
        
        // Return a copy of the list to prevent modification
        return new ArrayList<>(pluginCommandList);
    }

    @Override
    public Optional<Plugin> getPlugin(Command command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        
        return Optional.ofNullable(commandPlugins.get(command));
    }

    @Override
    public Optional<Plugin> getPlugin(String commandName) {
        if (commandName == null) {
            throw new IllegalArgumentException("Command name cannot be null");
        }
        
        Command command = commands.get(commandName);
        if (command == null) {
            return Optional.empty();
        }
        
        return Optional.ofNullable(commandPlugins.get(command));
    }

    @Override
    public Collection<String> getCommandNames() {
        // Return a copy of the command names to prevent modification
        return new HashSet<>(commands.keySet());
    }

    @Override
    public Collection<String> getCommandAliases() {
        // Return a copy of the aliases to prevent modification
        return new HashSet<>(aliases.keySet());
    }

    @Override
    public List<Command> getCommandsByCategory(String category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        
        // Filter commands by category
        return commands.values().stream()
                .filter(command -> category.equals(command.getCategory()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getCategories() {
        // Get all unique categories
        return commands.values().stream()
                .map(Command::getCategory)
                .distinct()
                .collect(Collectors.toSet());
    }

    @Override
    public boolean enableCommand(String name) {
        // Get the command
        Command command = commands.get(name);
        if (command == null) {
            logger.warn("Command {} does not exist, cannot enable", name);
            return false;
        }
        
        // Enable the command
        commandStatus.put(name, true);
        
        logger.debug("Enabled command {}", name);
        
        return true;
    }

    @Override
    public boolean disableCommand(String name) {
        // Get the command
        Command command = commands.get(name);
        if (command == null) {
            logger.warn("Command {} does not exist, cannot disable", name);
            return false;
        }
        
        // Disable the command
        commandStatus.put(name, false);
        
        logger.debug("Disabled command {}", name);
        
        return true;
    }
    
    /**
     * Checks if a command is enabled.
     *
     * @param name the command name
     * @return true if the command is enabled, false if disabled or not found
     */
    public boolean isCommandEnabled(String name) {
        Boolean status = commandStatus.get(name);
        return status != null && status;
    }
}

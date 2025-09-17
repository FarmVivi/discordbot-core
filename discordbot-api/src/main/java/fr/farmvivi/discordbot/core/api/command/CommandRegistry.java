package fr.farmvivi.discordbot.core.api.command;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Registry for commands.
 * Used to register and retrieve commands.
 */
public interface CommandRegistry {

    /**
     * Registers a command.
     *
     * @param command the command to register
     * @param plugin  the plugin registering the command
     * @return true if the command was registered, false if a command with the same name already exists
     */
    boolean register(Command command, Plugin plugin);

    /**
     * Unregisters a command by name.
     *
     * @param name the name of the command to unregister
     * @return true if the command was unregistered, false if the command was not found
     */
    boolean unregister(String name);

    /**
     * Unregisters all commands registered by a plugin.
     *
     * @param plugin the plugin
     * @return the number of commands unregistered
     */
    int unregisterAll(Plugin plugin);

    /**
     * Gets a command by name.
     *
     * @param name the command name
     * @return the command, or empty if not found
     */
    Optional<Command> getCommand(String name);

    /**
     * Gets a command by alias.
     *
     * @param alias the command alias
     * @return the command, or empty if not found
     */
    Optional<Command> getCommandByAlias(String alias);

    /**
     * Gets all registered commands.
     *
     * @return all registered commands
     */
    Collection<Command> getCommands();

    /**
     * Gets all commands registered by a plugin.
     *
     * @param plugin the plugin
     * @return the commands registered by the plugin
     */
    List<Command> getCommands(Plugin plugin);

    /**
     * Gets the plugin that registered a command.
     *
     * @param command the command
     * @return the plugin, or empty if the command is not registered
     */
    Optional<Plugin> getPlugin(Command command);

    /**
     * Gets the plugin that registered a command by name.
     *
     * @param commandName the command name
     * @return the plugin, or empty if the command is not registered
     */
    Optional<Plugin> getPlugin(String commandName);

    /**
     * Gets all registered command names.
     *
     * @return all registered command names
     */
    Collection<String> getCommandNames();

    /**
     * Gets all registered command aliases.
     *
     * @return all registered command aliases
     */
    Collection<String> getCommandAliases();

    /**
     * Gets all commands in a specific category.
     *
     * @param category the category
     * @return the commands in the category
     */
    List<Command> getCommandsByCategory(String category);

    /**
     * Gets all available categories.
     *
     * @return all available categories
     */
    Collection<String> getCategories();

    /**
     * Enables a command.
     *
     * @param name the command name
     * @return true if the command was enabled, false if the command was not found or already enabled
     */
    boolean enableCommand(String name);

    /**
     * Disables a command.
     *
     * @param name the command name
     * @return true if the command was disabled, false if the command was not found or already disabled
     */
    boolean disableCommand(String name);
}

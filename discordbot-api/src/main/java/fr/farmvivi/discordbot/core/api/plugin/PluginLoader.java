package fr.farmvivi.discordbot.core.api.plugin;

import java.util.List;

/**
 * Interface for loading and managing plugins.
 */
public interface PluginLoader {
    /**
     * Loads a plugin from a JAR file.
     *
     * @param jarFile the path to the JAR file
     * @return the loaded plugin, or null if loading failed
     */
    Plugin loadPlugin(String jarFile);

    /**
     * Enables a plugin.
     *
     * @param plugin the plugin to enable
     * @return true if the plugin was enabled successfully, false otherwise
     */
    boolean enablePlugin(Plugin plugin);

    /**
     * Disables a plugin.
     *
     * @param plugin the plugin to disable
     * @return true if the plugin was disabled successfully, false otherwise
     */
    boolean disablePlugin(Plugin plugin);

    /**
     * Gets a loaded plugin by name.
     *
     * @param name the name of the plugin
     * @return the plugin, or null if not found
     */
    Plugin getPlugin(String name);

    /**
     * Gets all loaded plugins.
     *
     * @return a list of all loaded plugins
     */
    List<Plugin> getPlugins();

    /**
     * Checks if a plugin is loaded.
     *
     * @param name the name of the plugin
     * @return true if the plugin is loaded, false otherwise
     */
    boolean isPluginLoaded(String name);
}
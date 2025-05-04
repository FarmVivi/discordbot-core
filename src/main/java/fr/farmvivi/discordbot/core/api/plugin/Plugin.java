package fr.farmvivi.discordbot.core.api.plugin;

/**
 * Base interface for all Discord bot plugins.
 * Each plugin must implement this interface to be detected and loaded.
 */
public interface Plugin {
    /**
     * Gets the name of the plugin.
     *
     * @return the plugin's name
     */
    String getName();

    /**
     * Gets the version of the plugin.
     *
     * @return the plugin's version string
     */
    String getVersion();

    /**
     * Called when the plugin is being loaded.
     *
     * @param pluginContext the context in which this plugin operates
     */
    void onLoad(PluginContext pluginContext);

    /**
     * Called during the pre-enable phase.
     * This is where plugins can configure Discord settings before connection.
     */
    void onPreEnable();

    /**
     * Called during the enable phase.
     * This is where most of the plugin's initialization should happen.
     */
    void onEnable();

    /**
     * Called during the post-enable phase.
     * This is where plugins can interact with other plugins.
     */
    void onPostEnable();

    /**
     * Called during the pre-disable phase.
     * This is where plugins prepare for shutdown.
     */
    void onPreDisable();

    /**
     * Called during the disable phase.
     * Clean up resources, save state, etc.
     */
    void onDisable();

    /**
     * Called during the post-disable phase.
     * Final cleanup after all plugins have been disabled.
     */
    void onPostDisable();

    /**
     * Gets the current status of the plugin.
     *
     * @return the plugin's status
     */
    PluginStatus getStatus();

    /**
     * Sets the status of the plugin.
     *
     * @param status the new status
     */
    void setStatus(PluginStatus status);
}
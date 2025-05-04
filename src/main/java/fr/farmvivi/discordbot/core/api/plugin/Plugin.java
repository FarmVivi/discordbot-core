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
     * Called when the plugin is being enabled.
     * This is where most of the plugin's initialization should happen.
     */
    void onEnable();

    /**
     * Called when the plugin is being disabled.
     * Clean up resources, save state, etc.
     */
    void onDisable();

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
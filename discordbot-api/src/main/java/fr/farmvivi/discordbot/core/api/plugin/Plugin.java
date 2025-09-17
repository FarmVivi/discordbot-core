package fr.farmvivi.discordbot.core.api.plugin;

/**
 * Core interface for all Discord bot plugins.
 * All plugins must implement this interface to be detected and loaded.
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
     * Initialize basic resources here, but avoid interacting with other plugins.
     *
     * @param context the context providing access to core services
     */
    void onLoad(PluginContext context);

    /**
     * Called during the pre-enable phase.
     * This is where plugins can configure Discord settings before connection.
     * Default implementation does nothing.
     */
    default void onPreEnable() {
        // Default implementation does nothing
    }

    /**
     * Called during the enable phase.
     * This is where most of the plugin's initialization should happen.
     */
    void onEnable();

    /**
     * Called during the post-enable phase.
     * This is where plugins can interact with other plugins.
     * Default implementation does nothing.
     */
    default void onPostEnable() {
        // Default implementation does nothing
    }

    /**
     * Called during the pre-disable phase.
     * This is where plugins prepare for shutdown.
     * Default implementation does nothing.
     */
    default void onPreDisable() {
        // Default implementation does nothing
    }

    /**
     * Called during the disable phase.
     * Clean up resources, save state, etc.
     */
    void onDisable();

    /**
     * Called during the post-disable phase.
     * Final cleanup after all plugins have been disabled.
     * Default implementation does nothing.
     */
    default void onPostDisable() {
        // Default implementation does nothing
    }

    /**
     * Gets the current lifecycle state of the plugin.
     *
     * @return the plugin's lifecycle state
     */
    PluginLifecycle getLifecycle();

    /**
     * Sets the lifecycle state of the plugin.
     * This should only be called by the plugin system.
     *
     * @param lifecycle the new lifecycle state
     */
    void setLifecycle(PluginLifecycle lifecycle);
}
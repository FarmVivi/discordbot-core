package fr.farmvivi.discordbot.core.api.plugin;

/**
 * Represents the lifecycle state of a plugin.
 * Plugins progress through these states during their lifetime.
 */
public enum PluginLifecycle {
    /**
     * Plugin JAR has been found but not loaded.
     */
    DISCOVERED,

    /**
     * Plugin has been loaded but not initialized.
     */
    LOADED,

    /**
     * Plugin is being pre-enabled (before Discord connection).
     */
    PRE_ENABLING,

    /**
     * Plugin is being enabled.
     */
    ENABLING,

    /**
     * Plugin is being post-enabled (after all plugins enabled).
     */
    POST_ENABLING,

    /**
     * Plugin is fully enabled and running.
     */
    ENABLED,

    /**
     * Plugin is being pre-disabled.
     */
    PRE_DISABLING,

    /**
     * Plugin is being disabled.
     */
    DISABLING,

    /**
     * Plugin is being post-disabled.
     */
    POST_DISABLING,

    /**
     * Plugin is fully disabled.
     */
    DISABLED,

    /**
     * Plugin has encountered an error and failed to load or enable.
     */
    ERROR;

    /**
     * Checks if this lifecycle state is an active state.
     *
     * @return true if the plugin is active (enabled)
     */
    public boolean isActive() {
        return this == ENABLED;
    }

    /**
     * Checks if this lifecycle state is a transitional state.
     *
     * @return true if the plugin is in a transitional state
     */
    public boolean isTransitional() {
        return this == PRE_ENABLING || this == ENABLING || this == POST_ENABLING ||
                this == PRE_DISABLING || this == DISABLING || this == POST_DISABLING;
    }
}
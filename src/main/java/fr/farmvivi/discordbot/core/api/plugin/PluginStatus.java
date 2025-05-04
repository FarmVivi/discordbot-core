package fr.farmvivi.discordbot.core.api.plugin;

/**
 * Represents the lifecycle status of a plugin.
 */
public enum PluginStatus {
    /**
     * Plugin JAR has been found and loaded but not initialized.
     */
    LOADED,

    /**
     * Plugin is in pre-enable phase, setting up dependencies.
     */
    PRE_ENABLE,

    /**
     * Plugin's onEnable method has been called.
     */
    ENABLE,

    /**
     * Plugin is in post-enable phase, setting up resources that depend on other plugins.
     */
    POST_ENABLE,

    /**
     * Plugin is fully enabled and running.
     */
    ENABLED,

    /**
     * Plugin is in pre-disable phase, preparing to shut down.
     */
    PRE_DISABLE,

    /**
     * Plugin's onDisable method has been called.
     */
    DISABLE,

    /**
     * Plugin is in post-disable phase, cleaning up resources.
     */
    POST_DISABLE,

    /**
     * Plugin is fully disabled.
     */
    DISABLED
}
package fr.farmvivi.discordbot.core.api.plugin.events;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import fr.farmvivi.discordbot.core.api.plugin.PluginLifecycle;

/**
 * Event fired when a plugin's lifecycle changes.
 * This can be used to track the lifecycle of a plugin.
 */
public class PluginLifecycleChangeEvent extends PluginEvent {
    private final PluginLifecycle oldStatus;
    private final PluginLifecycle newStatus;

    /**
     * Creates a new plugin status change event.
     *
     * @param plugin    the plugin
     * @param oldStatus the old status
     * @param newStatus the new status
     */
    public PluginLifecycleChangeEvent(Plugin plugin, PluginLifecycle oldStatus, PluginLifecycle newStatus) {
        super(plugin);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    /**
     * Gets the old status of the plugin.
     *
     * @return the old status
     */
    public PluginLifecycle getOldStatus() {
        return oldStatus;
    }

    /**
     * Gets the new status of the plugin.
     *
     * @return the new status
     */
    public PluginLifecycle getNewStatus() {
        return newStatus;
    }
}

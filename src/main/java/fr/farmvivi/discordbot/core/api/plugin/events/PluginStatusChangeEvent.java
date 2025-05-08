package fr.farmvivi.discordbot.core.api.plugin.events;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import fr.farmvivi.discordbot.core.api.plugin.PluginStatus;

/**
 * Event fired when a plugin's status changes.
 * This can be used to track the lifecycle of a plugin.
 */
public class PluginStatusChangeEvent extends PluginEvent {
    private final PluginStatus oldStatus;
    private final PluginStatus newStatus;

    /**
     * Creates a new plugin status change event.
     *
     * @param plugin    the plugin
     * @param oldStatus the old status
     * @param newStatus the new status
     */
    public PluginStatusChangeEvent(Plugin plugin, PluginStatus oldStatus, PluginStatus newStatus) {
        super(plugin);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    /**
     * Gets the old status of the plugin.
     *
     * @return the old status
     */
    public PluginStatus getOldStatus() {
        return oldStatus;
    }

    /**
     * Gets the new status of the plugin.
     *
     * @return the new status
     */
    public PluginStatus getNewStatus() {
        return newStatus;
    }
}

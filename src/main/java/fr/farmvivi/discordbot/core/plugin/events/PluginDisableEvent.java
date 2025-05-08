package fr.farmvivi.discordbot.core.plugin.events;

import fr.farmvivi.discordbot.core.api.event.Cancellable;
import fr.farmvivi.discordbot.core.api.plugin.Plugin;

/**
 * Event fired before a plugin is disabled.
 * This event is fired before the plugin's onDisable method is called.
 * This event is cancellable. If it is cancelled, the plugin will not be disabled.
 */
public class PluginDisableEvent extends PluginEvent implements Cancellable {
    private boolean cancelled = false;

    /**
     * Creates a new plugin disable event.
     *
     * @param plugin the plugin being disabled
     */
    public PluginDisableEvent(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

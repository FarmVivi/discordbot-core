package fr.farmvivi.discordbot.core.plugin.events;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;

/**
 * Event fired after a plugin has been disabled.
 * This event is fired after the plugin's onDisable method has been called.
 */
public class PluginDisabledEvent extends PluginEvent {
    /**
     * Creates a new plugin disabled event.
     *
     * @param plugin the plugin that was disabled
     */
    public PluginDisabledEvent(Plugin plugin) {
        super(plugin);
    }
}

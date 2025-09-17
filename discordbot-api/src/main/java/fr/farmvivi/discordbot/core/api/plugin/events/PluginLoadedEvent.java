package fr.farmvivi.discordbot.core.api.plugin.events;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;

/**
 * Event fired when a plugin has finished loading.
 * This event is fired after the plugin's onLoad method has been called.
 */
public class PluginLoadedEvent extends PluginEvent {
    /**
     * Creates a new plugin loaded event.
     *
     * @param plugin the plugin that was loaded
     */
    public PluginLoadedEvent(Plugin plugin) {
        super(plugin);
    }
}

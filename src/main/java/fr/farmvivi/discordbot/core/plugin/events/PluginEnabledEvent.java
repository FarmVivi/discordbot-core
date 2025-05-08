package fr.farmvivi.discordbot.core.plugin.events;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;

/**
 * Event fired after a plugin has been enabled.
 * This event is fired after the plugin's onEnable method has been called.
 */
public class PluginEnabledEvent extends PluginEvent {
    /**
     * Creates a new plugin enabled event.
     *
     * @param plugin the plugin that was enabled
     */
    public PluginEnabledEvent(Plugin plugin) {
        super(plugin);
    }
}

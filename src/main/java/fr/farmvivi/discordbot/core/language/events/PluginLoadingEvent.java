package fr.farmvivi.discordbot.core.language.events;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import fr.farmvivi.discordbot.core.plugin.events.PluginEvent;

/**
 * Event fired when a plugin is loading.
 * This event is fired before the plugin's onLoad method is called.
 */
public class PluginLoadingEvent extends PluginEvent {
    /**
     * Creates a new plugin loading event.
     *
     * @param plugin the plugin being loaded
     */
    public PluginLoadingEvent(Plugin plugin) {
        super(plugin);
    }
}

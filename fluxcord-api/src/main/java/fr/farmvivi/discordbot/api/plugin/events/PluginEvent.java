package fr.farmvivi.discordbot.api.plugin.events;

import fr.farmvivi.discordbot.api.event.Event;
import fr.farmvivi.discordbot.api.plugin.Plugin;

/**
 * Base class for plugin lifecycle events.
 */
public abstract class PluginEvent implements Event {
    private final Plugin plugin;

    /**
     * Creates a new plugin event.
     *
     * @param plugin the plugin involved in the event
     */
    protected PluginEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the plugin involved in the event.
     *
     * @return the plugin
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Gets the name of the plugin.
     *
     * @return the plugin name
     */
    public String getPluginName() {
        return plugin.getName();
    }
}
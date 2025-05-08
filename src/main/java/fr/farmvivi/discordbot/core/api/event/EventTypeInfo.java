package fr.farmvivi.discordbot.core.api.event;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;

/**
 * Information about a registered event type.
 * This class provides metadata about event types registered with the EventRegistry.
 */
public class EventTypeInfo {
    private final Class<? extends Event> eventType;
    private final Plugin plugin;
    private final String description;
    private final boolean cancellable;

    /**
     * Creates new event type information.
     *
     * @param eventType   the event type
     * @param plugin      the plugin that registered the event
     * @param description a description of the event
     */
    public EventTypeInfo(Class<? extends Event> eventType, Plugin plugin, String description) {
        this.eventType = eventType;
        this.plugin = plugin;
        this.description = description;
        this.cancellable = Cancellable.class.isAssignableFrom(eventType);
    }

    /**
     * Gets the event type.
     *
     * @return the event type
     */
    public Class<? extends Event> getEventType() {
        return eventType;
    }

    /**
     * Gets the plugin that registered the event type.
     *
     * @return the plugin
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Gets the description of the event type.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if the event type is cancellable.
     *
     * @return true if the event type is cancellable, false otherwise
     */
    public boolean isCancellable() {
        return cancellable;
    }

    /**
     * Gets the simple name of the event type.
     *
     * @return the simple name of the event type
     */
    public String getSimpleName() {
        return eventType.getSimpleName();
    }

    /**
     * Gets the fully qualified name of the event type.
     *
     * @return the fully qualified name of the event type
     */
    public String getFullName() {
        return eventType.getName();
    }

    @Override
    public String toString() {
        return String.format("%s (plugin: %s, cancellable: %s): %s",
                getSimpleName(), plugin.getName(), cancellable, description);
    }
}
package fr.farmvivi.discordbot.core.api.event;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;

import java.util.Set;

/**
 * Interface for registering and managing event types.
 * This provides a central registry for event types, allowing plugins to register
 * their own custom events and discover registered events.
 */
public interface EventRegistry {
    /**
     * Registers an event type with the registry.
     * Plugins should register their custom event types during initialization
     * to make them discoverable by other plugins.
     *
     * @param eventType   the event type to register
     * @param plugin      the plugin registering the event
     * @param description a description of the event
     * @return true if the event type was registered, false if it was already registered
     */
    boolean registerEventType(Class<? extends Event> eventType, Plugin plugin, String description);

    /**
     * Registers an event type with the registry.
     * This version uses a default empty description.
     *
     * @param eventType the event type to register
     * @param plugin    the plugin registering the event
     * @return true if the event type was registered, false if it was already registered
     */
    boolean registerEventType(Class<? extends Event> eventType, Plugin plugin);

    /**
     * Unregisters all event types registered by a plugin.
     * This is called automatically when a plugin is disabled.
     *
     * @param plugin the plugin
     * @return the number of event types unregistered
     */
    int unregisterEventTypes(Plugin plugin);

    /**
     * Gets information about a registered event type.
     *
     * @param eventType the event type
     * @return the event type information, or null if the event type is not registered
     */
    EventTypeInfo getEventTypeInfo(Class<? extends Event> eventType);

    /**
     * Gets all registered event types.
     *
     * @return a set of all registered event types
     */
    Set<Class<? extends Event>> getRegisteredEventTypes();

    /**
     * Gets all event types registered by a specific plugin.
     *
     * @param plugin the plugin
     * @return a set of event types registered by the plugin
     */
    Set<Class<? extends Event>> getPluginEventTypes(Plugin plugin);

    /**
     * Gets the plugin that registered an event type.
     *
     * @param eventType the event type
     * @return the plugin that registered the event type, or null if the event type is not registered
     */
    Plugin getEventTypeOwner(Class<? extends Event> eventType);
}
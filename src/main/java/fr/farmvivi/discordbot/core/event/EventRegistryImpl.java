package fr.farmvivi.discordbot.core.event;

import fr.farmvivi.discordbot.core.api.event.Event;
import fr.farmvivi.discordbot.core.api.event.EventRegistry;
import fr.farmvivi.discordbot.core.api.event.EventTypeInfo;
import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the EventRegistry interface.
 * This class provides a standalone implementation of EventRegistry
 * that can be used by SimpleEventManager.
 */
public class EventRegistryImpl implements EventRegistry {
    private static final Logger logger = LoggerFactory.getLogger(EventRegistryImpl.class);

    // Event type registry
    private final Map<Class<? extends Event>, EventTypeInfo> eventTypeRegistry = new ConcurrentHashMap<>();
    private final Map<Plugin, Set<Class<? extends Event>>> pluginEventTypes = new ConcurrentHashMap<>();

    @Override
    public boolean registerEventType(Class<? extends Event> eventType, Plugin plugin, String description) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }

        // Check if already registered
        if (eventTypeRegistry.containsKey(eventType)) {
            EventTypeInfo existingInfo = eventTypeRegistry.get(eventType);
            logger.warn("Event type {} is already registered by plugin {}",
                    eventType.getName(), existingInfo.getPlugin().getName());
            return false;
        }

        // Create event type info
        EventTypeInfo info = new EventTypeInfo(eventType, plugin, description);
        eventTypeRegistry.put(eventType, info);

        // Add to plugin event types
        pluginEventTypes.computeIfAbsent(plugin, k -> ConcurrentHashMap.newKeySet())
                .add(eventType);

        logger.debug("Registered event type {} owned by plugin {}",
                eventType.getName(), plugin.getName());
        return true;
    }

    @Override
    public boolean registerEventType(Class<? extends Event> eventType, Plugin plugin) {
        return registerEventType(eventType, plugin, "");
    }

    @Override
    public int unregisterEventTypes(Plugin plugin) {
        if (plugin == null) {
            return 0;
        }

        Set<Class<? extends Event>> eventTypes = pluginEventTypes.get(plugin);
        if (eventTypes == null || eventTypes.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Class<? extends Event> eventType : new HashSet<>(eventTypes)) {
            eventTypeRegistry.remove(eventType);
            count++;
        }

        pluginEventTypes.remove(plugin);

        logger.debug("Unregistered {} event types for plugin {}",
                count, plugin.getName());
        return count;
    }

    @Override
    public EventTypeInfo getEventTypeInfo(Class<? extends Event> eventType) {
        return eventTypeRegistry.get(eventType);
    }

    @Override
    public Set<Class<? extends Event>> getRegisteredEventTypes() {
        return new HashSet<>(eventTypeRegistry.keySet());
    }

    @Override
    public Set<Class<? extends Event>> getPluginEventTypes(Plugin plugin) {
        Set<Class<? extends Event>> types = pluginEventTypes.get(plugin);
        return types != null ? Collections.unmodifiableSet(types) : Collections.emptySet();
    }

    @Override
    public Plugin getEventTypeOwner(Class<? extends Event> eventType) {
        EventTypeInfo info = eventTypeRegistry.get(eventType);
        return info != null ? info.getPlugin() : null;
    }
}
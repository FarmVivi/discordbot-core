package fr.farmvivi.discordbot.core.api.storage.events;

import fr.farmvivi.discordbot.core.api.event.Event;
import fr.farmvivi.discordbot.core.api.storage.StorageKey;

/**
 * Base class for storage events.
 */
public abstract class StorageEvent implements Event {
    private final StorageKey key;
    private Object value;

    /**
     * Creates a new storage event.
     *
     * @param key   the storage key
     * @param value the value (may be null)
     */
    protected StorageEvent(StorageKey key, Object value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Gets the storage key.
     *
     * @return the storage key
     */
    public StorageKey getKey() {
        return key;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(Object value) {
        this.value = value;
    }
}
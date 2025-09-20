package fr.farmvivi.fluxcord.api.storage.binary.events;

import fr.farmvivi.fluxcord.api.event.Event;
import fr.farmvivi.fluxcord.api.storage.binary.BinaryStorageKey;

/**
 * Base class for binary storage events.
 */
public abstract class BinaryStorageEvent implements Event {
    private final BinaryStorageKey key;

    /**
     * Creates a new binary storage event.
     *
     * @param key the storage key
     */
    protected BinaryStorageEvent(BinaryStorageKey key) {
        this.key = key;
    }

    /**
     * Gets the storage key.
     *
     * @return the storage key
     */
    public BinaryStorageKey getKey() {
        return key;
    }
}
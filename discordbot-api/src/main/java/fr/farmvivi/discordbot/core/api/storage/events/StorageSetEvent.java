package fr.farmvivi.discordbot.core.api.storage.events;

import fr.farmvivi.discordbot.core.api.event.Cancellable;
import fr.farmvivi.discordbot.core.api.storage.StorageKey;

/**
 * Event fired when a value is set in storage.
 */
public class StorageSetEvent extends StorageEvent implements Cancellable {
    private boolean cancelled = false;

    /**
     * Creates a new storage set event.
     *
     * @param key   the storage key
     * @param value the value to set
     */
    public StorageSetEvent(StorageKey key, Object value) {
        super(key, value);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
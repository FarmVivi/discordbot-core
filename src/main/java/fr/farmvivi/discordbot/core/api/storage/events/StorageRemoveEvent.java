package fr.farmvivi.discordbot.core.api.storage.events;

import fr.farmvivi.discordbot.core.api.event.Cancellable;
import fr.farmvivi.discordbot.core.api.storage.StorageKey;

/**
 * Event fired when a key is removed from storage.
 */
public class StorageRemoveEvent extends StorageEvent implements Cancellable {
    private boolean cancelled = false;

    /**
     * Creates a new storage remove event.
     *
     * @param key the storage key
     */
    public StorageRemoveEvent(StorageKey key) {
        super(key, null);
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
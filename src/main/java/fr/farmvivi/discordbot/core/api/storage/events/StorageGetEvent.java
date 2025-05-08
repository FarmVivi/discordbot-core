package fr.farmvivi.discordbot.core.api.storage.events;

import fr.farmvivi.discordbot.core.api.event.Cancellable;
import fr.farmvivi.discordbot.core.api.storage.StorageKey;

/**
 * Event fired when a value is retrieved from storage.
 */
public class StorageGetEvent extends StorageEvent implements Cancellable {
    private boolean cancelled = false;
    private final Class<?> type;

    /**
     * Creates a new storage get event.
     *
     * @param key   the storage key
     * @param type  the requested type
     * @param value the retrieved value (may be null)
     */
    public StorageGetEvent(StorageKey key, Class<?> type, Object value) {
        super(key, value);
        this.type = type;
    }

    /**
     * Gets the requested type.
     *
     * @return the requested type
     */
    public Class<?> getType() {
        return type;
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

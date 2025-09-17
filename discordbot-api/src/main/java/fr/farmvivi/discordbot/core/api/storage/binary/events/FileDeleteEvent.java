package fr.farmvivi.discordbot.core.api.storage.binary.events;

import fr.farmvivi.discordbot.core.api.event.Cancellable;
import fr.farmvivi.discordbot.core.api.storage.binary.BinaryStorageKey;

/**
 * Event fired before/after a file is deleted from storage.
 */
public class FileDeleteEvent extends BinaryStorageEvent implements Cancellable {
    private boolean cancelled = false;

    /**
     * Creates a new file delete event.
     *
     * @param key the storage key
     */
    public FileDeleteEvent(BinaryStorageKey key) {
        super(key);
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
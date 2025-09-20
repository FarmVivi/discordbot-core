package fr.farmvivi.discordbot.api.storage.binary.events;

import fr.farmvivi.discordbot.api.event.Cancellable;
import fr.farmvivi.discordbot.api.storage.binary.BinaryStorageKey;

import java.io.File;

/**
 * Event fired before/after a file is downloaded from storage.
 */
public class FileDownloadEvent extends BinaryStorageEvent implements Cancellable {
    private final File destination;
    private boolean cancelled = false;

    /**
     * Creates a new file download event.
     *
     * @param key         the storage key
     * @param destination the destination file
     */
    public FileDownloadEvent(BinaryStorageKey key, File destination) {
        super(key);
        this.destination = destination;
    }

    /**
     * Gets the destination file.
     *
     * @return the destination file
     */
    public File getDestination() {
        return destination;
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
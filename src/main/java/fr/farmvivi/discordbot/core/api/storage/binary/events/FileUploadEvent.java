package fr.farmvivi.discordbot.core.api.storage.binary.events;

import fr.farmvivi.discordbot.core.api.event.Cancellable;
import fr.farmvivi.discordbot.core.api.storage.binary.BinaryStorageKey;

/**
 * Event fired before/after a file is uploaded to storage.
 */
public class FileUploadEvent extends BinaryStorageEvent implements Cancellable {
    private boolean cancelled = false;
    private final Object source; // File or InputStream
    private final boolean overwrite;

    /**
     * Creates a new file upload event.
     *
     * @param key       the storage key
     * @param source    the source (File or InputStream)
     * @param overwrite whether to overwrite if a file already exists
     */
    public FileUploadEvent(BinaryStorageKey key, Object source, boolean overwrite) {
        super(key);
        this.source = source;
        this.overwrite = overwrite;
    }

    /**
     * Gets the source of the upload.
     *
     * @return the source (File or InputStream)
     */
    public Object getSource() {
        return source;
    }

    /**
     * Checks if the upload will overwrite an existing file.
     *
     * @return true if overwrite is enabled
     */
    public boolean isOverwrite() {
        return overwrite;
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

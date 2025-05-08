package fr.farmvivi.discordbot.core.storage.binary;

import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.storage.binary.BinaryStorage;
import fr.farmvivi.discordbot.core.api.storage.binary.BinaryStorageKey;
import fr.farmvivi.discordbot.core.api.storage.binary.events.FileDeleteEvent;
import fr.farmvivi.discordbot.core.api.storage.binary.events.FileDownloadEvent;
import fr.farmvivi.discordbot.core.api.storage.binary.events.FileUploadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract implementation of BinaryStorage with common functionality.
 */
public abstract class AbstractBinaryStorage implements BinaryStorage {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractBinaryStorage.class);
    protected static final int BUFFER_SIZE = 8192;

    protected final String storageName;
    protected final EventManager eventManager;

    // Cache for content types to avoid repeated computation
    private static final Map<String, String> contentTypeCache = new ConcurrentHashMap<>();

    /**
     * Creates a new abstract binary storage.
     *
     * @param storageName  the name of this storage
     * @param eventManager the event manager, or null if events should not be fired
     */
    protected AbstractBinaryStorage(String storageName, EventManager eventManager) {
        this.storageName = storageName;
        this.eventManager = eventManager;
    }

    @Override
    public boolean saveFile(BinaryStorageKey key, File file, boolean overwrite) {
        if (!file.exists() || !file.isFile()) {
            logger.error("[{}] Cannot save non-existent file: {}", storageName, file.getAbsolutePath());
            return false;
        }

        // Fire pre-upload event
        if (eventManager != null) {
            FileUploadEvent event = new FileUploadEvent(key, file, overwrite);
            eventManager.fireEvent(event);

            if (event.isCancelled()) {
                logger.debug("[{}] File upload was cancelled by an event listener", storageName);
                return false;
            }
        }

        try (InputStream is = new FileInputStream(file)) {
            return saveFile(key, is, overwrite);
        } catch (IOException e) {
            logger.error("[{}] Error reading file {}: {}", storageName, file.getAbsolutePath(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean saveFile(BinaryStorageKey key, InputStream inputStream, boolean overwrite) {
        // File exists check handled by implementation

        // Fire pre-upload event
        if (eventManager != null) {
            FileUploadEvent event = new FileUploadEvent(key, inputStream, overwrite);
            eventManager.fireEvent(event);

            if (event.isCancelled()) {
                logger.debug("[{}] File upload was cancelled by an event listener", storageName);
                return false;
            }
        }

        try (OutputStream os = getOutputStream(key, overwrite)) {
            if (os == null) {
                logger.error("[{}] Failed to get output stream for path: {}", storageName, key.getFullPath());
                return false;
            }

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            return true;
        } catch (IOException e) {
            logger.error("[{}] Error saving file to path {}: {}",
                    storageName, key.getFullPath(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean downloadFile(BinaryStorageKey key, File destFile) {
        if (!fileExists(key)) {
            logger.error("[{}] File does not exist at path: {}", storageName, key.getFullPath());
            return false;
        }

        // Fire pre-download event
        if (eventManager != null) {
            FileDownloadEvent event = new FileDownloadEvent(key, destFile);
            eventManager.fireEvent(event);

            if (event.isCancelled()) {
                logger.debug("[{}] File download was cancelled by an event listener", storageName);
                return false;
            }
        }

        try {
            // Create parent directories if they don't exist
            File parentDir = destFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    logger.error("[{}] Failed to create parent directories for: {}",
                            storageName, destFile.getAbsolutePath());
                    return false;
                }
            }

            Optional<InputStream> isOpt = getInputStream(key);
            if (isOpt.isEmpty()) {
                logger.error("[{}] Failed to get input stream for path: {}", storageName, key.getFullPath());
                return false;
            }

            try (InputStream is = isOpt.get()) {
                Files.copy(is, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                return true;
            }
        } catch (IOException e) {
            logger.error("[{}] Error downloading file from path {} to {}: {}",
                    storageName, key.getFullPath(), destFile.getAbsolutePath(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteFile(BinaryStorageKey key) {
        if (!fileExists(key)) {
            return false;
        }

        // Fire pre-delete event
        if (eventManager != null) {
            FileDeleteEvent event = new FileDeleteEvent(key);
            eventManager.fireEvent(event);

            if (event.isCancelled()) {
                logger.debug("[{}] File deletion was cancelled by an event listener", storageName);
                return false;
            }
        }

        boolean result = doDeleteFile(key);

        return result;
    }

    @Override
    public String getContentType(BinaryStorageKey key) {
        String path = key.path();

        // Check cache first
        return contentTypeCache.computeIfAbsent(path, this::determineContentType);
    }

    /**
     * Determines the content type based on file extension.
     *
     * @param path the file path
     * @return the content type
     */
    protected String determineContentType(String path) {
        String lowerPath = path.toLowerCase();

        // Image types
        if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerPath.endsWith(".png")) {
            return "image/png";
        } else if (lowerPath.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerPath.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerPath.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (lowerPath.endsWith(".ico")) {
            return "image/x-icon";
        }

        // Audio types
        if (lowerPath.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (lowerPath.endsWith(".wav")) {
            return "audio/wav";
        } else if (lowerPath.endsWith(".ogg")) {
            return "audio/ogg";
        } else if (lowerPath.endsWith(".flac")) {
            return "audio/flac";
        }

        // Video types
        if (lowerPath.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerPath.endsWith(".webm")) {
            return "video/webm";
        } else if (lowerPath.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (lowerPath.endsWith(".mov")) {
            return "video/quicktime";
        }

        // Document types
        if (lowerPath.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerPath.endsWith(".doc")) {
            return "application/msword";
        } else if (lowerPath.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (lowerPath.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        } else if (lowerPath.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (lowerPath.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        } else if (lowerPath.endsWith(".pptx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        }

        // Text types
        if (lowerPath.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerPath.endsWith(".html") || lowerPath.endsWith(".htm")) {
            return "text/html";
        } else if (lowerPath.endsWith(".css")) {
            return "text/css";
        } else if (lowerPath.endsWith(".js")) {
            return "application/javascript";
        } else if (lowerPath.endsWith(".json")) {
            return "application/json";
        } else if (lowerPath.endsWith(".xml")) {
            return "application/xml";
        }

        // Archive types
        if (lowerPath.endsWith(".zip")) {
            return "application/zip";
        } else if (lowerPath.endsWith(".gz") || lowerPath.endsWith(".gzip")) {
            return "application/gzip";
        } else if (lowerPath.endsWith(".tar")) {
            return "application/x-tar";
        } else if (lowerPath.endsWith(".rar")) {
            return "application/x-rar-compressed";
        } else if (lowerPath.endsWith(".7z")) {
            return "application/x-7z-compressed";
        }

        // Default
        return "application/octet-stream";
    }

    /**
     * Lists files in a directory and caches the result.
     * Default implementation returns an empty list.
     *
     * @param key the directory key
     * @return a list of file paths
     */
    @Override
    public List<String> listFiles(BinaryStorageKey key) {
        return Collections.emptyList();
    }

    /**
     * Gets a public URL for a file.
     * Default implementation returns an empty Optional.
     *
     * @param key      the file key
     * @param expireIn the number of seconds until the URL expires
     * @return an Optional containing the URL
     */
    @Override
    public Optional<String> getPublicUrl(BinaryStorageKey key, int expireIn) {
        return Optional.empty();
    }

    /**
     * Performs the actual file deletion operation.
     * Implementations should override this method to delete the file.
     *
     * @param key the key of the file to delete
     * @return true if the deletion was successful
     */
    protected abstract boolean doDeleteFile(BinaryStorageKey key);

    @Override
    public String toString() {
        return "BinaryStorage[" + storageName + "]";
    }
}
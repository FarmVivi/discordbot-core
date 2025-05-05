package fr.farmvivi.discordbot.core.data.binary;

import fr.farmvivi.discordbot.core.api.data.binary.BinaryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Abstract implementation of BinaryStorage that provides common functionality.
 * This class implements common operations that can be shared between different storage backends.
 */
public abstract class AbstractBinaryStorage implements BinaryStorage {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractBinaryStorage.class);
    protected static final int BUFFER_SIZE = 8192;

    protected final String storageName;

    /**
     * Creates a new abstract binary storage with the specified name.
     *
     * @param storageName the name of the storage
     */
    protected AbstractBinaryStorage(String storageName) {
        this.storageName = storageName;
    }

    @Override
    public boolean saveFile(String path, File file, boolean overwrite) {
        if (!file.exists() || !file.isFile()) {
            logger.error("[{}] Cannot save non-existent file: {}", storageName, file.getAbsolutePath());
            return false;
        }

        if (!overwrite && fileExists(path)) {
            logger.warn("[{}] File already exists at path {} and overwrite is false", storageName, path);
            return false;
        }

        try (InputStream is = new FileInputStream(file)) {
            return saveFile(path, is, overwrite);
        } catch (IOException e) {
            logger.error("[{}] Error reading file {}: {}", storageName, file.getAbsolutePath(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean saveFile(String path, InputStream inputStream, boolean overwrite) {
        if (!overwrite && fileExists(path)) {
            logger.warn("[{}] File already exists at path {} and overwrite is false", storageName, path);
            return false;
        }

        try (OutputStream os = getOutputStream(path, overwrite)) {
            if (os == null) {
                logger.error("[{}] Failed to get output stream for path: {}", storageName, path);
                return false;
            }

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (IOException e) {
            logger.error("[{}] Error saving file to path {}: {}", storageName, path, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean downloadFile(String path, File destFile) {
        if (!fileExists(path)) {
            logger.error("[{}] File does not exist at path: {}", storageName, path);
            return false;
        }

        try {
            // Create parent directories if they don't exist
            File parentDir = destFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    logger.error("[{}] Failed to create parent directories for: {}", storageName, destFile.getAbsolutePath());
                    return false;
                }
            }

            try (InputStream is = getInputStream(path)) {
                if (is == null) {
                    logger.error("[{}] Failed to get input stream for path: {}", storageName, path);
                    return false;
                }

                Files.copy(is, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
        } catch (IOException e) {
            logger.error("[{}] Error downloading file from path {} to {}: {}",
                    storageName, path, destFile.getAbsolutePath(), e.getMessage());
            return false;
        }
    }

    /**
     * Ensures the path is normalized and consistent.
     *
     * @param path the path to normalize
     * @return the normalized path
     */
    protected String normalizePath(String path) {
        if (path == null) {
            return "";
        }

        // Replace backslashes with forward slashes
        String normalized = path.replace('\\', '/');

        // Remove leading slash if present
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        return normalized;
    }

    /**
     * Extracts the content type from a file path based on its extension.
     *
     * @param path the file path
     * @return the content type, or "application/octet-stream" if unknown
     */
    protected String getContentTypeFromPath(String path) {
        String lowerPath = path.toLowerCase();

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
        } else if (lowerPath.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (lowerPath.endsWith(".wav")) {
            return "audio/wav";
        } else if (lowerPath.endsWith(".ogg")) {
            return "audio/ogg";
        } else if (lowerPath.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerPath.endsWith(".webm")) {
            return "video/webm";
        } else if (lowerPath.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerPath.endsWith(".txt")) {
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
        } else if (lowerPath.endsWith(".zip")) {
            return "application/zip";
        } else if (lowerPath.endsWith(".gz") || lowerPath.endsWith(".gzip")) {
            return "application/gzip";
        } else if (lowerPath.endsWith(".tar")) {
            return "application/x-tar";
        } else if (lowerPath.endsWith(".doc") || lowerPath.endsWith(".docx")) {
            return "application/msword";
        } else if (lowerPath.endsWith(".xls") || lowerPath.endsWith(".xlsx")) {
            return "application/vnd.ms-excel";
        } else if (lowerPath.endsWith(".ppt") || lowerPath.endsWith(".pptx")) {
            return "application/vnd.ms-powerpoint";
        }

        return "application/octet-stream";
    }

    /**
     * Gets the parent directory path from a file path.
     *
     * @param path the file path
     * @return the parent directory path, or empty string if there is no parent
     */
    protected String getParentPath(String path) {
        int lastSlash = normalizePath(path).lastIndexOf('/');
        if (lastSlash < 0) {
            return "";
        }
        return path.substring(0, lastSlash);
    }

    /**
     * Gets the file name from a path.
     *
     * @param path the file path
     * @return the file name
     */
    protected String getFileName(String path) {
        int lastSlash = normalizePath(path).lastIndexOf('/');
        if (lastSlash < 0) {
            return path;
        }
        return path.substring(lastSlash + 1);
    }

    @Override
    public String getContentType(String path) {
        return getContentTypeFromPath(path);
    }

    @Override
    public String toString() {
        return "BinaryStorage[" + storageName + "]";
    }
}
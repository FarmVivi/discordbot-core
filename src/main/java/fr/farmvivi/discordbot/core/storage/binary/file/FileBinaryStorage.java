package fr.farmvivi.discordbot.core.storage.binary.file;

import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.storage.binary.BinaryStorageKey;
import fr.farmvivi.discordbot.core.storage.binary.AbstractBinaryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of BinaryStorage using the local file system.
 */
public class FileBinaryStorage extends AbstractBinaryStorage {
    private static final Logger logger = LoggerFactory.getLogger(FileBinaryStorage.class);

    private final File baseDirectory;

    /**
     * Creates a new file binary storage.
     *
     * @param storageName   the name of the storage
     * @param baseDirectory the base directory for storing files
     * @param eventManager  the event manager
     */
    public FileBinaryStorage(String storageName, File baseDirectory, EventManager eventManager) {
        super(storageName, eventManager);
        this.baseDirectory = baseDirectory;

        if (!baseDirectory.exists()) {
            if (!baseDirectory.mkdirs()) {
                logger.error("[{}] Failed to create base directory: {}",
                        storageName, baseDirectory.getAbsolutePath());
            }
        }
    }

    @Override
    public OutputStream getOutputStream(BinaryStorageKey key, boolean overwrite) {
        File file = resolveFile(key);

        if (!overwrite && file.exists()) {
            logger.warn("[{}] File already exists at path {} and overwrite is false",
                    storageName, key.getFullPath());
            return null;
        }

        // Create parent directories if they don't exist
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                logger.error("[{}] Failed to create parent directories for path: {}",
                        storageName, key.getFullPath());
                return null;
            }
        }

        try {
            return new FileOutputStream(file);
        } catch (IOException e) {
            logger.error("[{}] Error creating output stream for path {}: {}",
                    storageName, key.getFullPath(), e.getMessage());
            return null;
        }
    }

    @Override
    public Optional<InputStream> getInputStream(BinaryStorageKey key) {
        File file = resolveFile(key);

        if (!file.exists() || !file.isFile()) {
            logger.debug("[{}] File does not exist at path: {}", storageName, key.getFullPath());
            return Optional.empty();
        }

        try {
            return Optional.of(new FileInputStream(file));
        } catch (IOException e) {
            logger.error("[{}] Error creating input stream for path {}: {}",
                    storageName, key.getFullPath(), e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean fileExists(BinaryStorageKey key) {
        File file = resolveFile(key);
        return file.exists() && file.isFile();
    }

    @Override
    protected boolean doDeleteFile(BinaryStorageKey key) {
        File file = resolveFile(key);

        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            logger.warn("[{}] Path {} is a directory, not a file", storageName, key.getFullPath());
            return false;
        }

        if (!file.delete()) {
            logger.error("[{}] Failed to delete file at path: {}", storageName, key.getFullPath());
            return false;
        }

        // Clean up empty parent directories
        File parentDir = file.getParentFile();
        while (parentDir != null && !parentDir.equals(baseDirectory) && parentDir.isDirectory()
                && parentDir.list() != null && parentDir.list().length == 0) {
            if (!parentDir.delete()) {
                break;
            }
            parentDir = parentDir.getParentFile();
        }

        return true;
    }

    @Override
    public List<String> listFiles(BinaryStorageKey key) {
        File dir = resolveFile(key);

        if (!dir.exists() || !dir.isDirectory()) {
            return new ArrayList<>();
        }

        try (Stream<Path> stream = Files.walk(dir.toPath(), 1)) {
            return stream
                    .skip(1) // Skip the directory itself
                    .filter(path -> !Files.isDirectory(path))
                    .map(path -> {
                        // Get path relative to the base directory
                        Path relativePath = baseDirectory.toPath().relativize(path);

                        // Remove the scope part from the path to get file name relative to the scope
                        String pathStr = relativePath.toString().replace('\\', '/');
                        String scope = key.scope();

                        if (pathStr.startsWith(scope + "/")) {
                            return pathStr.substring(scope.length() + 1);
                        }

                        return pathStr;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("[{}] Error listing files in directory {}: {}",
                    storageName, key.getFullPath(), e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public long getFileSize(BinaryStorageKey key) {
        File file = resolveFile(key);

        if (!file.exists() || !file.isFile()) {
            return -1;
        }

        return file.length();
    }

    @Override
    public long getLastModifiedTime(BinaryStorageKey key) {
        File file = resolveFile(key);

        if (!file.exists()) {
            return -1;
        }

        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return attrs.lastModifiedTime().toMillis();
        } catch (IOException e) {
            logger.error("[{}] Error getting last modified time for path {}: {}",
                    storageName, key.getFullPath(), e.getMessage());
            return file.lastModified(); // Fallback to less precise method
        }
    }

    @Override
    public boolean createDirectory(BinaryStorageKey key) {
        File dir = resolveFile(key);

        if (dir.exists()) {
            return dir.isDirectory();
        }

        return dir.mkdirs();
    }

    @Override
    public boolean isDirectory(BinaryStorageKey key) {
        File file = resolveFile(key);
        return file.exists() && file.isDirectory();
    }

    @Override
    public boolean close() {
        // No resources to release
        return true;
    }

    /**
     * Resolves a storage key to a file in the local file system.
     *
     * @param key the storage key
     * @return the resolved file
     */
    private File resolveFile(BinaryStorageKey key) {
        return new File(baseDirectory, key.getFullPath());
    }
}
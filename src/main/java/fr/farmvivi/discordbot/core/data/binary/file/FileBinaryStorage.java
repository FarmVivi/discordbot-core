package fr.farmvivi.discordbot.core.data.binary.file;

import fr.farmvivi.discordbot.core.data.binary.AbstractBinaryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of BinaryStorage that uses the local file system.
 */
public class FileBinaryStorage extends AbstractBinaryStorage {
    private static final Logger logger = LoggerFactory.getLogger(FileBinaryStorage.class);

    private final File baseDirectory;

    /**
     * Creates a new file binary storage with the specified base directory.
     *
     * @param storageName   the name of the storage
     * @param baseDirectory the base directory for storing files
     */
    public FileBinaryStorage(String storageName, File baseDirectory) {
        super(storageName);
        this.baseDirectory = baseDirectory;

        if (!baseDirectory.exists()) {
            if (!baseDirectory.mkdirs()) {
                logger.error("[{}] Failed to create base directory: {}", storageName, baseDirectory.getAbsolutePath());
            }
        }
    }

    @Override
    public OutputStream getOutputStream(String path, boolean overwrite) {
        path = normalizePath(path);
        File file = new File(baseDirectory, path);

        if (!overwrite && file.exists()) {
            logger.warn("[{}] File already exists at path {} and overwrite is false", storageName, path);
            return null;
        }

        // Create parent directories if they don't exist
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                logger.error("[{}] Failed to create parent directories for path: {}", storageName, path);
                return null;
            }
        }

        try {
            return new FileOutputStream(file);
        } catch (IOException e) {
            logger.error("[{}] Error creating output stream for path {}: {}", storageName, path, e.getMessage());
            return null;
        }
    }

    @Override
    public InputStream getInputStream(String path) {
        path = normalizePath(path);
        File file = new File(baseDirectory, path);

        if (!file.exists() || !file.isFile()) {
            logger.error("[{}] File does not exist at path: {}", storageName, path);
            return null;
        }

        try {
            return new FileInputStream(file);
        } catch (IOException e) {
            logger.error("[{}] Error creating input stream for path {}: {}", storageName, path, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean fileExists(String path) {
        path = normalizePath(path);
        File file = new File(baseDirectory, path);
        return file.exists() && file.isFile();
    }

    @Override
    public boolean deleteFile(String path) {
        path = normalizePath(path);
        File file = new File(baseDirectory, path);

        if (!file.exists()) {
            logger.warn("[{}] Cannot delete non-existent file at path: {}", storageName, path);
            return false;
        }

        if (file.isDirectory()) {
            logger.warn("[{}] Path {} is a directory, not a file", storageName, path);
            return false;
        }

        if (!file.delete()) {
            logger.error("[{}] Failed to delete file at path: {}", storageName, path);
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
    public List<String> listFiles(String directory) {
        directory = normalizePath(directory);
        File dir = new File(baseDirectory, directory);

        if (!dir.exists() || !dir.isDirectory()) {
            return new ArrayList<>();
        }

        try (Stream<Path> stream = Files.walk(dir.toPath(), 1)) {
            return stream
                    .skip(1) // Skip the directory itself
                    .filter(path -> !Files.isDirectory(path))
                    .map(path -> baseDirectory.toPath().relativize(path).toString().replace('\\', '/'))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("[{}] Error listing files in directory {}: {}", storageName, directory, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public long getFileSize(String path) {
        path = normalizePath(path);
        File file = new File(baseDirectory, path);

        if (!file.exists() || !file.isFile()) {
            return -1;
        }

        return file.length();
    }

    @Override
    public long getLastModifiedTime(String path) {
        path = normalizePath(path);
        File file = new File(baseDirectory, path);

        if (!file.exists()) {
            return -1;
        }

        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return attrs.lastModifiedTime().toMillis();
        } catch (IOException e) {
            logger.error("[{}] Error getting last modified time for path {}: {}", storageName, path, e.getMessage());
            return file.lastModified(); // Fallback to less precise method
        }
    }

    @Override
    public boolean createDirectory(String path) {
        path = normalizePath(path);
        File dir = new File(baseDirectory, path);

        if (dir.exists()) {
            return dir.isDirectory();
        }

        return dir.mkdirs();
    }

    @Override
    public boolean isDirectory(String path) {
        path = normalizePath(path);
        File file = new File(baseDirectory, path);
        return file.exists() && file.isDirectory();
    }

    @Override
    public String getPublicUrl(String path, int expireIn) {
        // Local file system doesn't support public URLs by default
        return null;
    }

    /**
     * Gets the absolute file path for a given storage path.
     *
     * @param path the storage path
     * @return the absolute file path
     */
    public Path getAbsolutePath(String path) {
        path = normalizePath(path);
        return Paths.get(baseDirectory.getAbsolutePath(), path);
    }
}
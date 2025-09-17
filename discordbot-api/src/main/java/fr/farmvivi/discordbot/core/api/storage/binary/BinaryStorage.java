package fr.farmvivi.discordbot.core.api.storage.binary;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

/**
 * Core interface for binary storage operations.
 * Provides operations for storing, retrieving, and managing binary files.
 */
public interface BinaryStorage {
    /**
     * Saves a file to storage.
     *
     * @param key       the storage key
     * @param file      the file to save
     * @param overwrite whether to overwrite if a file already exists
     * @return true if the file was saved successfully
     */
    boolean saveFile(BinaryStorageKey key, File file, boolean overwrite);

    /**
     * Saves data from an input stream to storage.
     *
     * @param key         the storage key
     * @param inputStream the input stream containing the data
     * @param overwrite   whether to overwrite if a file already exists
     * @return true if the data was saved successfully
     */
    boolean saveFile(BinaryStorageKey key, InputStream inputStream, boolean overwrite);

    /**
     * Gets an output stream to write data to a file in storage.
     *
     * @param key       the storage key
     * @param overwrite whether to overwrite if a file already exists
     * @return an output stream, or null if the operation failed
     */
    OutputStream getOutputStream(BinaryStorageKey key, boolean overwrite);

    /**
     * Gets an input stream to read data from a file in storage.
     *
     * @param key the storage key
     * @return an Optional containing the input stream if the file exists
     */
    Optional<InputStream> getInputStream(BinaryStorageKey key);

    /**
     * Downloads a file from storage to a local file.
     *
     * @param key      the storage key
     * @param destFile the destination file
     * @return true if the file was downloaded successfully
     */
    boolean downloadFile(BinaryStorageKey key, File destFile);

    /**
     * Checks if a file exists in storage.
     *
     * @param key the storage key
     * @return true if the file exists
     */
    boolean fileExists(BinaryStorageKey key);

    /**
     * Deletes a file from storage.
     *
     * @param key the storage key
     * @return true if the file was deleted successfully
     */
    boolean deleteFile(BinaryStorageKey key);

    /**
     * Lists files in a directory in storage.
     *
     * @param key the directory key
     * @return a list of file paths relative to the directory
     */
    List<String> listFiles(BinaryStorageKey key);

    /**
     * Gets the size of a file in storage.
     *
     * @param key the storage key
     * @return the size of the file in bytes, or -1 if the file doesn't exist
     */
    long getFileSize(BinaryStorageKey key);

    /**
     * Gets the last modified time of a file in storage.
     *
     * @param key the storage key
     * @return the last modified time in milliseconds since epoch, or -1 if not available
     */
    long getLastModifiedTime(BinaryStorageKey key);

    /**
     * Gets the content type of a file based on its extension.
     *
     * @param key the storage key
     * @return the content type (MIME type)
     */
    String getContentType(BinaryStorageKey key);

    /**
     * Creates a directory in storage.
     *
     * @param key the directory key
     * @return true if the directory was created successfully
     */
    boolean createDirectory(BinaryStorageKey key);

    /**
     * Checks if a path in storage is a directory.
     *
     * @param key the storage key
     * @return true if the path is a directory
     */
    boolean isDirectory(BinaryStorageKey key);

    /**
     * Gets a public URL for a file, if supported by the storage backend.
     *
     * @param key      the storage key
     * @param expireIn the number of seconds until the URL expires (0 for default)
     * @return an Optional containing the URL, or empty if not supported
     */
    Optional<String> getPublicUrl(BinaryStorageKey key, int expireIn);

    /**
     * Closes the storage and releases any resources.
     *
     * @return true if closed successfully
     */
    boolean close();
}
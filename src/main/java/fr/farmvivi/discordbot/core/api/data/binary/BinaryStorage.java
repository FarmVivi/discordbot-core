package fr.farmvivi.discordbot.core.api.data.binary;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Interface for storage operations on binary files.
 * This interface is used for managing large files such as images, audio, documents, etc.
 */
public interface BinaryStorage {
    /**
     * Saves a file to the storage.
     *
     * @param path      the path where the file will be stored
     * @param file      the file to save
     * @param overwrite whether to overwrite if a file already exists at the path
     * @return true if the file was saved successfully, false otherwise
     */
    boolean saveFile(String path, File file, boolean overwrite);

    /**
     * Saves data from an input stream to the storage.
     *
     * @param path        the path where the data will be stored
     * @param inputStream the input stream containing the data
     * @param overwrite   whether to overwrite if a file already exists at the path
     * @return true if the data was saved successfully, false otherwise
     */
    boolean saveFile(String path, InputStream inputStream, boolean overwrite);

    /**
     * Gets an output stream to write data to a file in the storage.
     *
     * @param path      the path where the data will be stored
     * @param overwrite whether to overwrite if a file already exists at the path
     * @return an output stream, or null if the operation failed
     */
    OutputStream getOutputStream(String path, boolean overwrite);

    /**
     * Gets an input stream to read data from a file in the storage.
     *
     * @param path the path of the file to read
     * @return an input stream, or null if the file does not exist or the operation failed
     */
    InputStream getInputStream(String path);

    /**
     * Downloads a file from the storage to a local file.
     *
     * @param path     the path of the file in the storage
     * @param destFile the destination file
     * @return true if the file was downloaded successfully, false otherwise
     */
    boolean downloadFile(String path, File destFile);

    /**
     * Checks if a file exists in the storage.
     *
     * @param path the path of the file
     * @return true if the file exists, false otherwise
     */
    boolean fileExists(String path);

    /**
     * Deletes a file from the storage.
     *
     * @param path the path of the file to delete
     * @return true if the file was deleted successfully, false otherwise
     */
    boolean deleteFile(String path);

    /**
     * Lists files in a directory in the storage.
     *
     * @param directory the directory to list
     * @return a list of file paths, or an empty list if the directory does not exist or is empty
     */
    List<String> listFiles(String directory);

    /**
     * Gets the size of a file in the storage.
     *
     * @param path the path of the file
     * @return the size of the file in bytes, or -1 if the file does not exist or the operation failed
     */
    long getFileSize(String path);

    /**
     * Gets the last modified time of a file in the storage.
     *
     * @param path the path of the file
     * @return the last modified time in milliseconds since the epoch, or -1 if the file does not exist or the operation failed
     */
    long getLastModifiedTime(String path);

    /**
     * Gets the content type of a file, if available.
     *
     * @param path the path of the file
     * @return the content type, or null if not available
     */
    String getContentType(String path);

    /**
     * Creates a directory in the storage.
     *
     * @param path the path of the directory to create
     * @return true if the directory was created successfully, false otherwise
     */
    boolean createDirectory(String path);

    /**
     * Checks if a path in the storage is a directory.
     *
     * @param path the path to check
     * @return true if the path is a directory, false otherwise
     */
    boolean isDirectory(String path);

    /**
     * Gets the URL for public access to a file, if supported by the storage backend.
     *
     * @param path     the path of the file
     * @param expireIn the number of seconds until the URL expires (0 for never)
     * @return the URL as a string, or null if the operation is not supported or failed
     */
    String getPublicUrl(String path, int expireIn);
}
package fr.farmvivi.discordbot.core.api.storage.binary;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Plugin-specific guild binary storage context.
 */
public class PluginGuildBinaryStorage {
    private final GuildBinaryStorage storage;
    private final String namespace;

    public PluginGuildBinaryStorage(GuildBinaryStorage storage, String namespace) {
        this.storage = storage;
        this.namespace = namespace;
    }

    /**
     * Creates the namespaced path for this plugin.
     *
     * @param path the path
     * @return the namespaced path
     */
    private String namespacePath(String path) {
        return namespace + "/" + path;
    }

    /**
     * Removes the namespace prefix from a path.
     *
     * @param namespacedPath the namespaced path
     * @return the path without namespace
     */
    private String removeNamespace(String namespacedPath) {
        String prefix = namespace + "/";
        return namespacedPath.startsWith(prefix) ? namespacedPath.substring(prefix.length()) : namespacedPath;
    }

    /**
     * Saves a file to guild storage.
     *
     * @param path      the path
     * @param file      the file to save
     * @param overwrite whether to overwrite if a file already exists
     * @return true if the file was saved successfully
     */
    public boolean saveFile(String path, File file, boolean overwrite) {
        return storage.saveFile(namespacePath(path), file, overwrite);
    }

    /**
     * Saves data from an input stream to guild storage.
     *
     * @param path        the path
     * @param inputStream the input stream containing the data
     * @param overwrite   whether to overwrite if a file already exists
     * @return true if the data was saved successfully
     */
    public boolean saveFile(String path, InputStream inputStream, boolean overwrite) {
        return storage.saveFile(namespacePath(path), inputStream, overwrite);
    }

    /**
     * Gets an output stream to write data to a file in guild storage.
     *
     * @param path      the path
     * @param overwrite whether to overwrite if a file already exists
     * @return an output stream, or null if the operation failed
     */
    public OutputStream getOutputStream(String path, boolean overwrite) {
        return storage.getOutputStream(namespacePath(path), overwrite);
    }

    /**
     * Gets an input stream to read data from a file in guild storage.
     *
     * @param path the path
     * @return an Optional containing the input stream if the file exists
     */
    public Optional<InputStream> getInputStream(String path) {
        return storage.getInputStream(namespacePath(path));
    }

    /**
     * Downloads a file from guild storage to a local file.
     *
     * @param path     the path
     * @param destFile the destination file
     * @return true if the file was downloaded successfully
     */
    public boolean downloadFile(String path, File destFile) {
        return storage.downloadFile(namespacePath(path), destFile);
    }

    /**
     * Checks if a file exists in guild storage.
     *
     * @param path the path
     * @return true if the file exists
     */
    public boolean fileExists(String path) {
        return storage.fileExists(namespacePath(path));
    }

    /**
     * Deletes a file from guild storage.
     *
     * @param path the path
     * @return true if the file was deleted successfully
     */
    public boolean deleteFile(String path) {
        return storage.deleteFile(namespacePath(path));
    }

    /**
     * Lists files in a directory in guild storage.
     *
     * @param path the directory path
     * @return a list of file paths relative to the directory
     */
    public List<String> listFiles(String path) {
        List<String> files = storage.listFiles(namespacePath(path));
        return files.stream().map(this::removeNamespace).collect(Collectors.toList());
    }

    /**
     * Gets the size of a file in guild storage.
     *
     * @param path the path
     * @return the size of the file in bytes, or -1 if the file doesn't exist
     */
    public long getFileSize(String path) {
        return storage.getFileSize(namespacePath(path));
    }

    /**
     * Gets the last modified time of a file in guild storage.
     *
     * @param path the path
     * @return the last modified time in milliseconds since epoch, or -1 if not available
     */
    public long getLastModifiedTime(String path) {
        return storage.getLastModifiedTime(namespacePath(path));
    }

    /**
     * Gets the content type of a file based on its extension.
     *
     * @param path the path
     * @return the content type (MIME type)
     */
    public String getContentType(String path) {
        return storage.getContentType(namespacePath(path));
    }

    /**
     * Creates a directory in guild storage.
     *
     * @param path the directory path
     * @return true if the directory was created successfully
     */
    public boolean createDirectory(String path) {
        return storage.createDirectory(namespacePath(path));
    }

    /**
     * Checks if a path in guild storage is a directory.
     *
     * @param path the path
     * @return true if the path is a directory
     */
    public boolean isDirectory(String path) {
        return storage.isDirectory(namespacePath(path));
    }

    /**
     * Gets a public URL for a file, if supported by the storage backend.
     *
     * @param path     the path
     * @param expireIn the number of seconds until the URL expires (0 for default)
     * @return an Optional containing the URL, or empty if not supported
     */
    public Optional<String> getPublicUrl(String path, int expireIn) {
        return storage.getPublicUrl(namespacePath(path), expireIn);
    }

    /**
     * Gets the guild ID.
     *
     * @return the guild ID
     */
    public String getGuildId() {
        return storage.getGuildId();
    }
}
package fr.farmvivi.discordbot.core.api.storage.binary;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

/**
 * User-guild binary storage context.
 */
public class UserGuildBinaryStorage {
    private final BinaryStorage storage;
    private final String userId;
    private final String guildId;

    /**
     * Creates a new user-guild binary storage.
     *
     * @param storage the underlying storage
     * @param userId  the user ID
     * @param guildId the guild ID
     */
    public UserGuildBinaryStorage(BinaryStorage storage, String userId, String guildId) {
        this.storage = storage;
        this.userId = userId;
        this.guildId = guildId;
    }

    /**
     * Saves a file to user-guild storage.
     *
     * @param path      the path
     * @param file      the file to save
     * @param overwrite whether to overwrite if a file already exists
     * @return true if the file was saved successfully
     */
    public boolean saveFile(String path, File file, boolean overwrite) {
        return storage.saveFile(BinaryStorageKey.userGuild(userId, guildId, path), file, overwrite);
    }

    /**
     * Saves data from an input stream to user-guild storage.
     *
     * @param path        the path
     * @param inputStream the input stream containing the data
     * @param overwrite   whether to overwrite if a file already exists
     * @return true if the data was saved successfully
     */
    public boolean saveFile(String path, InputStream inputStream, boolean overwrite) {
        return storage.saveFile(BinaryStorageKey.userGuild(userId, guildId, path), inputStream, overwrite);
    }

    /**
     * Gets an output stream to write data to a file in user-guild storage.
     *
     * @param path      the path
     * @param overwrite whether to overwrite if a file already exists
     * @return an output stream, or null if the operation failed
     */
    public OutputStream getOutputStream(String path, boolean overwrite) {
        return storage.getOutputStream(BinaryStorageKey.userGuild(userId, guildId, path), overwrite);
    }

    /**
     * Gets an input stream to read data from a file in user-guild storage.
     *
     * @param path the path
     * @return an Optional containing the input stream if the file exists
     */
    public Optional<InputStream> getInputStream(String path) {
        return storage.getInputStream(BinaryStorageKey.userGuild(userId, guildId, path));
    }

    /**
     * Downloads a file from user-guild storage to a local file.
     *
     * @param path     the path
     * @param destFile the destination file
     * @return true if the file was downloaded successfully
     */
    public boolean downloadFile(String path, File destFile) {
        return storage.downloadFile(BinaryStorageKey.userGuild(userId, guildId, path), destFile);
    }

    /**
     * Checks if a file exists in user-guild storage.
     *
     * @param path the path
     * @return true if the file exists
     */
    public boolean fileExists(String path) {
        return storage.fileExists(BinaryStorageKey.userGuild(userId, guildId, path));
    }

    /**
     * Deletes a file from user-guild storage.
     *
     * @param path the path
     * @return true if the file was deleted successfully
     */
    public boolean deleteFile(String path) {
        return storage.deleteFile(BinaryStorageKey.userGuild(userId, guildId, path));
    }

    /**
     * Lists files in a directory in user-guild storage.
     *
     * @param path the directory path
     * @return a list of file paths relative to the directory
     */
    public List<String> listFiles(String path) {
        return storage.listFiles(BinaryStorageKey.userGuild(userId, guildId, path));
    }

    /**
     * Gets the size of a file in user-guild storage.
     *
     * @param path the path
     * @return the size of the file in bytes, or -1 if the file doesn't exist
     */
    public long getFileSize(String path) {
        return storage.getFileSize(BinaryStorageKey.userGuild(userId, guildId, path));
    }

    /**
     * Gets the last modified time of a file in user-guild storage.
     *
     * @param path the path
     * @return the last modified time in milliseconds since epoch, or -1 if not available
     */
    public long getLastModifiedTime(String path) {
        return storage.getLastModifiedTime(BinaryStorageKey.userGuild(userId, guildId, path));
    }

    /**
     * Gets the content type of a file based on its extension.
     *
     * @param path the path
     * @return the content type (MIME type)
     */
    public String getContentType(String path) {
        return storage.getContentType(BinaryStorageKey.userGuild(userId, guildId, path));
    }

    /**
     * Creates a directory in user-guild storage.
     *
     * @param path the directory path
     * @return true if the directory was created successfully
     */
    public boolean createDirectory(String path) {
        return storage.createDirectory(BinaryStorageKey.userGuild(userId, guildId, path));
    }

    /**
     * Checks if a path in user-guild storage is a directory.
     *
     * @param path the path
     * @return true if the path is a directory
     */
    public boolean isDirectory(String path) {
        return storage.isDirectory(BinaryStorageKey.userGuild(userId, guildId, path));
    }

    /**
     * Gets a public URL for a file, if supported by the storage backend.
     *
     * @param path     the path
     * @param expireIn the number of seconds until the URL expires (0 for default)
     * @return an Optional containing the URL, or empty if not supported
     */
    public Optional<String> getPublicUrl(String path, int expireIn) {
        return storage.getPublicUrl(BinaryStorageKey.userGuild(userId, guildId, path), expireIn);
    }

    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the guild ID.
     *
     * @return the guild ID
     */
    public String getGuildId() {
        return guildId;
    }
}


package fr.farmvivi.discordbot.core.api.data.binary;

/**
 * Interface for managing binary storage with different scopes.
 * This interface provides methods to access different scopes of binary storage:
 * - Global: Accessible by all users and guilds
 * - User: Specific to a user
 * - Guild: Specific to a guild
 * - User-Guild: Specific to a user in a specific guild
 */
public interface BinaryStorageProvider {
    /**
     * Gets the global storage.
     * This storage is accessible by all users and guilds.
     *
     * @return the global binary storage
     */
    BinaryStorage getGlobalStorage();

    /**
     * Gets a user-specific storage.
     * This storage is only accessible by the specified user.
     *
     * @param userId the ID of the user
     * @return the user-specific binary storage
     */
    BinaryStorage getUserStorage(String userId);

    /**
     * Gets a guild-specific storage.
     * This storage is only accessible within the specified guild.
     *
     * @param guildId the ID of the guild
     * @return the guild-specific binary storage
     */
    BinaryStorage getGuildStorage(String guildId);

    /**
     * Gets a storage specific to a user in a guild.
     * This storage is only accessible by the specified user within the specified guild.
     *
     * @param userId  the ID of the user
     * @param guildId the ID of the guild
     * @return the user-guild-specific binary storage
     */
    BinaryStorage getUserGuildStorage(String userId, String guildId);

    /**
     * Gets the type of storage backend being used.
     *
     * @return the storage type
     */
    StorageType getStorageType();

    /**
     * Closes the storage provider and releases any resources.
     *
     * @return true if closed successfully, false otherwise
     */
    boolean close();

    /**
     * Enum representing the available types of binary storage backends.
     */
    enum StorageType {
        FILE, S3
    }
}
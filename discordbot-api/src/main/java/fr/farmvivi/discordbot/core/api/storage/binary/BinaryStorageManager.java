package fr.farmvivi.discordbot.core.api.storage.binary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager class for binary storage operations.
 * Provides convenience methods for accessing different storage scopes.
 */
public class BinaryStorageManager {
    private static final Logger logger = LoggerFactory.getLogger(BinaryStorageManager.class);

    private final BinaryStorage storage;

    /**
     * Creates a new binary storage manager.
     *
     * @param storage the storage implementation
     */
    public BinaryStorageManager(BinaryStorage storage) {
        this.storage = storage;
    }

    /**
     * Gets the global binary storage.
     *
     * @return the global storage
     */
    public GlobalBinaryStorage getGlobalStorage() {
        return new GlobalBinaryStorage(storage);
    }

    /**
     * Gets the user binary storage for a specific user.
     *
     * @param userId the user ID
     * @return the user storage
     */
    public UserBinaryStorage getUserStorage(String userId) {
        return new UserBinaryStorage(storage, userId);
    }

    /**
     * Gets the guild binary storage for a specific guild.
     *
     * @param guildId the guild ID
     * @return the guild storage
     */
    public GuildBinaryStorage getGuildStorage(String guildId) {
        return new GuildBinaryStorage(storage, guildId);
    }

    /**
     * Gets the user-guild binary storage for a specific user and guild.
     *
     * @param userId  the user ID
     * @param guildId the guild ID
     * @return the user-guild storage
     */
    public UserGuildBinaryStorage getUserGuildStorage(String userId, String guildId) {
        return new UserGuildBinaryStorage(storage, userId, guildId);
    }

    /**
     * Closes the storage manager and releases resources.
     *
     * @return true if closed successfully
     */
    public boolean close() {
        return storage.close();
    }
}
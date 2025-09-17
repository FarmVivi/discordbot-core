package fr.farmvivi.discordbot.core.api.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager class that provides a unified interface to the storage system.
 * This is the main entry point for plugins to interact with data storage.
 */
public class DataStorageManager {
    private static final Logger logger = LoggerFactory.getLogger(DataStorageManager.class);

    private final DataStorage storage;

    /**
     * Creates a new data storage manager.
     *
     * @param storage the underlying storage implementation
     */
    public DataStorageManager(DataStorage storage) {
        this.storage = storage;
    }

    /**
     * Gets the global storage.
     *
     * @return the global storage
     */
    public GlobalStorage getGlobalStorage() {
        return new GlobalStorage(storage);
    }

    /**
     * Gets the user storage for a specific user.
     *
     * @param userId the user ID
     * @return the user storage
     */
    public UserStorage getUserStorage(String userId) {
        return new UserStorage(storage, userId);
    }

    /**
     * Gets the guild storage for a specific guild.
     *
     * @param guildId the guild ID
     * @return the guild storage
     */
    public GuildStorage getGuildStorage(String guildId) {
        return new GuildStorage(storage, guildId);
    }

    /**
     * Gets the user-guild storage for a specific user and guild.
     *
     * @param userId  the user ID
     * @param guildId the guild ID
     * @return the user-guild storage
     */
    public UserGuildStorage getUserGuildStorage(String userId, String guildId) {
        return new UserGuildStorage(storage, userId, guildId);
    }

    /**
     * Saves all pending changes.
     *
     * @return true if the operation was successful
     */
    public boolean saveAll() {
        return storage.save();
    }

    /**
     * Closes the storage and releases resources.
     *
     * @return true if the operation was successful
     */
    public boolean close() {
        return storage.close();
    }
}
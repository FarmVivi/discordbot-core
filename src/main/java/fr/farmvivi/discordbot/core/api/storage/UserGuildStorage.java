package fr.farmvivi.discordbot.core.api.storage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * User-guild storage context.
 */
public class UserGuildStorage {
    private final DataStorage storage;
    private final String userId;
    private final String guildId;

    /**
     * Creates a new user-guild storage.
     *
     * @param storage the underlying storage
     * @param userId  the user ID
     * @param guildId the guild ID
     */
    public UserGuildStorage(DataStorage storage, String userId, String guildId) {
        this.storage = storage;
        this.userId = userId;
        this.guildId = guildId;
    }

    /**
     * Gets a value from user-guild storage.
     *
     * @param key  the key
     * @param type the type
     * @param <T>  the return type
     * @return the value
     */
    public <T> Optional<T> get(String key, Class<T> type) {
        return storage.get(StorageKey.userGuild(userId, guildId, key), type);
    }

    /**
     * Sets a value in user-guild storage.
     *
     * @param key   the key
     * @param value the value
     * @param <T>   the value type
     * @return true if successful
     */
    public <T> boolean set(String key, T value) {
        return storage.set(StorageKey.userGuild(userId, guildId, key), value);
    }

    /**
     * Checks if a key exists in user-guild storage.
     *
     * @param key the key
     * @return true if the key exists
     */
    public boolean exists(String key) {
        return storage.exists(StorageKey.userGuild(userId, guildId, key));
    }

    /**
     * Removes a key from user-guild storage.
     *
     * @param key the key
     * @return true if successful
     */
    public boolean remove(String key) {
        return storage.remove(StorageKey.userGuild(userId, guildId, key));
    }

    /**
     * Gets all keys in user-guild storage.
     *
     * @return a set of keys
     */
    public Set<String> getKeys() {
        return storage.getKeys("user:" + userId + ":guild:" + guildId);
    }

    /**
     * Gets all values in user-guild storage.
     *
     * @return a map of keys to values
     */
    public Map<String, Object> getAll() {
        return storage.getAll("user:" + userId + ":guild:" + guildId);
    }

    /**
     * Clears all user-guild storage data.
     *
     * @return true if successful
     */
    public boolean clear() {
        return storage.clear("user:" + userId + ":guild:" + guildId);
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

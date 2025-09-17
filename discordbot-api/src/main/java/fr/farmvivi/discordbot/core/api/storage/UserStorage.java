package fr.farmvivi.discordbot.core.api.storage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * User storage context.
 */
public class UserStorage {
    private final DataStorage storage;
    private final String userId;

    /**
     * Creates a new user storage.
     *
     * @param storage the underlying storage
     * @param userId  the user ID
     */
    public UserStorage(DataStorage storage, String userId) {
        this.storage = storage;
        this.userId = userId;
    }

    /**
     * Gets a value from user storage.
     *
     * @param key  the key
     * @param type the type
     * @param <T>  the return type
     * @return the value
     */
    public <T> Optional<T> get(String key, Class<T> type) {
        return storage.get(StorageKey.user(userId, key), type);
    }

    /**
     * Sets a value in user storage.
     *
     * @param key   the key
     * @param value the value
     * @param <T>   the value type
     * @return true if successful
     */
    public <T> boolean set(String key, T value) {
        return storage.set(StorageKey.user(userId, key), value);
    }

    /**
     * Checks if a key exists in user storage.
     *
     * @param key the key
     * @return true if the key exists
     */
    public boolean exists(String key) {
        return storage.exists(StorageKey.user(userId, key));
    }

    /**
     * Removes a key from user storage.
     *
     * @param key the key
     * @return true if successful
     */
    public boolean remove(String key) {
        return storage.remove(StorageKey.user(userId, key));
    }

    /**
     * Gets all keys in user storage.
     *
     * @return a set of keys
     */
    public Set<String> getKeys() {
        return storage.getKeys("user:" + userId);
    }

    /**
     * Gets all values in user storage.
     *
     * @return a map of keys to values
     */
    public Map<String, Object> getAll() {
        return storage.getAll("user:" + userId);
    }

    /**
     * Clears all user storage data.
     *
     * @return true if successful
     */
    public boolean clear() {
        return storage.clear("user:" + userId);
    }

    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }
}

package fr.farmvivi.discordbot.core.api.storage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Global storage context.
 */
public class GlobalStorage {
    private final DataStorage storage;

    /**
     * Creates a new global storage.
     *
     * @param storage the underlying storage
     */
    public GlobalStorage(DataStorage storage) {
        this.storage = storage;
    }

    /**
     * Gets a value from global storage.
     *
     * @param key  the key
     * @param type the type
     * @param <T>  the return type
     * @return the value
     */
    public <T> Optional<T> get(String key, Class<T> type) {
        return storage.get(StorageKey.global(key), type);
    }

    /**
     * Sets a value in global storage.
     *
     * @param key   the key
     * @param value the value
     * @param <T>   the value type
     * @return true if successful
     */
    public <T> boolean set(String key, T value) {
        return storage.set(StorageKey.global(key), value);
    }

    /**
     * Checks if a key exists in global storage.
     *
     * @param key the key
     * @return true if the key exists
     */
    public boolean exists(String key) {
        return storage.exists(StorageKey.global(key));
    }

    /**
     * Removes a key from global storage.
     *
     * @param key the key
     * @return true if successful
     */
    public boolean remove(String key) {
        return storage.remove(StorageKey.global(key));
    }

    /**
     * Gets all keys in global storage.
     *
     * @return a set of keys
     */
    public Set<String> getKeys() {
        return storage.getKeys("global");
    }

    /**
     * Gets all values in global storage.
     *
     * @return a map of keys to values
     */
    public Map<String, Object> getAll() {
        return storage.getAll("global");
    }

    /**
     * Clears all global storage data.
     *
     * @return true if successful
     */
    public boolean clear() {
        return storage.clear("global");
    }
}
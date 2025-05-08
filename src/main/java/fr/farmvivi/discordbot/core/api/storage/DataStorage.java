package fr.farmvivi.discordbot.core.api.storage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Core interface for data storage operations.
 * Provides a unified set of operations for all storage types.
 */
public interface DataStorage {
    /**
     * Gets a value from storage.
     *
     * @param key  the storage key
     * @param type the class representing the return type
     * @param <T>  the return type
     * @return the value wrapped in an Optional
     */
    <T> Optional<T> get(StorageKey key, Class<T> type);

    /**
     * Sets a value in storage.
     *
     * @param key   the storage key
     * @param value the value to store
     * @param <T>   the value type
     * @return true if the operation was successful
     */
    <T> boolean set(StorageKey key, T value);

    /**
     * Checks if a key exists in storage.
     *
     * @param key the storage key
     * @return true if the key exists
     */
    boolean exists(StorageKey key);

    /**
     * Removes a key from storage.
     *
     * @param key the storage key
     * @return true if the key was removed
     */
    boolean remove(StorageKey key);

    /**
     * Gets all keys with a specific scope.
     *
     * @param scope the scope prefix
     * @return a set of keys
     */
    Set<String> getKeys(String scope);

    /**
     * Gets all key-value pairs with a specific scope.
     *
     * @param scope the scope prefix
     * @return a map of keys to values
     */
    Map<String, Object> getAll(String scope);

    /**
     * Clears all data with a specific scope.
     *
     * @param scope the scope prefix
     * @return true if the operation was successful
     */
    boolean clear(String scope);

    /**
     * Saves any pending changes to persistent storage.
     *
     * @return true if the operation was successful
     */
    boolean save();

    /**
     * Closes the storage and releases any resources.
     *
     * @return true if the operation was successful
     */
    boolean close();
}
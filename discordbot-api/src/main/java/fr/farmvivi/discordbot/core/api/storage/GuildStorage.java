package fr.farmvivi.discordbot.core.api.storage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Guild storage context.
 */
public class GuildStorage {
    private final DataStorage storage;
    private final String guildId;

    /**
     * Creates a new guild storage.
     *
     * @param storage the underlying storage
     * @param guildId the guild ID
     */
    public GuildStorage(DataStorage storage, String guildId) {
        this.storage = storage;
        this.guildId = guildId;
    }

    /**
     * Gets a value from guild storage.
     *
     * @param key  the key
     * @param type the type
     * @param <T>  the return type
     * @return the value
     */
    public <T> Optional<T> get(String key, Class<T> type) {
        return storage.get(StorageKey.guild(guildId, key), type);
    }

    /**
     * Sets a value in guild storage.
     *
     * @param key   the key
     * @param value the value
     * @param <T>   the value type
     * @return true if successful
     */
    public <T> boolean set(String key, T value) {
        return storage.set(StorageKey.guild(guildId, key), value);
    }

    /**
     * Checks if a key exists in guild storage.
     *
     * @param key the key
     * @return true if the key exists
     */
    public boolean exists(String key) {
        return storage.exists(StorageKey.guild(guildId, key));
    }

    /**
     * Removes a key from guild storage.
     *
     * @param key the key
     * @return true if successful
     */
    public boolean remove(String key) {
        return storage.remove(StorageKey.guild(guildId, key));
    }

    /**
     * Gets all keys in guild storage.
     *
     * @return a set of keys
     */
    public Set<String> getKeys() {
        return storage.getKeys("guild:" + guildId);
    }

    /**
     * Gets all values in guild storage.
     *
     * @return a map of keys to values
     */
    public Map<String, Object> getAll() {
        return storage.getAll("guild:" + guildId);
    }

    /**
     * Clears all guild storage data.
     *
     * @return true if successful
     */
    public boolean clear() {
        return storage.clear("guild:" + guildId);
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

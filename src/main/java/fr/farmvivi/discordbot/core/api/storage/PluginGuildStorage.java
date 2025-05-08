package fr.farmvivi.discordbot.core.api.storage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Plugin-specific guild storage context.
 */
public class PluginGuildStorage {
    private final GuildStorage storage;
    private final String namespace;

    public PluginGuildStorage(GuildStorage storage, String namespace) {
        this.storage = storage;
        this.namespace = namespace;
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
        return storage.get(namespace + "." + key, type);
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
        return storage.set(namespace + "." + key, value);
    }

    /**
     * Checks if a key exists in guild storage.
     *
     * @param key the key
     * @return true if the key exists
     */
    public boolean exists(String key) {
        return storage.exists(namespace + "." + key);
    }

    /**
     * Removes a key from guild storage.
     *
     * @param key the key
     * @return true if successful
     */
    public boolean remove(String key) {
        return storage.remove(namespace + "." + key);
    }

    /**
     * Gets all keys in guild storage for this plugin.
     *
     * @return a set of keys without the namespace prefix
     */
    public Set<String> getKeys() {
        return storage.getKeys().stream()
                .filter(key -> key.startsWith(namespace + "."))
                .map(key -> key.substring(namespace.length() + 1))
                .collect(Collectors.toSet());
    }

    /**
     * Gets all values in guild storage for this plugin.
     *
     * @return a map of keys to values without the namespace prefix
     */
    public Map<String, Object> getAll() {
        String prefix = namespace + ".";
        return storage.getAll().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().substring(prefix.length()),
                        Map.Entry::getValue));
    }

    /**
     * Clears all guild storage data for this plugin.
     *
     * @return true if successful
     */
    public boolean clear() {
        boolean success = true;
        for (String key : getKeys()) {
            if (!remove(key)) {
                success = false;
            }
        }
        return success;
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
package fr.farmvivi.discordbot.core.api.config;

import java.util.List;
import java.util.Map;

/**
 * Interface for plugin configuration.
 */
public interface Configuration {
    /**
     * Gets a string value from the configuration.
     *
     * @param key the key
     * @return the value
     * @throws ConfigurationException if the key doesn't exist or the value is not a string
     */
    String getString(String key) throws ConfigurationException;

    /**
     * Gets a string value from the configuration with a default.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value, or the default if the key doesn't exist
     */
    String getString(String key, String defaultValue);

    /**
     * Gets an integer value from the configuration.
     *
     * @param key the key
     * @return the value
     * @throws ConfigurationException if the key doesn't exist or the value is not an integer
     */
    int getInt(String key) throws ConfigurationException;

    /**
     * Gets an integer value from the configuration with a default.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value, or the default if the key doesn't exist
     */
    int getInt(String key, int defaultValue);

    /**
     * Gets a boolean value from the configuration.
     *
     * @param key the key
     * @return the value
     * @throws ConfigurationException if the key doesn't exist or the value is not a boolean
     */
    boolean getBoolean(String key) throws ConfigurationException;

    /**
     * Gets a boolean value from the configuration with a default.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value, or the default if the key doesn't exist
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Gets a list of strings from the configuration.
     *
     * @param key the key
     * @return the value
     * @throws ConfigurationException if the key doesn't exist or the value is not a list
     */
    List<String> getStringList(String key) throws ConfigurationException;

    /**
     * Gets a list of strings from the configuration with a default.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the value, or the default if the key doesn't exist
     */
    List<String> getStringList(String key, List<String> defaultValue);

    /**
     * Sets a value in the configuration.
     *
     * @param key   the key
     * @param value the value
     */
    void set(String key, Object value);

    /**
     * Checks if the configuration contains a key.
     *
     * @param key the key
     * @return true if the key exists
     */
    boolean contains(String key);

    /**
     * Gets all keys in the configuration.
     *
     * @return a list of all keys
     */
    List<String> getKeys();

    /**
     * Gets all values in the configuration.
     *
     * @return a map of all keys to values
     */
    Map<String, Object> getValues();

    /**
     * Saves the configuration to disk.
     *
     * @throws ConfigurationException if there's an error saving
     */
    void save() throws ConfigurationException;

    /**
     * Reloads the configuration from disk.
     *
     * @throws ConfigurationException if there's an error loading
     */
    void reload() throws ConfigurationException;
}

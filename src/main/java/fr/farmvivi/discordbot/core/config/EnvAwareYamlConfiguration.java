package fr.farmvivi.discordbot.core.config;

import fr.farmvivi.discordbot.core.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.util.EnvironmentUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class EnvAwareYamlConfiguration extends YamlConfiguration {
    public EnvAwareYamlConfiguration(File configFile) throws ConfigurationException {
        super(configFile);
    }

    /**
     * Gets a value from environment variable first, then from the YAML configuration.
     *
     * @param key the key
     * @return the value
     * @throws ConfigurationException if not found in either
     */
    public String getStringWithEnvFallback(String key) throws ConfigurationException {
        String envValue = EnvironmentUtils.getEnv(key);
        if (envValue != null) {
            return envValue;
        }
        return getString(key);
    }

    /**
     * Gets a list of values from environment variable first, then from the YAML configuration.
     *
     * @param key the key
     * @return the values
     * @throws ConfigurationException if not found in either
     */
    public List<String> getStringListWithEnvFallback(String key) throws ConfigurationException {
        String[] envValues = EnvironmentUtils.getEnvValues(key);
        if (envValues.length > 0) {
            return Arrays.asList(envValues);
        }
        return getStringList(key);
    }

    // Add similar methods for other types (int, boolean, etc.) as needed
}
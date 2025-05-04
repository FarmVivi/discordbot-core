package fr.farmvivi.discordbot.core.config;

import fr.farmvivi.discordbot.core.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.util.EnvironmentUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration YAML avec support des variables d'environnement.
 * Vérifie d'abord les variables d'environnement avant d'utiliser le fichier YAML.
 */
public class EnvAwareYamlConfiguration extends YamlConfiguration {

    /**
     * Crée une nouvelle configuration YAML avec support des variables d'environnement.
     *
     * @param configFile le fichier de configuration
     * @throws ConfigurationException en cas d'erreur lors du chargement du fichier
     */
    public EnvAwareYamlConfiguration(File configFile) throws ConfigurationException {
        super(configFile);
    }

    @Override
    public String getString(String key) throws ConfigurationException {
        String envValue = EnvironmentUtils.getEnv(key);
        if (envValue != null) {
            return envValue;
        }
        return super.getString(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        String envValue = EnvironmentUtils.getEnv(key);
        if (envValue != null) {
            return envValue;
        }
        return super.getString(key, defaultValue);
    }

    @Override
    public int getInt(String key) throws ConfigurationException {
        String envValue = EnvironmentUtils.getEnv(key);
        if (envValue != null) {
            try {
                return Integer.parseInt(envValue);
            } catch (NumberFormatException e) {
                throw new ConfigurationException("La valeur de la variable d'environnement pour la clé '" + key + "' n'est pas un entier: " + envValue, e);
            }
        }
        return super.getInt(key);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String envValue = EnvironmentUtils.getEnv(key);
        if (envValue != null) {
            try {
                return Integer.parseInt(envValue);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return super.getInt(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key) throws ConfigurationException {
        String envValue = EnvironmentUtils.getEnv(key);
        if (envValue != null) {
            return Boolean.parseBoolean(envValue);
        }
        return super.getBoolean(key);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        String envValue = EnvironmentUtils.getEnv(key);
        if (envValue != null) {
            return Boolean.parseBoolean(envValue);
        }
        return super.getBoolean(key, defaultValue);
    }

    @Override
    public List<String> getStringList(String key) throws ConfigurationException {
        String[] envValues = EnvironmentUtils.getEnvValues(key);
        if (envValues.length > 0) {
            return Arrays.asList(envValues);
        }
        return super.getStringList(key);
    }

    @Override
    public List<String> getStringList(String key, List<String> defaultValue) {
        String[] envValues = EnvironmentUtils.getEnvValues(key);
        if (envValues.length > 0) {
            return Arrays.asList(envValues);
        }
        return super.getStringList(key, defaultValue);
    }
}
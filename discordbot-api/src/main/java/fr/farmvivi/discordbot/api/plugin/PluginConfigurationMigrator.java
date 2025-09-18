package fr.farmvivi.discordbot.api.plugin;

import fr.farmvivi.discordbot.api.config.Configuration;
import fr.farmvivi.discordbot.api.config.ConfigurationException;

/**
 * Interface for classes that handle plugin configuration migration.
 * This allows plugins to define a separate migration class instead of 
 * implementing migration logic directly in the main plugin class.
 */
public interface PluginConfigurationMigrator {
    
    /**
     * Gets the current configuration version expected by this migrator.
     *
     * @return the expected configuration version
     */
    int getExpectedConfigVersion();
    
    /**
     * Migrates the plugin's configuration from one version to another.
     *
     * @param config the configuration to migrate
     * @param fromVersion the current version of the configuration
     * @param toVersion the target version
     * @throws ConfigurationException if migration fails
     */
    void migrateConfiguration(Configuration config, int fromVersion, int toVersion) throws ConfigurationException;
    
    /**
     * Called after configuration is loaded and potentially migrated.
     * Can be used to validate the configuration.
     *
     * @param config the loaded configuration
     * @throws ConfigurationException if configuration is invalid
     */
    default void validateConfiguration(Configuration config) throws ConfigurationException {
        // Default implementation does nothing
    }
}
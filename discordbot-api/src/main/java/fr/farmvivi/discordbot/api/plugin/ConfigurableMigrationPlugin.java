package fr.farmvivi.discordbot.api.plugin;

import fr.farmvivi.discordbot.api.config.Configuration;
import fr.farmvivi.discordbot.api.config.ConfigurationException;

/**
 * Interface for plugins that need to handle configuration migration.
 */
public interface ConfigurableMigrationPlugin {

    /**
     * Gets the current configuration version expected by this plugin.
     *
     * @return the expected configuration version
     */
    int getExpectedConfigVersion();

    /**
     * Migrates the plugin's configuration from one version to another.
     *
     * @param config      the configuration to migrate
     * @param fromVersion the current version of the configuration
     * @param toVersion   the target version
     * @throws ConfigurationException if migration fails
     */
    void migrateConfiguration(Configuration config, int fromVersion, int toVersion) throws ConfigurationException;

    /**
     * Called after configuration is loaded and potentially migrated.
     * Plugins can validate their configuration here.
     *
     * @param config the loaded configuration
     * @throws ConfigurationException if configuration is invalid
     */
    default void validateConfiguration(Configuration config) throws ConfigurationException {
        // Default implementation does nothing
    }
}
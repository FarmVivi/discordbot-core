package fr.farmvivi.discordbot.core.config;

import fr.farmvivi.discordbot.api.config.Configuration;
import fr.farmvivi.discordbot.api.config.ConfigurationException;

/**
 * Interface for configuration migrators that handle version updates.
 */
public interface ConfigurationMigrator {
    
    /**
     * Gets the version that this migrator upgrades from.
     *
     * @return the source version
     */
    String getFromVersion();
    
    /**
     * Gets the version that this migrator upgrades to.
     *
     * @return the target version
     */
    String getToVersion();
    
    /**
     * Performs the migration from the source version to the target version.
     *
     * @param configuration the configuration to migrate
     * @throws ConfigurationException if the migration fails
     */
    void migrate(Configuration configuration) throws ConfigurationException;
    
    /**
     * Validates that the configuration can be migrated safely.
     *
     * @param configuration the configuration to validate
     * @return true if migration is safe, false otherwise
     */
    default boolean canMigrate(Configuration configuration) {
        return true;
    }
    
    /**
     * Gets a description of what this migration does.
     *
     * @return migration description
     */
    default String getDescription() {
        return "Migration from " + getFromVersion() + " to " + getToVersion();
    }
}
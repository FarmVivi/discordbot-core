package fr.farmvivi.discordbot.core.config;

import fr.farmvivi.discordbot.api.config.Configuration;
import fr.farmvivi.discordbot.api.config.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for configuration migrations and versioning.
 */
public class ConfigurationMigrationManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationMigrationManager.class);
    
    private static final String CONFIG_VERSION_KEY = "config_version";
    private static final String DEFAULT_VERSION = "1.0.0";
    
    private final Map<String, List<ConfigurationMigrator>> migrators = new ConcurrentHashMap<>();
    
    /**
     * Registers a configuration migrator.
     *
     * @param migrator the migrator to register
     */
    public void registerMigrator(ConfigurationMigrator migrator) {
        String fromVersion = migrator.getFromVersion();
        migrators.computeIfAbsent(fromVersion, k -> new ArrayList<>()).add(migrator);
        logger.debug("Registered migrator: {} -> {}", fromVersion, migrator.getToVersion());
    }
    
    /**
     * Checks if a configuration needs migration.
     *
     * @param configuration the configuration to check
     * @param targetVersion the target version
     * @return true if migration is needed, false otherwise
     */
    public boolean needsMigration(Configuration configuration, String targetVersion) {
        String currentVersion = getCurrentVersion(configuration);
        return VersionUtils.compareVersions(currentVersion, targetVersion) < 0;
    }
    
    /**
     * Migrates a configuration to the target version.
     *
     * @param configuration the configuration to migrate
     * @param targetVersion the target version
     * @param backupEnabled whether to create a backup before migration
     * @throws ConfigurationException if migration fails
     */
    public void migrateConfiguration(Configuration configuration, String targetVersion, boolean backupEnabled) 
            throws ConfigurationException {
        String currentVersion = getCurrentVersion(configuration);
        
        if (!needsMigration(configuration, targetVersion)) {
            logger.debug("Configuration is already up to date (current: {}, target: {})", 
                        currentVersion, targetVersion);
            return;
        }
        
        logger.info("Starting configuration migration from {} to {}", currentVersion, targetVersion);
        
        // Create backup if requested and configuration is file-based
        if (backupEnabled && configuration instanceof YamlConfiguration yamlConfig) {
            createBackup(yamlConfig);
        }
        
        try {
            // Find migration path
            List<ConfigurationMigrator> migrationPath = findMigrationPath(currentVersion, targetVersion);
            if (migrationPath.isEmpty()) {
                throw new ConfigurationException(
                    "No migration path found from " + currentVersion + " to " + targetVersion);
            }
            
            // Execute migrations in sequence
            for (ConfigurationMigrator migrator : migrationPath) {
                logger.info("Applying migration: {}", migrator.getDescription());
                
                if (!migrator.canMigrate(configuration)) {
                    throw new ConfigurationException(
                        "Migration validation failed: " + migrator.getDescription());
                }
                
                migrator.migrate(configuration);
                
                // Update version after each successful migration
                configuration.set(CONFIG_VERSION_KEY, migrator.getToVersion());
                configuration.save();
                
                logger.debug("Successfully applied migration to version {}", migrator.getToVersion());
            }
            
            logger.info("Configuration migration completed successfully to version {}", targetVersion);
            
        } catch (Exception e) {
            logger.error("Configuration migration failed: {}", e.getMessage(), e);
            throw new ConfigurationException("Migration failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets the current version of a configuration.
     *
     * @param configuration the configuration
     * @return the current version, or default version if not set
     */
    public String getCurrentVersion(Configuration configuration) {
        return configuration.getString(CONFIG_VERSION_KEY, DEFAULT_VERSION);
    }
    
    /**
     * Sets the version of a configuration.
     *
     * @param configuration the configuration
     * @param version the version to set
     */
    public void setVersion(Configuration configuration, String version) {
        configuration.set(CONFIG_VERSION_KEY, version);
    }
    
    /**
     * Finds a migration path from source to target version.
     *
     * @param fromVersion the source version
     * @param toVersion the target version
     * @return list of migrators to apply in order
     */
    private List<ConfigurationMigrator> findMigrationPath(String fromVersion, String toVersion) {
        List<ConfigurationMigrator> path = new ArrayList<>();
        String currentVersion = fromVersion;
        
        // Simple linear path finding - could be enhanced with graph algorithms if needed
        int maxIterations = 100; // Prevent infinite loops
        int iterations = 0;
        
        while (!currentVersion.equals(toVersion) && iterations < maxIterations) {
            List<ConfigurationMigrator> availableMigrators = migrators.get(currentVersion);
            if (availableMigrators == null || availableMigrators.isEmpty()) {
                // No migrators available for this version
                break;
            }
            
            // Find the migrator that gets us closest to the target
            ConfigurationMigrator bestMigrator = null;
            int bestScore = Integer.MAX_VALUE;
            
            for (ConfigurationMigrator migrator : availableMigrators) {
                String migratorToVersion = migrator.getToVersion();
                int comparison = VersionUtils.compareVersions(migratorToVersion, toVersion);
                
                if (comparison <= 0) { // Migrator version is <= target version
                    int score = Math.abs(comparison);
                    if (score < bestScore) {
                        bestScore = score;
                        bestMigrator = migrator;
                    }
                }
            }
            
            if (bestMigrator == null) {
                // No suitable migrator found
                break;
            }
            
            path.add(bestMigrator);
            currentVersion = bestMigrator.getToVersion();
            iterations++;
        }
        
        return path;
    }
    
    /**
     * Creates a backup of the configuration file.
     *
     * @param yamlConfig the YAML configuration to backup
     */
    private void createBackup(YamlConfiguration yamlConfig) {
        File configFile = yamlConfig.getConfigFile();
        if (configFile == null || !configFile.exists()) {
            return;
        }
        
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupName = configFile.getName() + ".backup." + timestamp;
            File backupFile = new File(configFile.getParent(), backupName);
            
            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Created configuration backup: {}", backupFile.getAbsolutePath());
            
        } catch (IOException e) {
            logger.warn("Failed to create configuration backup: {}", e.getMessage());
        }
    }
}
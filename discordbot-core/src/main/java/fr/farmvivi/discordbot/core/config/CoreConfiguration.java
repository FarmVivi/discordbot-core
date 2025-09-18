package fr.farmvivi.discordbot.core.config;

import fr.farmvivi.discordbot.api.config.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Core configuration with versioning and migration support.
 */
public class CoreConfiguration extends YamlConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(CoreConfiguration.class);
    
    private static final String DEFAULT_CONFIG_RESOURCE = "default-core-config.yml";
    private static final String CONFIG_VERSION_KEY = "config_version";
    private static final String CURRENT_VERSION = "2.3.27";
    
    private final ConfigurationMigrationManager migrationManager;
    
    /**
     * Creates a new core configuration.
     *
     * @param configFile the configuration file
     * @param migrationManager the migration manager
     */
    public CoreConfiguration(File configFile, ConfigurationMigrationManager migrationManager) {
        super();
        this.migrationManager = migrationManager;
        setConfigFile(configFile);
        
        initialize();
    }
    
    /**
     * Initializes the core configuration.
     */
    private void initialize() {
        File configFile = getConfigFile();
        
        try {
            // Copy default configuration if it doesn't exist
            if (!configFile.exists()) {
                copyDefaultConfiguration(configFile);
            }
            
            // Load configuration
            reload();
            
            // Perform migration if needed
            if (needsMigration()) {
                performMigration();
            } else {
                // Ensure version is set
                ensureVersionSet();
            }
            
        } catch (ConfigurationException e) {
            logger.error("Failed to initialize core configuration: {}", e.getMessage(), e);
            createMinimalConfiguration();
        }
    }
    
    /**
     * Copies the default core configuration.
     *
     * @param configFile the target configuration file
     */
    private void copyDefaultConfiguration(File configFile) {
        try (InputStream defaultConfigStream = getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIG_RESOURCE)) {
            if (defaultConfigStream != null) {
                // Ensure parent directory exists
                File parentDir = configFile.getParentFile();
                if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                    logger.warn("Failed to create configuration directory: {}", parentDir.getAbsolutePath());
                }
                
                Files.copy(defaultConfigStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.info("Created default core configuration: {}", configFile.getAbsolutePath());
            } else {
                logger.warn("Default core configuration not found in resources: {}", DEFAULT_CONFIG_RESOURCE);
                createMinimalConfiguration();
            }
        } catch (IOException e) {
            logger.warn("Failed to copy default core configuration: {}", e.getMessage());
            createMinimalConfiguration();
        }
    }
    
    /**
     * Checks if migration is needed.
     *
     * @return true if migration is needed
     */
    private boolean needsMigration() {
        if (migrationManager == null) {
            return false;
        }
        
        return migrationManager.needsMigration(this, CURRENT_VERSION);
    }
    
    /**
     * Performs configuration migration.
     */
    private void performMigration() {
        if (migrationManager == null) {
            logger.warn("Migration needed for core configuration but no migration manager available");
            return;
        }
        
        try {
            String currentVersion = migrationManager.getCurrentVersion(this);
            logger.info("Migrating core configuration from version {} to {}", currentVersion, CURRENT_VERSION);
            
            migrationManager.migrateConfiguration(this, CURRENT_VERSION, true);
            
            logger.info("Successfully migrated core configuration to version {}", CURRENT_VERSION);
            
        } catch (ConfigurationException e) {
            logger.error("Failed to migrate core configuration: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Ensures that the configuration version is set.
     */
    private void ensureVersionSet() {
        if (!contains(CONFIG_VERSION_KEY)) {
            set(CONFIG_VERSION_KEY, CURRENT_VERSION);
            try {
                save();
                logger.debug("Set core configuration version to {}", CURRENT_VERSION);
            } catch (ConfigurationException e) {
                logger.warn("Failed to save core configuration version: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Creates a minimal core configuration.
     */
    private void createMinimalConfiguration() {
        try {
            values.clear();
            
            // Set version
            set(CONFIG_VERSION_KEY, CURRENT_VERSION);
            
            // Basic Discord configuration
            set("discord.token", "YOUR_BOT_TOKEN");
            set("discord.activity.type", "PLAYING");
            set("discord.activity.text", "with plugins!");
            set("discord.prefix", "!");
            set("discord.enable_slash_commands", true);
            
            // Basic language configuration
            set("language.default", "en-US");
            
            // Basic command configuration
            set("commands.default-prefix", "!");
            set("commands.cooldown", 3);
            
            // Basic database configuration
            set("database.type", "file");
            set("database.file.path", "data/database.db");
            
            save();
            
            logger.info("Created minimal core configuration with version {}", CURRENT_VERSION);
            
        } catch (ConfigurationException e) {
            logger.error("Failed to create minimal core configuration: {}", e.getMessage());
        }
    }
    
    /**
     * Gets the current configuration version.
     *
     * @return the configuration version
     */
    public String getConfigurationVersion() {
        return getString(CONFIG_VERSION_KEY, "1.0.0");
    }
    
    /**
     * Gets the target configuration version.
     *
     * @return the target version
     */
    public String getTargetVersion() {
        return CURRENT_VERSION;
    }
    
    /**
     * Validates the core configuration.
     *
     * @return true if the configuration is valid
     */
    public boolean validateConfiguration() {
        try {
            // Check required fields
            String token = getString("discord.token", "");
            if (token.isEmpty() || "YOUR_BOT_TOKEN".equals(token)) {
                logger.warn("Discord token is not configured properly");
                return false;
            }
            
            // Check database configuration
            String dbType = getString("database.type", "file");
            if ("file".equals(dbType)) {
                String dbPath = getString("database.file.path", "");
                if (dbPath.isEmpty()) {
                    logger.warn("Database file path is not configured");
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            logger.warn("Core configuration validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public void save() throws ConfigurationException {
        // Ensure version is always set before saving
        if (!contains(CONFIG_VERSION_KEY)) {
            set(CONFIG_VERSION_KEY, CURRENT_VERSION);
        }
        
        super.save();
        
        // Validate after saving
        if (!validateConfiguration()) {
            logger.warn("Core configuration validation failed after save");
        }
    }
}
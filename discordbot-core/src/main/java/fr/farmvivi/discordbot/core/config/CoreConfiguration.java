package fr.farmvivi.discordbot.core.config;

import fr.farmvivi.discordbot.api.config.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Core configuration manager with versioning and migration support.
 */
public class CoreConfiguration extends EnvAwareYamlConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(CoreConfiguration.class);
    private static final String CONFIG_VERSION_KEY = "config_version";
    private static final int CURRENT_CONFIG_VERSION = 1;

    /**
     * Creates a new core configuration.
     *
     * @param configFile the configuration file
     * @throws ConfigurationException if there's an error loading the configuration
     */
    public CoreConfiguration(File configFile) throws ConfigurationException {
        super(configFile);
        
        // Create default config if it doesn't exist
        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        }
        
        // Handle versioning and migration
        handleConfigVersioning();
    }

    /**
     * Creates the default configuration file with current version.
     *
     * @param configFile the configuration file to create
     * @throws ConfigurationException if creation fails
     */
    private void createDefaultConfig(File configFile) throws ConfigurationException {
        try {
            String defaultConfig = generateDefaultConfigContent();
            Files.writeString(Path.of(configFile.getAbsolutePath()), defaultConfig);
            logger.info("Created default core configuration at {}", configFile.getAbsolutePath());
            
            // Reload to parse the created configuration
            reload();
        } catch (IOException e) {
            throw new ConfigurationException("Failed to create default configuration", e);
        }
    }
    
    /**
     * Generates the default configuration content.
     *
     * @return the default configuration as a string
     */
    private String generateDefaultConfigContent() {
        return "# Discord Bot Core Configuration\n" +
               "# Configuration version (automatically managed)\n" +
               "config_version: " + CURRENT_CONFIG_VERSION + "\n\n" +
               
               "# Discord Bot Configuration\n" +
               "discord:\n" +
               "  # Bot token from Discord Developer Portal\n" +
               "  token: \"YOUR_BOT_TOKEN\"\n" +
               "  \n" +
               "  # Bot activity settings\n" +
               "  activity:\n" +
               "    type: \"PLAYING\"  # PLAYING, LISTENING, WATCHING, COMPETING\n" +
               "    text: \"with plugins!\"\n" +
               "  \n" +
               "  # Default command prefix for text commands\n" +
               "  prefix: \"!\"\n" +
               "  \n" +
               "  # Enable slash commands\n" +
               "  enable_slash_commands: true\n\n" +
               
               "# Language settings\n" +
               "language:\n" +
               "  # Default language for the bot\n" +
               "  default: \"en-US\"\n" +
               "  \n" +
               "  # Supported languages\n" +
               "  supported:\n" +
               "    - \"en-US\"\n" +
               "    - \"fr-FR\"\n" +
               "    - \"es-ES\"\n" +
               "    - \"de-DE\"\n\n" +
               
               "# Command system settings\n" +
               "commands:\n" +
               "  default-prefix: \"!\"  # Default prefix for text commands\n" +
               "  cooldown: 3  # Global default cooldown in seconds\n" +
               "  system:\n" +
               "    help: true      # Enable/disable help command\n" +
               "    version: true   # Enable/disable version command\n" +
               "    shutdown: true  # Enable/disable shutdown command\n\n" +
               
               "# Database Configuration\n" +
               "database:\n" +
               "  # Database type: file, mysql, postgresql\n" +
               "  type: \"file\"\n" +
               "  \n" +
               "  # File database settings\n" +
               "  file:\n" +
               "    path: \"data/database.db\"\n" +
               "  \n" +
               "  # MySQL settings (when type: mysql)\n" +
               "  mysql:\n" +
               "    host: \"${DB_HOST:localhost}\"\n" +
               "    port: \"${DB_PORT:3306}\"\n" +
               "    database: \"${DB_NAME:discordbot}\"\n" +
               "    username: \"${DB_USER:root}\"\n" +
               "    password: \"${DB_PASSWORD:}\"\n\n" +
               
               "# Data storage settings\n" +
               "data:\n" +
               "  storage:\n" +
               "    type: \"FILE\"  # Options: FILE, DB\n" +
               "    db:\n" +
               "      url: \"jdbc:mysql://localhost:3306/discordbot\"\n" +
               "      username: \"username\"\n" +
               "      password: \"password\"\n\n" +
               
               "# Storage Configuration\n" +
               "storage:\n" +
               "  # Binary storage backend: file, s3\n" +
               "  binary_storage: \"file\"\n" +
               "  \n" +
               "  # File storage settings\n" +
               "  file:\n" +
               "    base_path: \"data/files/\"\n" +
               "  \n" +
               "  # S3 storage settings (when binary_storage: s3)\n" +
               "  s3:\n" +
               "    bucket: \"${S3_BUCKET}\"\n" +
               "    region: \"${S3_REGION:us-east-1}\"\n" +
               "    access_key: \"${S3_ACCESS_KEY}\"\n" +
               "    secret_key: \"${S3_SECRET_KEY}\"\n" +
               "    endpoint: \"https://s3.amazonaws.com\"  # Optional, for S3-compatible services\n" +
               "    prefix: \"discordbot\"  # Optional, folder prefix in bucket\n\n" +
               
               "# Binary storage settings for large files\n" +
               "binary:\n" +
               "  storage:\n" +
               "    type: \"FILE\"  # Options: FILE, S3\n" +
               "    file:\n" +
               "      folder: \"binary\"\n" +
               "    s3:\n" +
               "      bucket: \"your-bucket-name\"\n" +
               "      region: \"eu-west-3\"\n" +
               "      access_key: \"your-access-key\"\n" +
               "      secret_key: \"your-secret-key\"\n" +
               "      endpoint: \"https://s3.amazonaws.com\"  # Optional, for S3-compatible services\n" +
               "      prefix: \"discordbot\"  # Optional, folder prefix in bucket\n\n" +
               
               "# Logging Configuration\n" +
               "logging:\n" +
               "  # Log level: trace, debug, info, warn, error\n" +
               "  level: \"info\"\n" +
               "  \n" +
               "  # Log to file\n" +
               "  file:\n" +
               "    enabled: true\n" +
               "    path: \"logs/bot.log\"\n" +
               "    max_size: \"10MB\"\n" +
               "    max_files: 10\n" +
               "  \n" +
               "  # Log to console\n" +
               "  console:\n" +
               "    enabled: true\n" +
               "    colored: true\n\n" +
               
               "# Plugin configurations\n" +
               "plugins:\n" +
               "  # Example plugin configurations would go here\n" +
               "  # Each plugin can override these defaults\n";
    }

    /**
     * Handles configuration versioning and migration.
     */
    private void handleConfigVersioning() {
        int currentVersion = getInt(CONFIG_VERSION_KEY, 0);
        
        if (currentVersion == 0) {
            // Old configuration without version
            logger.info("Adding version {} to core configuration", CURRENT_CONFIG_VERSION);
            set(CONFIG_VERSION_KEY, CURRENT_CONFIG_VERSION);
            try {
                save();
            } catch (ConfigurationException e) {
                logger.warn("Failed to save version to core configuration: {}", e.getMessage());
            }
        } else if (currentVersion < CURRENT_CONFIG_VERSION) {
            // Configuration needs migration
            logger.info("Migrating core configuration from version {} to {}", 
                       currentVersion, CURRENT_CONFIG_VERSION);
            migrateConfiguration(currentVersion, CURRENT_CONFIG_VERSION);
        } else if (currentVersion > CURRENT_CONFIG_VERSION) {
            // Configuration is from a newer version
            logger.warn("Core configuration version {} is newer than expected {}. " +
                       "This may cause compatibility issues.", 
                       currentVersion, CURRENT_CONFIG_VERSION);
        }
    }
    
    /**
     * Migrates configuration from one version to another.
     *
     * @param fromVersion the current version
     * @param toVersion the target version
     */
    private void migrateConfiguration(int fromVersion, int toVersion) {
        // Create backup before migration
        createBackup();
        
        // Apply migrations step by step
        for (int version = fromVersion; version < toVersion; version++) {
            try {
                applyMigration(version, version + 1);
                logger.debug("Applied core configuration migration {} -> {}", version, version + 1);
            } catch (Exception e) {
                logger.error("Failed to apply core configuration migration {} -> {}: {}", 
                           version, version + 1, e.getMessage());
                return; // Stop migration on error
            }
        }
        
        // Update version and save
        set(CONFIG_VERSION_KEY, toVersion);
        try {
            save();
            logger.info("Successfully migrated core configuration to version {}", toVersion);
        } catch (ConfigurationException e) {
            logger.error("Failed to save migrated core configuration: {}", e.getMessage());
        }
    }
    
    /**
     * Creates a backup of the current configuration.
     */
    private void createBackup() {
        if (getConfigFile() == null || !getConfigFile().exists()) {
            return;
        }
        
        try {
            File backupFile = new File(getConfigFile().getParent(), 
                                     "config.yml.backup." + System.currentTimeMillis());
            Files.copy(getConfigFile().toPath(), backupFile.toPath());
            logger.info("Created configuration backup at {}", backupFile.getAbsolutePath());
        } catch (IOException e) {
            logger.warn("Failed to create configuration backup: {}", e.getMessage());
        }
    }
    
    /**
     * Applies a specific migration step.
     *
     * @param fromVersion the version to migrate from
     * @param toVersion the version to migrate to
     */
    private void applyMigration(int fromVersion, int toVersion) {
        // Apply version-specific migrations
        switch (fromVersion) {
            case 0 -> migrateFrom0To1();
            default -> logger.debug("No specific migration needed from version {} to {}", 
                                   fromVersion, toVersion);
        }
    }
    
    /**
     * Migration from version 0 (legacy) to version 1.
     */
    private void migrateFrom0To1() {
        // This is for migrating from the old hardcoded configuration to the new structured one
        logger.info("Migrating from legacy configuration format to version 1");
        
        // Ensure all required sections exist with defaults
        if (!contains("discord.activity")) {
            set("discord.activity.type", "PLAYING");
            set("discord.activity.text", "with plugins!");
        }
        
        if (!contains("language.supported")) {
            set("language.supported", java.util.List.of("en-US", "fr-FR", "es-ES", "de-DE"));
        }
        
        if (!contains("commands.system")) {
            set("commands.system.help", true);
            set("commands.system.version", true);
            set("commands.system.shutdown", true);
        }
        
        if (!contains("logging")) {
            set("logging.level", "info");
            set("logging.file.enabled", true);
            set("logging.file.path", "logs/bot.log");
            set("logging.file.max_size", "10MB");
            set("logging.file.max_files", 10);
            set("logging.console.enabled", true);
            set("logging.console.colored", true);
        }
    }
    
    /**
     * Gets the current configuration version.
     *
     * @return the configuration version
     */
    public int getConfigVersion() {
        return getInt(CONFIG_VERSION_KEY, 0);
    }
    
    /**
     * Validates that the configuration has all required fields.
     *
     * @throws ConfigurationException if required fields are missing
     */
    public void validateConfiguration() throws ConfigurationException {
        // Check required discord configuration
        if (!contains("discord.token") || getString("discord.token", "").equals("YOUR_BOT_TOKEN")) {
            throw new ConfigurationException("Discord token is required. Please set discord.token in config.yml");
        }
        
        // Validate other critical settings
        if (!contains("language.default")) {
            throw new ConfigurationException("Default language is required. Please set language.default in config.yml");
        }
        
        if (!contains("commands.default-prefix")) {
            throw new ConfigurationException("Default command prefix is required. Please set commands.default-prefix in config.yml");
        }
    }
}
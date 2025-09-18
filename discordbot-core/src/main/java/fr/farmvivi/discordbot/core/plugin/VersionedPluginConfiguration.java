package fr.farmvivi.discordbot.core.plugin;

import fr.farmvivi.discordbot.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.config.ConfigurationMigrationManager;
import fr.farmvivi.discordbot.core.config.VersionUtils;
import fr.farmvivi.discordbot.core.config.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Enhanced plugin configuration with versioning and automatic default config copying.
 */
public class VersionedPluginConfiguration extends YamlConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(VersionedPluginConfiguration.class);
    
    private static final String DEFAULT_CONFIG_RESOURCE = "default-config.yml";
    private static final String CONFIG_VERSION_KEY = "config_version";
    
    private final String pluginName;
    private final ClassLoader pluginClassLoader;
    private final String targetVersion;
    private final ConfigurationMigrationManager migrationManager;
    
    /**
     * Creates a new versioned plugin configuration.
     *
     * @param pluginName the name of the plugin
     * @param pluginClassLoader the plugin's class loader for accessing resources
     * @param targetVersion the target configuration version
     * @param migrationManager the migration manager for handling version upgrades
     */
    public VersionedPluginConfiguration(String pluginName, ClassLoader pluginClassLoader, 
                                      String targetVersion, ConfigurationMigrationManager migrationManager) {
        super();
        this.pluginName = pluginName;
        this.pluginClassLoader = pluginClassLoader;
        this.targetVersion = VersionUtils.normalizeVersion(targetVersion);
        this.migrationManager = migrationManager;
        
        initialize();
    }
    
    /**
     * Initializes the configuration by setting up the file structure and loading/migrating config.
     */
    private void initialize() {
        // Create plugin folder structure
        File pluginsFolder = new File("plugins");
        File pluginFolder = new File(pluginsFolder, pluginName);
        
        if (!pluginFolder.exists() && !pluginFolder.mkdirs()) {
            logger.warn("Failed to create plugin folder: {}", pluginFolder.getAbsolutePath());
        }
        
        // Set config file path
        File configFile = new File(pluginFolder, "config.yml");
        setConfigFile(configFile);
        
        // Handle configuration initialization
        try {
            if (!configFile.exists()) {
                // Try to copy default configuration from plugin resources
                copyDefaultConfigIfAvailable(configFile);
            }
            
            // Load existing configuration
            reload();
            
            // Check and perform migration if needed
            if (needsMigration()) {
                performMigration();
            } else {
                // Ensure version is set for new configurations
                ensureVersionSet();
            }
            
        } catch (ConfigurationException e) {
            logger.warn("Failed to initialize configuration for plugin {}: {}", pluginName, e.getMessage());
            
            // Create minimal configuration with version
            createMinimalConfiguration();
        }
    }
    
    /**
     * Copies the default configuration from plugin resources if available.
     *
     * @param configFile the target configuration file
     */
    private void copyDefaultConfigIfAvailable(File configFile) {
        try (InputStream defaultConfigStream = pluginClassLoader.getResourceAsStream(DEFAULT_CONFIG_RESOURCE)) {
            if (defaultConfigStream != null) {
                Files.copy(defaultConfigStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.info("Copied default configuration for plugin {} from {}", pluginName, DEFAULT_CONFIG_RESOURCE);
            } else {
                logger.debug("No default configuration found for plugin {} (looking for {})", 
                           pluginName, DEFAULT_CONFIG_RESOURCE);
            }
        } catch (IOException e) {
            logger.warn("Failed to copy default configuration for plugin {}: {}", pluginName, e.getMessage());
        }
    }
    
    /**
     * Checks if the configuration needs migration.
     *
     * @return true if migration is needed
     */
    private boolean needsMigration() {
        if (migrationManager == null) {
            return false;
        }
        
        return migrationManager.needsMigration(this, targetVersion);
    }
    
    /**
     * Performs configuration migration.
     */
    private void performMigration() {
        if (migrationManager == null) {
            logger.warn("Migration needed for plugin {} but no migration manager available", pluginName);
            return;
        }
        
        try {
            String currentVersion = migrationManager.getCurrentVersion(this);
            logger.info("Migrating configuration for plugin {} from version {} to {}", 
                       pluginName, currentVersion, targetVersion);
            
            migrationManager.migrateConfiguration(this, targetVersion, true);
            
            logger.info("Successfully migrated configuration for plugin {} to version {}", 
                       pluginName, targetVersion);
                       
        } catch (ConfigurationException e) {
            logger.error("Failed to migrate configuration for plugin {}: {}", pluginName, e.getMessage(), e);
            
            // Optionally create a minimal configuration as fallback
            createMinimalConfiguration();
        }
    }
    
    /**
     * Ensures that the configuration version is set.
     */
    private void ensureVersionSet() {
        if (!contains(CONFIG_VERSION_KEY)) {
            set(CONFIG_VERSION_KEY, targetVersion);
            try {
                save();
                logger.debug("Set initial configuration version for plugin {} to {}", pluginName, targetVersion);
            } catch (ConfigurationException e) {
                logger.warn("Failed to save configuration version for plugin {}: {}", pluginName, e.getMessage());
            }
        }
    }
    
    /**
     * Creates a minimal configuration with just the version set.
     */
    private void createMinimalConfiguration() {
        try {
            values.clear();
            set(CONFIG_VERSION_KEY, targetVersion);
            set("plugin.name", pluginName);
            set("plugin.enabled", true);
            save();
            
            logger.info("Created minimal configuration for plugin {} with version {}", pluginName, targetVersion);
            
        } catch (ConfigurationException e) {
            logger.error("Failed to create minimal configuration for plugin {}: {}", pluginName, e.getMessage());
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
        return targetVersion;
    }
    
    /**
     * Gets plugin data folder path.
     *
     * @return plugin data folder path
     */
    public String getPluginDataFolder() {
        return getConfigFile().getParentFile().getAbsolutePath();
    }
    
    /**
     * Validates the configuration structure and values.
     *
     * @return true if the configuration is valid
     */
    public boolean validateConfiguration() {
        try {
            // Basic validation - check if version is valid
            String version = getConfigurationVersion();
            if (!VersionUtils.isValidVersion(version)) {
                logger.warn("Invalid configuration version for plugin {}: {}", pluginName, version);
                return false;
            }
            
            // Check if plugin name matches
            String configPluginName = getString("plugin.name", "");
            if (!pluginName.equals(configPluginName) && !configPluginName.isEmpty()) {
                logger.warn("Plugin name mismatch in configuration for {}: expected {}, found {}", 
                           pluginName, pluginName, configPluginName);
                // This is not necessarily an error, just a warning
            }
            
            return true;
            
        } catch (Exception e) {
            logger.warn("Configuration validation failed for plugin {}: {}", pluginName, e.getMessage());
            return false;
        }
    }
    
    @Override
    public void save() throws ConfigurationException {
        // Ensure version is always set before saving
        if (!contains(CONFIG_VERSION_KEY)) {
            set(CONFIG_VERSION_KEY, targetVersion);
        }
        
        super.save();
        
        // Validate after saving
        if (!validateConfiguration()) {
            logger.warn("Configuration validation failed after save for plugin {}", pluginName);
        }
    }
}
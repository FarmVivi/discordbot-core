package fr.farmvivi.discordbot.core.plugin;

import fr.farmvivi.discordbot.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.config.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Plugin-specific configuration implementation.
 * Supports version management and automatic config file copying from plugin JAR.
 */
public class PluginConfiguration extends YamlConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(PluginConfiguration.class);
    private static final String CONFIG_VERSION_KEY = "config_version";
    private static final int CURRENT_CONFIG_VERSION = 1;
    
    private final String pluginName;
    private final PluginClassLoader classLoader;

    /**
     * Creates a new plugin configuration.
     *
     * @param pluginName the name of the plugin
     * @param classLoader the plugin's class loader for accessing JAR resources
     */
    public PluginConfiguration(String pluginName, PluginClassLoader classLoader) {
        super();
        this.pluginName = pluginName;
        this.classLoader = classLoader;

        // Create paths using modern Path API
        File pluginsFolder = new File("plugins");
        File pluginFolder = new File(pluginsFolder, pluginName);

        if (!pluginFolder.exists() && !pluginFolder.mkdirs()) {
            logger.warn("Failed to create plugin folder: {}", pluginFolder.getAbsolutePath());
        }

        // Set config file path
        File configFile = new File(pluginFolder, "config.yml");
        setConfigFile(configFile);

        // Copy default config from JAR if not exists
        copyDefaultConfigIfNeeded(configFile);

        // Try to load existing config or create a new one
        try {
            reload();
            // Check and handle version migration
            handleConfigVersioning();
        } catch (ConfigurationException e) {
            logger.debug("No existing config for plugin {}, will create new when saved", pluginName);
        }
    }

    /**
     * Gets plugin data folder path
     *
     * @return plugin data folder path
     */
    public String getPluginDataFolder() {
        return getConfigFile().getParentFile().getAbsolutePath();
    }
    
    /**
     * Copies the default config.yml from the plugin JAR to the plugin folder if it doesn't exist.
     *
     * @param configFile the target config file
     */
    private void copyDefaultConfigIfNeeded(File configFile) {
        if (configFile.exists()) {
            return; // Config file already exists, nothing to do
        }
        
        if (classLoader == null) {
            logger.debug("No class loader available for plugin {}, cannot copy default config", pluginName);
            return;
        }
        
        // Try to find config.yml in the plugin JAR
        try (InputStream defaultConfigStream = classLoader.getResourceAsStream("config.yml")) {
            if (defaultConfigStream == null) {
                logger.debug("No default config.yml found in plugin {} JAR", pluginName);
                return;
            }
            
            // Copy the default config to the plugin folder
            Files.copy(defaultConfigStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Copied default config.yml for plugin {} to {}", pluginName, configFile.getAbsolutePath());
            
        } catch (IOException e) {
            logger.warn("Failed to copy default config for plugin {}: {}", pluginName, e.getMessage());
        }
    }
    
    /**
     * Handles configuration versioning and migration.
     */
    private void handleConfigVersioning() {
        int currentVersion = getInt(CONFIG_VERSION_KEY, 0);
        
        if (currentVersion == 0) {
            // First time loading or old config without version
            logger.info("Adding version {} to plugin {} configuration", CURRENT_CONFIG_VERSION, pluginName);
            set(CONFIG_VERSION_KEY, CURRENT_CONFIG_VERSION);
            try {
                save();
            } catch (ConfigurationException e) {
                logger.warn("Failed to save version to plugin {} configuration: {}", pluginName, e.getMessage());
            }
        } else if (currentVersion < CURRENT_CONFIG_VERSION) {
            // Configuration needs migration
            logger.info("Migrating plugin {} configuration from version {} to {}", 
                       pluginName, currentVersion, CURRENT_CONFIG_VERSION);
            migrateConfiguration(currentVersion, CURRENT_CONFIG_VERSION);
        } else if (currentVersion > CURRENT_CONFIG_VERSION) {
            // Configuration is from a newer version
            logger.warn("Plugin {} configuration version {} is newer than expected {}. " +
                       "This may cause compatibility issues.", 
                       pluginName, currentVersion, CURRENT_CONFIG_VERSION);
        }
    }
    
    /**
     * Migrates configuration from one version to another.
     *
     * @param fromVersion the current version
     * @param toVersion the target version
     */
    private void migrateConfiguration(int fromVersion, int toVersion) {
        // Apply migrations step by step
        for (int version = fromVersion; version < toVersion; version++) {
            try {
                applyMigration(version, version + 1);
                logger.debug("Applied migration {} -> {} for plugin {}", version, version + 1, pluginName);
            } catch (Exception e) {
                logger.error("Failed to apply migration {} -> {} for plugin {}: {}", 
                           version, version + 1, pluginName, e.getMessage());
                return; // Stop migration on error
            }
        }
        
        // Update version and save
        set(CONFIG_VERSION_KEY, toVersion);
        try {
            save();
            logger.info("Successfully migrated plugin {} configuration to version {}", pluginName, toVersion);
        } catch (ConfigurationException e) {
            logger.error("Failed to save migrated configuration for plugin {}: {}", pluginName, e.getMessage());
        }
    }
    
    /**
     * Applies a specific migration step.
     *
     * @param fromVersion the version to migrate from
     * @param toVersion the version to migrate to
     */
    private void applyMigration(int fromVersion, int toVersion) {
        // Future plugin-specific migrations can be added here
        // For now, this is a placeholder for when migrations are needed
        logger.debug("No specific migration needed for plugin {} from version {} to {}", 
                    pluginName, fromVersion, toVersion);
    }
    
    /**
     * Gets the current configuration version.
     *
     * @return the configuration version
     */
    public int getConfigVersion() {
        return getInt(CONFIG_VERSION_KEY, 0);
    }
}
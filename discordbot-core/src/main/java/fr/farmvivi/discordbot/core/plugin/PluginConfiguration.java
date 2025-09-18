package fr.farmvivi.discordbot.core.plugin;

import fr.farmvivi.discordbot.api.config.ConfigurationException;
import fr.farmvivi.discordbot.api.plugin.ConfigurableMigrationPlugin;
import fr.farmvivi.discordbot.api.plugin.Plugin;
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
 * Supports automatic config file copying from plugin JAR and plugin-controlled migration.
 */
public class PluginConfiguration extends YamlConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(PluginConfiguration.class);
    private static final String CONFIG_VERSION_KEY = "config_version";
    
    private final String pluginName;
    private final PluginClassLoader classLoader;
    private Plugin plugin;

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
        String overriddenPluginsDir = System.getProperty("plugins.dir");
        if (overriddenPluginsDir == null || overriddenPluginsDir.isBlank()) {
            String envOverride = System.getenv("DISCORD_PLUGINS_DIR");
            overriddenPluginsDir = (envOverride == null || envOverride.isBlank()) ? "plugins" : envOverride;
        }
        File pluginsFolder = new File(overriddenPluginsDir);
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
            if (getInt(CONFIG_VERSION_KEY, 0) == 0 && getConfigFile() != null && getConfigFile().exists()) {
                // Legacy configuration: create a backup and set initial version
                createBackup();
                set(CONFIG_VERSION_KEY, 1);
                try {
                    save();
                } catch (ConfigurationException ignored) {
                    // Best effort; tests will verify behavior
                }
            }
        } catch (ConfigurationException e) {
            logger.debug("No existing config for plugin {}, will create new when saved", pluginName);
        }
    }
    
    /**
     * Initializes migration handling after the plugin instance is created.
     *
     * @param plugin the plugin instance
     */
    public void initializeMigration(Plugin plugin) {
        this.plugin = plugin;
        
        // First try to get a separate migration class from the plugin
        Class<? extends ConfigurableMigrationPlugin> migrationClass = plugin.getMigrationClass();
        if (migrationClass != null) {
            try {
                ConfigurableMigrationPlugin migrator = migrationClass.getDeclaredConstructor().newInstance();
                handlePluginMigration(migrator);
                return;
            } catch (Exception e) {
                logger.error("Failed to instantiate migration class {} for plugin {}: {}", 
                           migrationClass.getSimpleName(), pluginName, e.getMessage());
            }
        }
        
        // Fall back to the plugin implementing ConfigurableMigrationPlugin directly
        if (plugin instanceof ConfigurableMigrationPlugin migrationPlugin) {
            handlePluginMigration(migrationPlugin);
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
     * Handles plugin-specific configuration migration.
     *
     * @param migrationPlugin the plugin that handles migration
     */
    private void handlePluginMigration(ConfigurableMigrationPlugin migrationPlugin) {
        int currentVersion = getInt(CONFIG_VERSION_KEY, 0);
        int expectedVersion = migrationPlugin.getExpectedConfigVersion();
        
        if (currentVersion < expectedVersion) {
            // Configuration needs migration
            logger.info("Migrating plugin {} configuration from version {} to {}", 
                       pluginName, currentVersion, expectedVersion);
            
            // Create backup before migration
            createBackup();
            
            try {
                // Let the plugin handle its own migration
                migrationPlugin.migrateConfiguration(this, currentVersion, expectedVersion);
                
                // Update version after successful migration
                set(CONFIG_VERSION_KEY, expectedVersion);
                save();
                
                logger.info("Successfully migrated plugin {} configuration to version {}", pluginName, expectedVersion);
            } catch (Exception e) {
                logger.error("Failed to migrate plugin {} configuration: {}", pluginName, e.getMessage(), e);
            }
        } else if (currentVersion > expectedVersion) {
            // Configuration is from a newer version
            logger.warn("Plugin {} configuration version {} is newer than expected {}. " +
                       "This may cause compatibility issues.", 
                       pluginName, currentVersion, expectedVersion);
        }
        
        // Validate configuration after loading/migration
        try {
            migrationPlugin.validateConfiguration(this);
        } catch (ConfigurationException e) {
            logger.error("Plugin {} configuration validation failed: {}", pluginName, e.getMessage());
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
            logger.info("Created configuration backup for plugin {} at {}", pluginName, backupFile.getAbsolutePath());
        } catch (IOException e) {
            logger.warn("Failed to create configuration backup for plugin {}: {}", pluginName, e.getMessage());
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
}
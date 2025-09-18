package fr.farmvivi.discordbot.core.plugin;

import fr.farmvivi.discordbot.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.config.ConfigurationMigrationManager;
import fr.farmvivi.discordbot.core.config.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Plugin-specific configuration implementation with basic default config copying.
 * For advanced versioning and migration support, use VersionedPluginConfiguration.
 */
public class PluginConfiguration extends YamlConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(PluginConfiguration.class);
    
    private static final String DEFAULT_CONFIG_RESOURCE = "default-config.yml";
    
    private final String pluginName;

    /**
     * Creates a new plugin configuration.
     *
     * @param pluginName the name of the plugin
     */
    public PluginConfiguration(String pluginName) {
        this(pluginName, null);
    }
    
    /**
     * Creates a new plugin configuration with optional class loader for default config copying.
     *
     * @param pluginName the name of the plugin
     * @param pluginClassLoader the plugin's class loader (null to skip default config copying)
     */
    public PluginConfiguration(String pluginName, ClassLoader pluginClassLoader) {
        super();
        this.pluginName = pluginName;

        // Create paths using modern Path API
        File pluginsFolder = new File("plugins");
        File pluginFolder = new File(pluginsFolder, pluginName);

        if (!pluginFolder.exists() && !pluginFolder.mkdirs()) {
            logger.warn("Failed to create plugin folder: {}", pluginFolder.getAbsolutePath());
        }

        // Set config file path
        File configFile = new File(pluginFolder, "config.yml");
        setConfigFile(configFile);

        // Try to copy default configuration if file doesn't exist and we have a class loader
        if (!configFile.exists() && pluginClassLoader != null) {
            copyDefaultConfigIfAvailable(configFile, pluginClassLoader);
        }

        // Try to load existing config or create a new one
        try {
            reload();
        } catch (ConfigurationException e) {
            logger.debug("No existing config for plugin {}, will create new when saved", pluginName);
        }
    }
    
    /**
     * Creates a versioned plugin configuration.
     *
     * @param pluginName the name of the plugin
     * @param pluginClassLoader the plugin's class loader
     * @param targetVersion the target configuration version
     * @param migrationManager the migration manager
     * @return a versioned plugin configuration
     */
    public static VersionedPluginConfiguration createVersioned(String pluginName, 
                                                             ClassLoader pluginClassLoader,
                                                             String targetVersion,
                                                             ConfigurationMigrationManager migrationManager) {
        return new VersionedPluginConfiguration(pluginName, pluginClassLoader, targetVersion, migrationManager);
    }
    
    /**
     * Copies the default configuration from plugin resources if available.
     *
     * @param configFile the target configuration file
     * @param pluginClassLoader the plugin's class loader
     */
    private void copyDefaultConfigIfAvailable(File configFile, ClassLoader pluginClassLoader) {
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
     * Gets plugin data folder path
     *
     * @return plugin data folder path
     */
    public String getPluginDataFolder() {
        return getConfigFile().getParentFile().getAbsolutePath();
    }
}
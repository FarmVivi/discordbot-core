package fr.farmvivi.discordbot.core.plugin;

import fr.farmvivi.discordbot.core.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.config.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Plugin-specific configuration implementation.
 */
public class PluginConfiguration extends YamlConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(PluginConfiguration.class);

    /**
     * Creates a new plugin configuration.
     *
     * @param pluginName the name of the plugin
     */
    public PluginConfiguration(String pluginName) {
        super();

        // Create paths using modern Path API
        File pluginsFolder = new File("plugins");
        File pluginFolder = new File(pluginsFolder, pluginName);

        if (!pluginFolder.exists() && !pluginFolder.mkdirs()) {
            logger.warn("Failed to create plugin folder: {}", pluginFolder.getAbsolutePath());
        }

        // Set config file path
        File configFile = new File(pluginFolder, "config.yml");
        setConfigFile(configFile);

        // Try to load existing config or create a new one
        try {
            reload();
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
}
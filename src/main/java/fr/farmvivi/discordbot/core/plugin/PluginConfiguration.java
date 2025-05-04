package fr.farmvivi.discordbot.core.plugin;

import fr.farmvivi.discordbot.core.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.config.YamlConfiguration;

import java.io.File;

/**
 * Configuration implementation for plugins that extends YamlConfiguration.
 */
public class PluginConfiguration extends YamlConfiguration {
    /**
     * Creates a new plugin configuration.
     *
     * @param pluginName the name of the plugin
     */
    public PluginConfiguration(String pluginName) {
        super();

        // Configuration file is in plugins/PluginName/config.yml
        File pluginsFolder = new File("plugins");
        File pluginFolder = new File(pluginsFolder, pluginName);

        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        setConfigFile(new File(pluginFolder, "config.yml"));

        try {
            reload();
        } catch (ConfigurationException e) {
            // Ignore, will create a new config file
        }
    }
}
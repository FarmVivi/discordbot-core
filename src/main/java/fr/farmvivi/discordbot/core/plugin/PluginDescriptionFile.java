package fr.farmvivi.discordbot.core.plugin;

import fr.farmvivi.discordbot.core.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.config.YamlConfiguration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the metadata for a plugin loaded from plugin.yml.
 * Now extends YamlConfiguration.
 */
public class PluginDescriptionFile extends YamlConfiguration {
    /**
     * Creates a new plugin description file from an input stream.
     *
     * @param inputStream the input stream containing the YAML data
     * @throws ConfigurationException if there's an error parsing the YAML
     */
    public PluginDescriptionFile(InputStream inputStream) throws ConfigurationException {
        super();

        // Load YAML from input stream
        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        values = yaml.load(inputStream);

        // Validate required fields
        if (getName() == null || getName().isEmpty()) {
            throw new ConfigurationException("Plugin name cannot be empty");
        }

        if (getMain() == null || getMain().isEmpty()) {
            throw new ConfigurationException("Main class cannot be empty");
        }

        if (getVersion() == null || getVersion().isEmpty()) {
            throw new ConfigurationException("Version cannot be empty");
        }
    }

    /**
     * Gets the plugin's name.
     *
     * @return the name
     */
    public String getName() {
        return getString("name", "");
    }

    /**
     * Gets the plugin's main class.
     *
     * @return the main class
     */
    public String getMain() {
        return getString("main", "");
    }

    /**
     * Gets the plugin's version.
     *
     * @return the version
     */
    public String getVersion() {
        return getString("version", "");
    }

    /**
     * Gets the plugin's description.
     *
     * @return the description
     */
    public String getDescription() {
        return getString("description", "");
    }

    /**
     * Gets the plugin's authors.
     *
     * @return the authors
     */
    public List<String> getAuthors() {
        try {
            return getStringList("authors");
        } catch (ConfigurationException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Gets the plugin's dependencies.
     *
     * @return the dependencies
     */
    public List<String> getDependencies() {
        try {
            return getStringList("dependencies");
        } catch (ConfigurationException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Gets the plugin's soft dependencies.
     *
     * @return the soft dependencies
     */
    public List<String> getSoftDependencies() {
        try {
            return getStringList("soft-dependencies");
        } catch (ConfigurationException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void save() throws ConfigurationException {
        throw new ConfigurationException("PluginDescriptionFile cannot be saved");
    }

    @Override
    public void reload() throws ConfigurationException {
        throw new ConfigurationException("PluginDescriptionFile cannot be reloaded");
    }
}
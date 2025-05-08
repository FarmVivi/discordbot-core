package fr.farmvivi.discordbot.core.plugin;

import fr.farmvivi.discordbot.core.api.config.ConfigurationException;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable record representing plugin metadata loaded from plugin.yml.
 */
public record PluginDescriptor(
        String name,
        String main,
        String version,
        String description,
        List<String> authors,
        List<String> dependencies,
        List<String> softDependencies
) {
    /**
     * Creates a new plugin descriptor from plugin.yml input stream.
     *
     * @param inputStream the input stream of the plugin.yml file
     * @return a new PluginDescriptor
     * @throws ConfigurationException if there's an error parsing the YAML
     */
    public static PluginDescriptor fromYaml(InputStream inputStream) throws ConfigurationException {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> values = yaml.load(inputStream);

            // Extract required fields with validation
            String name = getRequiredString(values, "name", "Plugin name cannot be empty");
            String main = getRequiredString(values, "main", "Main class cannot be empty");
            String version = getRequiredString(values, "version", "Version cannot be empty");

            // Extract optional fields
            String description = getString(values, "description", "");
            List<String> authors = getStringList(values, "authors");
            List<String> dependencies = getStringList(values, "dependencies");
            List<String> softDependencies = getStringList(values, "soft-dependencies");

            return new PluginDescriptor(
                    name, main, version, description,
                    Collections.unmodifiableList(authors),
                    Collections.unmodifiableList(dependencies),
                    Collections.unmodifiableList(softDependencies)
            );
        } catch (Exception e) {
            throw new ConfigurationException("Failed to parse plugin.yml: " + e.getMessage(), e);
        }
    }

    private static String getRequiredString(Map<String, Object> map, String key, String errorMessage)
            throws ConfigurationException {
        Object value = map.get(key);
        if (value == null || value.toString().isEmpty()) {
            throw new ConfigurationException(errorMessage);
        }
        return value.toString();
    }

    private static String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    @SuppressWarnings("unchecked")
    private static List<String> getStringList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        List<String> result = new ArrayList<>();

        if (value instanceof List) {
            List<?> list = (List<?>) value;
            for (Object item : list) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
        }

        return result;
    }
}
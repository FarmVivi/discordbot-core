package fr.farmvivi.discordbot.core.plugin;

import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import fr.farmvivi.discordbot.core.api.plugin.PluginLoader;
import fr.farmvivi.discordbot.core.api.plugin.PluginStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Implementation of PluginLoader that loads and manages plugins.
 */
public class PluginManager implements PluginLoader {
    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);

    private final Map<String, Plugin> plugins = new HashMap<>();
    private final Map<String, PluginClassLoader> classLoaders = new HashMap<>();
    private final File pluginsFolder;
    private final EventManager eventManager;
    private final DiscordAPI discordAPI;
    private final Configuration coreConfig;

    /**
     * Creates a new plugin manager.
     *
     * @param pluginsFolder the folder where plugins are stored
     * @param eventManager  the event manager
     * @param discordAPI    the Discord API
     * @param coreConfig    the core configuration
     */
    public PluginManager(File pluginsFolder, EventManager eventManager, DiscordAPI discordAPI, Configuration coreConfig) {
        this.pluginsFolder = pluginsFolder;
        this.eventManager = eventManager;
        this.discordAPI = discordAPI;
        this.coreConfig = coreConfig;

        if (!pluginsFolder.exists()) {
            pluginsFolder.mkdirs();
        }
    }

    /**
     * Loads all plugins from the plugins folder.
     */
    public void loadPlugins() {
        logger.info("Loading plugins from {}", pluginsFolder.getAbsolutePath());

        File[] files = pluginsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null) {
            logger.warn("Failed to list files in plugins folder");
            return;
        }

        for (File file : files) {
            try {
                Plugin plugin = loadPlugin(file.getAbsolutePath());
                if (plugin != null) {
                    plugins.put(plugin.getName(), plugin);
                    logger.info("Loaded plugin: {} v{}", plugin.getName(), plugin.getVersion());
                }
            } catch (Exception e) {
                logger.error("Failed to load plugin: {}", file.getName(), e);
            }
        }
    }

    /**
     * Pre-enables all loaded plugins.
     * This is when plugins can configure the JDABuilder before Discord connection.
     */
    public void preEnablePlugins() {
        logger.info("Pre-enabling plugins...");

        for (Plugin plugin : plugins.values()) {
            try {
                if (plugin.getStatus() == PluginStatus.LOADED) {
                    plugin.setStatus(PluginStatus.PRE_ENABLE);
                    plugin.onPreEnable();
                    logger.debug("Pre-enabled plugin: {}", plugin.getName());
                }
            } catch (Exception e) {
                logger.error("Error during pre-enable of plugin: {}", plugin.getName(), e);
                plugin.setStatus(PluginStatus.LOADED);
            }
        }
    }

    /**
     * Enables all pre-enabled plugins.
     * This is called after Discord connection is established.
     */
    public void enablePlugins() {
        logger.info("Enabling plugins...");

        for (Plugin plugin : plugins.values()) {
            try {
                if (plugin.getStatus() == PluginStatus.PRE_ENABLE) {
                    plugin.setStatus(PluginStatus.ENABLE);
                    plugin.onEnable();
                    logger.debug("Enabled plugin: {}", plugin.getName());
                }
            } catch (Exception e) {
                logger.error("Error during enable of plugin: {}", plugin.getName(), e);
                plugin.setStatus(PluginStatus.PRE_ENABLE);
            }
        }
    }

    /**
     * Post-enables all enabled plugins.
     */
    public void postEnablePlugins() {
        logger.info("Post-enabling plugins...");

        for (Plugin plugin : plugins.values()) {
            try {
                if (plugin.getStatus() == PluginStatus.ENABLE) {
                    plugin.setStatus(PluginStatus.POST_ENABLE);
                    plugin.onPostEnable();
                    logger.debug("Post-enabled plugin: {}", plugin.getName());
                }
            } catch (Exception e) {
                logger.error("Error during post-enable of plugin: {}", plugin.getName(), e);
            }
        }

        // Final pass: mark as enabled
        for (Plugin plugin : plugins.values()) {
            try {
                if (plugin.getStatus() == PluginStatus.POST_ENABLE) {
                    plugin.setStatus(PluginStatus.ENABLED);
                    logger.info("Plugin fully enabled: {} v{}", plugin.getName(), plugin.getVersion());
                }
            } catch (Exception e) {
                logger.error("Error during final enable of plugin: {}", plugin.getName(), e);
            }
        }
    }

    /**
     * Pre-disables all enabled plugins.
     */
    public void preDisablePlugins() {
        logger.info("Pre-disabling plugins...");

        for (Plugin plugin : plugins.values()) {
            try {
                if (plugin.getStatus() == PluginStatus.ENABLED) {
                    plugin.setStatus(PluginStatus.PRE_DISABLE);
                    plugin.onPreDisable();
                    logger.debug("Pre-disabled plugin: {}", plugin.getName());
                }
            } catch (Exception e) {
                logger.error("Error during pre-disable of plugin: {}", plugin.getName(), e);
            }
        }
    }

    /**
     * Disables all pre-disabled plugins.
     */
    public void disablePlugins() {
        logger.info("Disabling plugins...");

        // Call onDisable
        for (Plugin plugin : plugins.values()) {
            try {
                if (plugin.getStatus() == PluginStatus.PRE_DISABLE) {
                    plugin.setStatus(PluginStatus.DISABLE);
                    plugin.onDisable();
                    logger.debug("Disabled plugin: {}", plugin.getName());
                }
            } catch (Exception e) {
                logger.error("Error during disable of plugin: {}", plugin.getName(), e);
            }
        }
    }

    /**
     * Post-disables all disabled plugins.
     */
    public void postDisablePlugins() {
        logger.info("Post-disabling plugins...");

        // Post-disable phase
        for (Plugin plugin : plugins.values()) {
            try {
                if (plugin.getStatus() == PluginStatus.DISABLE) {
                    plugin.setStatus(PluginStatus.POST_DISABLE);
                    plugin.onPostDisable();
                    logger.debug("Post-disabled plugin: {}", plugin.getName());
                }
            } catch (Exception e) {
                logger.error("Error during post-disable of plugin: {}", plugin.getName(), e);
            }
        }

        // Final pass: mark as disabled and clean up
        for (Plugin plugin : plugins.values()) {
            try {
                if (plugin.getStatus() == PluginStatus.POST_DISABLE) {
                    plugin.setStatus(PluginStatus.DISABLED);
                    logger.info("Plugin fully disabled: {}", plugin.getName());
                }
            } catch (Exception e) {
                logger.error("Error during final disable of plugin: {}", plugin.getName(), e);
            }
        }

        // Clean up classloaders
        for (PluginClassLoader classLoader : classLoaders.values()) {
            try {
                classLoader.close();
            } catch (IOException e) {
                logger.error("Error closing classloader", e);
            }
        }

        plugins.clear();
        classLoaders.clear();
    }

    /**
     * Reloads all plugins.
     * This will disable all plugins, then load and enable them again.
     */
    public void reloadPlugins() {
        logger.info("Reloading all plugins...");

        // Save plugin names for logging
        Set<String> oldPluginNames = new HashSet<>(plugins.keySet());

        // Disable all plugins in the proper sequence
        preDisablePlugins();
        disablePlugins();
        postDisablePlugins();

        // Load and enable plugins
        loadPlugins();
        preEnablePlugins();
        // Note: Connection to Discord should happen here in Discobocor

        // Load and enable plugins
        Set<String> newPluginNames = plugins.keySet();

        // Log removed plugins
        Set<String> removedPlugins = new HashSet<>(oldPluginNames);
        removedPlugins.removeAll(newPluginNames);
        if (!removedPlugins.isEmpty()) {
            logger.info("The following plugins were removed during reload: {}", String.join(", ", removedPlugins));
        }

        // Log new plugins
        Set<String> addedPlugins = new HashSet<>(newPluginNames);
        addedPlugins.removeAll(oldPluginNames);
        if (!addedPlugins.isEmpty()) {
            logger.info("The following plugins were added during reload: {}", String.join(", ", addedPlugins));
        }
    }

    /**
     * Completes the reload process by enabling and post-enabling plugins.
     * This should be called after Discord has been reconnected.
     */
    public void completeReload() {
        enablePlugins();
        postEnablePlugins();
        logger.info("Plugin reload complete!");
    }

    /**
     * Reloads a specific plugin.
     *
     * @param pluginName the name of the plugin to reload
     * @return true if the plugin was successfully reloaded, false otherwise
     */
    public boolean reloadPlugin(String pluginName) {
        logger.info("Reloading plugin: {}", pluginName);

        Plugin plugin = getPlugin(pluginName);
        if (plugin == null) {
            logger.warn("Cannot reload plugin {}: not found", pluginName);
            return false;
        }

        // Get the plugin's JAR file
        PluginClassLoader classLoader = classLoaders.get(pluginName);
        if (classLoader == null) {
            logger.warn("Cannot reload plugin {}: classloader not found", pluginName);
            return false;
        }

        URL[] urls = classLoader.getURLs();
        if (urls.length == 0) {
            logger.warn("Cannot reload plugin {}: no JAR file", pluginName);
            return false;
        }

        String jarPath = urls[0].getPath();
        if (!new File(jarPath).exists()) {
            logger.warn("Cannot reload plugin {}: JAR file not found at {}", pluginName, jarPath);
            return false;
        }

        // Disable the plugin in proper sequence
        if (plugin.getStatus() == PluginStatus.ENABLED) {
            plugin.setStatus(PluginStatus.PRE_DISABLE);
            plugin.onPreDisable();
            plugin.setStatus(PluginStatus.DISABLE);
            plugin.onDisable();
            plugin.setStatus(PluginStatus.POST_DISABLE);
            plugin.onPostDisable();
            plugin.setStatus(PluginStatus.DISABLED);
        }

        // Remove the plugin
        plugins.remove(pluginName);

        // Close the classloader
        try {
            classLoader.close();
        } catch (IOException e) {
            logger.error("Error closing classloader for plugin {}", pluginName, e);
        }
        classLoaders.remove(pluginName);

        // Load the plugin again
        try {
            Plugin newPlugin = loadPlugin(jarPath);
            if (newPlugin != null) {
                plugins.put(newPlugin.getName(), newPlugin);

                // Enable the plugin in proper sequence
                newPlugin.setStatus(PluginStatus.PRE_ENABLE);
                newPlugin.onPreEnable();
                newPlugin.setStatus(PluginStatus.ENABLE);
                newPlugin.onEnable();
                newPlugin.setStatus(PluginStatus.POST_ENABLE);
                newPlugin.onPostEnable();
                newPlugin.setStatus(PluginStatus.ENABLED);

                logger.info("Successfully reloaded plugin: {} v{}", newPlugin.getName(), newPlugin.getVersion());
                return true;
            } else {
                logger.error("Failed to reload plugin: {}", pluginName);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error reloading plugin: {}", pluginName, e);
            return false;
        }
    }

    @Override
    public Plugin loadPlugin(String jarFile) {
        File file = new File(jarFile);
        if (!file.exists()) {
            logger.error("Plugin file does not exist: {}", jarFile);
            return null;
        }

        try (JarFile jar = new JarFile(file)) {
            // Look for plugin.yml
            JarEntry entry = jar.getJarEntry("plugin.yml");
            if (entry == null) {
                logger.error("Plugin does not contain plugin.yml: {}", jarFile);
                return null;
            }

            // Parse plugin.yml to get the main class
            PluginDescriptionFile description = new PluginDescriptionFile(jar.getInputStream(entry));

            // Create the class loader
            URL[] urls = {file.toURI().toURL()};
            PluginClassLoader classLoader = new PluginClassLoader(urls, getClass().getClassLoader());

            // Load the main class
            Class<?> mainClass = classLoader.loadClass(description.getMain());
            if (!Plugin.class.isAssignableFrom(mainClass)) {
                logger.error("Main class does not implement Plugin: {}", description.getMain());
                classLoader.close();
                return null;
            }

            // Instantiate the plugin
            Plugin plugin = (Plugin) mainClass.getDeclaredConstructor().newInstance();

            // Create the plugin context
            PluginContextImpl context = new PluginContextImpl(
                    LoggerFactory.getLogger(description.getName()),
                    eventManager,
                    discordAPI,
                    new PluginConfiguration(description.getName()),
                    new File(pluginsFolder, description.getName()).getAbsolutePath(),
                    this,
                    classLoader
            );

            // Initialize the plugin
            plugin.onLoad(context);

            // Store the classloader
            classLoaders.put(description.getName(), classLoader);

            return plugin;
        } catch (Exception e) {
            logger.error("Failed to load plugin: {}", jarFile, e);
            return null;
        }
    }

    @Override
    public boolean enablePlugin(Plugin plugin) {
        if (plugin.getStatus() != PluginStatus.LOADED) {
            logger.warn("Cannot enable plugin {}: not in LOADED state", plugin.getName());
            return false;
        }

        try {
            // Follow the proper sequence
            plugin.setStatus(PluginStatus.PRE_ENABLE);
            plugin.onPreEnable();

            plugin.setStatus(PluginStatus.ENABLE);
            plugin.onEnable();

            plugin.setStatus(PluginStatus.POST_ENABLE);
            plugin.onPostEnable();

            plugin.setStatus(PluginStatus.ENABLED);
            logger.info("Plugin enabled: {} v{}", plugin.getName(), plugin.getVersion());
            return true;
        } catch (Exception e) {
            logger.error("Failed to enable plugin: {}", plugin.getName(), e);
            plugin.setStatus(PluginStatus.LOADED);
            return false;
        }
    }

    @Override
    public boolean disablePlugin(Plugin plugin) {
        if (plugin.getStatus() != PluginStatus.ENABLED) {
            logger.warn("Cannot disable plugin {}: not in ENABLED state", plugin.getName());
            return false;
        }

        try {
            // Follow the proper sequence
            plugin.setStatus(PluginStatus.PRE_DISABLE);
            plugin.onPreDisable();

            plugin.setStatus(PluginStatus.DISABLE);
            plugin.onDisable();

            plugin.setStatus(PluginStatus.POST_DISABLE);
            plugin.onPostDisable();

            plugin.setStatus(PluginStatus.DISABLED);
            logger.info("Plugin disabled: {}", plugin.getName());
            return true;
        } catch (Exception e) {
            logger.error("Failed to disable plugin: {}", plugin.getName(), e);
            plugin.setStatus(PluginStatus.ENABLED);
            return false;
        }
    }

    @Override
    public Plugin getPlugin(String name) {
        return plugins.get(name);
    }

    @Override
    public List<Plugin> getPlugins() {
        return new ArrayList<>(plugins.values());
    }

    @Override
    public boolean isPluginLoaded(String name) {
        return plugins.containsKey(name);
    }
}
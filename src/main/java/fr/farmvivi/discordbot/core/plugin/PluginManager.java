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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * Enables all loaded plugins.
     */
    public void enablePlugins() {
        logger.info("Enabling plugins...");

        // First pass: pre-enable
        for (Plugin plugin : plugins.values()) {
            try {
                plugin.setStatus(PluginStatus.PRE_ENABLE);
                logger.debug("Pre-enabling plugin: {}", plugin.getName());
            } catch (Exception e) {
                logger.error("Error during pre-enable of plugin: {}", plugin.getName(), e);
            }
        }

        // Second pass: enable
        for (Plugin plugin : plugins.values()) {
            try {
                if (plugin.getStatus() == PluginStatus.PRE_ENABLE) {
                    plugin.setStatus(PluginStatus.ENABLE);
                    plugin.onEnable();
                    logger.debug("Enabled plugin: {}", plugin.getName());
                }
            } catch (Exception e) {
                logger.error("Error during enable of plugin: {}", plugin.getName(), e);
            }
        }

        // Third pass: post-enable
        for (Plugin plugin : plugins.values()) {
            try {
                if (plugin.getStatus() == PluginStatus.ENABLE) {
                    plugin.setStatus(PluginStatus.POST_ENABLE);
                    logger.debug("Post-enabling plugin: {}", plugin.getName());
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
                    logger.info("Plugin enabled: {} v{}", plugin.getName(), plugin.getVersion());
                }
            } catch (Exception e) {
                logger.error("Error during final enable of plugin: {}", plugin.getName(), e);
            }
        }
    }

    /**
     * Disables all loaded plugins.
     */
    public void disablePlugins() {
        logger.info("Disabling plugins...");

        // First pass: pre-disable
        for (Plugin plugin : plugins.values()) {
            try {
                if (plugin.getStatus() == PluginStatus.ENABLED) {
                    plugin.setStatus(PluginStatus.PRE_DISABLE);
                    logger.debug("Pre-disabling plugin: {}", plugin.getName());
                }
            } catch (Exception e) {
                logger.error("Error during pre-disable of plugin: {}", plugin.getName(), e);
            }
        }

        // Second pass: disable
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

        // Third pass: post-disable
        for (Plugin plugin : plugins.values()) {
            try {
                if (plugin.getStatus() == PluginStatus.DISABLE) {
                    plugin.setStatus(PluginStatus.POST_DISABLE);
                    logger.debug("Post-disabling plugin: {}", plugin.getName());
                }
            } catch (Exception e) {
                logger.error("Error during post-disable of plugin: {}", plugin.getName(), e);
            }
        }

        // Final pass: mark as disabled
        for (Plugin plugin : plugins.values()) {
            try {
                if (plugin.getStatus() == PluginStatus.POST_DISABLE) {
                    plugin.setStatus(PluginStatus.DISABLED);
                    logger.info("Plugin disabled: {}", plugin.getName());
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
            plugin.setStatus(PluginStatus.PRE_ENABLE);
            plugin.setStatus(PluginStatus.ENABLE);
            plugin.onEnable();
            plugin.setStatus(PluginStatus.POST_ENABLE);
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
            plugin.setStatus(PluginStatus.PRE_DISABLE);
            plugin.setStatus(PluginStatus.DISABLE);
            plugin.onDisable();
            plugin.setStatus(PluginStatus.POST_DISABLE);
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
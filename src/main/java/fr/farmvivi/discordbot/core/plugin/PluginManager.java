package fr.farmvivi.discordbot.core.plugin;

import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.api.permissions.PermissionManager;
import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import fr.farmvivi.discordbot.core.api.plugin.PluginLifecycle;
import fr.farmvivi.discordbot.core.api.plugin.PluginLoader;
import fr.farmvivi.discordbot.core.api.plugin.events.*;
import fr.farmvivi.discordbot.core.api.storage.DataStorageManager;
import fr.farmvivi.discordbot.core.api.storage.binary.BinaryStorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Main implementation of PluginLoader that manages the plugin lifecycle.
 */
public class PluginManager implements PluginLoader, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);

    // Core services
    private final File pluginsFolder;
    private final EventManager eventManager;
    private final DiscordAPI discordAPI;
    private final LanguageManager languageManager;
    private final DataStorageManager dataStorageManager;
    private final BinaryStorageManager binaryStorageManager;
    private final PermissionManager permissionManager;

    // Plugin tracking
    private final Map<String, Plugin> plugins = new ConcurrentHashMap<>();
    private final Map<String, PluginClassLoader> classLoaders = new ConcurrentHashMap<>();
    private final Map<String, PluginDescriptor> pluginDescriptors = new ConcurrentHashMap<>();
    private final Map<String, String> pluginJarPaths = new ConcurrentHashMap<>();
    private final Set<String> failedPlugins = ConcurrentHashMap.newKeySet();

    /**
     * Creates a new plugin manager.
     *
     * @param pluginsFolder        the folder where plugins are stored
     * @param eventManager         the event manager
     * @param discordAPI           the Discord API
     * @param languageManager      the language manager
     * @param dataStorageManager   the data storage manager
     * @param binaryStorageManager the binary storage manager
     * @param permissionManager    the permission manager
     */
    public PluginManager(
            File pluginsFolder,
            EventManager eventManager,
            DiscordAPI discordAPI,
            LanguageManager languageManager,
            DataStorageManager dataStorageManager,
            BinaryStorageManager binaryStorageManager,
            PermissionManager permissionManager) {
        this.pluginsFolder = pluginsFolder;
        this.eventManager = eventManager;
        this.discordAPI = discordAPI;
        this.languageManager = languageManager;
        this.dataStorageManager = dataStorageManager;
        this.binaryStorageManager = binaryStorageManager;
        this.permissionManager = permissionManager;

        if (!pluginsFolder.exists() && !pluginsFolder.mkdirs()) {
            logger.warn("Failed to create plugins folder: {}", pluginsFolder.getAbsolutePath());
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

            // Parse plugin.yml
            PluginDescriptor descriptor = PluginDescriptor.fromYaml(jar.getInputStream(entry));

            // Create the class loader
            URL[] urls = {file.toURI().toURL()};
            PluginClassLoader classLoader = new PluginClassLoader(urls, getClass().getClassLoader(), descriptor);

            // Load the main class
            Class<?> mainClass = classLoader.loadClass(descriptor.main());
            if (!Plugin.class.isAssignableFrom(mainClass)) {
                logger.error("Main class does not implement Plugin: {}", descriptor.main());
                classLoader.close();
                return null;
            }

            // Instantiate the plugin
            Plugin plugin = (Plugin) mainClass.getDeclaredConstructor().newInstance();

            // Fire the plugin loading event
            if (eventManager != null) {
                PluginLoadingEvent loadingEvent = new PluginLoadingEvent(plugin);
                eventManager.fireEvent(loadingEvent);
            }

            // Create the plugin context
            PluginContextImpl context = new PluginContextImpl(
                    LoggerFactory.getLogger(descriptor.name()),
                    eventManager,
                    discordAPI,
                    new PluginConfiguration(descriptor.name()),
                    new File(pluginsFolder, descriptor.name()).getAbsolutePath(),
                    this,
                    classLoader,
                    languageManager,
                    dataStorageManager,
                    binaryStorageManager,
                    permissionManager
            );

            // Initialize the plugin
            plugin.setLifecycle(PluginLifecycle.LOADED);
            plugin.onLoad(context);

            // Store the classloader
            classLoaders.put(descriptor.name(), classLoader);

            // Store the descriptor
            pluginDescriptors.put(descriptor.name(), descriptor);

            // Fire the plugin loaded event
            if (eventManager != null) {
                PluginLoadedEvent loadedEvent = new PluginLoadedEvent(plugin);
                eventManager.fireEvent(loadedEvent);
            }

            return plugin;
        } catch (Exception e) {
            logger.error("Failed to load plugin: {}", jarFile, e);
            return null;
        }
    }

    @Override
    public boolean enablePlugin(Plugin plugin) {
        if (plugin.getLifecycle() != PluginLifecycle.LOADED) {
            logger.warn("Cannot enable plugin {}: not in LOADED state", plugin.getName());
            return false;
        }

        // Fire the plugin enable event - check if any listeners want to prevent enabling
        if (eventManager != null) {
            PluginEnableEvent enableEvent = new PluginEnableEvent(plugin);
            eventManager.fireEvent(enableEvent);

            if (enableEvent.isCancelled()) {
                logger.warn("Plugin {} enable was cancelled by a listener", plugin.getName());
                return false;
            }
        }

        try {
            // Follow the proper lifecycle
            executeLifecyclePhase(plugin, PluginLifecycle.PRE_ENABLING, Plugin::onPreEnable);
            executeLifecyclePhase(plugin, PluginLifecycle.ENABLING, Plugin::onEnable);
            executeLifecyclePhase(plugin, PluginLifecycle.POST_ENABLING, Plugin::onPostEnable);

            // Set to enabled
            PluginLifecycle oldLifecycle = plugin.getLifecycle();
            plugin.setLifecycle(PluginLifecycle.ENABLED);
            fireLifecycleChangeEvent(plugin, oldLifecycle, PluginLifecycle.ENABLED);

            // Fire the plugin enabled event
            if (eventManager != null) {
                PluginEnabledEvent enabledEvent = new PluginEnabledEvent(plugin);
                eventManager.fireEvent(enabledEvent);
            }

            logger.info("Plugin enabled: {} v{}", plugin.getName(), plugin.getVersion());
            return true;
        } catch (Exception e) {
            logger.error("Failed to enable plugin: {}", plugin.getName(), e);
            plugin.setLifecycle(PluginLifecycle.ERROR);
            return false;
        }
    }

    @Override
    public boolean disablePlugin(Plugin plugin) {
        if (plugin.getLifecycle() != PluginLifecycle.ENABLED) {
            logger.warn("Cannot disable plugin {}: not in ENABLED state", plugin.getName());
            return false;
        }

        // Fire the plugin disable event - check if any listeners want to prevent disabling
        if (eventManager != null) {
            PluginDisableEvent disableEvent = new PluginDisableEvent(plugin);
            eventManager.fireEvent(disableEvent);

            if (disableEvent.isCancelled()) {
                logger.warn("Plugin {} disable was cancelled by a listener", plugin.getName());
                return false;
            }
        }

        try {
            // Follow the proper lifecycle
            executeLifecyclePhase(plugin, PluginLifecycle.PRE_DISABLING, Plugin::onPreDisable);
            executeLifecyclePhase(plugin, PluginLifecycle.DISABLING, Plugin::onDisable);
            executeLifecyclePhase(plugin, PluginLifecycle.POST_DISABLING, Plugin::onPostDisable);

            // Cleanup
            if (eventManager != null) {
                eventManager.unregisterAll(plugin);
            }
            if (permissionManager != null) {
                permissionManager.unregisterPermissions(plugin);
            }

            // Set to disabled
            PluginLifecycle oldLifecycle = plugin.getLifecycle();
            plugin.setLifecycle(PluginLifecycle.DISABLED);
            fireLifecycleChangeEvent(plugin, oldLifecycle, PluginLifecycle.DISABLED);

            // Fire the plugin disabled event
            if (eventManager != null) {
                PluginDisabledEvent disabledEvent = new PluginDisabledEvent(plugin);
                eventManager.fireEvent(disabledEvent);
            }

            logger.info("Plugin disabled: {}", plugin.getName());
            return true;
        } catch (Exception e) {
            logger.error("Failed to disable plugin: {}", plugin.getName(), e);
            plugin.setLifecycle(PluginLifecycle.ERROR);
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

    /**
     * Loads all plugins from the plugins folder in dependency order.
     */
    public void loadPlugins() {
        // First, scan all plugins to get their metadata
        scanPlugins();

        // Resolve dependencies to determine load order
        DependencyResolver resolver = new DependencyResolver(pluginDescriptors);
        List<String> loadOrder = resolver.resolve();

        // Add missing dependencies to failed list
        failedPlugins.addAll(resolver.getMissingDependencies());

        // Load plugins in the determined order
        for (String pluginName : loadOrder) {
            if (failedPlugins.contains(pluginName)) {
                continue; // Skip failed plugins
            }

            String jarPath = pluginJarPaths.get(pluginName);
            try {
                Plugin plugin = loadPlugin(jarPath);
                if (plugin != null) {
                    plugins.put(pluginName, plugin);
                    logger.info("Loaded plugin: {} v{}", pluginName, plugin.getVersion());
                } else {
                    failedPlugins.add(pluginName);
                    logger.error("Failed to load plugin: {}", pluginName);
                }
            } catch (Exception e) {
                failedPlugins.add(pluginName);
                logger.error("Error loading plugin: {}", pluginName, e);
            }
        }

        // Log summary
        int successCount = plugins.size();
        int failedCount = failedPlugins.size();
        logger.info("Plugin loading complete: {} loaded successfully, {} failed", successCount, failedCount);
    }

    /**
     * Pre-enables all loaded plugins in dependency order.
     */
    public void preEnablePlugins() {
        logger.info("Pre-enabling plugins...");
        for (Plugin plugin : getPluginsInOrder()) {
            if (plugin.getLifecycle() == PluginLifecycle.LOADED && !failedPlugins.contains(plugin.getName())) {
                try {
                    executeLifecyclePhase(plugin, PluginLifecycle.PRE_ENABLING, Plugin::onPreEnable);
                } catch (Exception e) {
                    logger.error("Error pre-enabling plugin: {}", plugin.getName(), e);
                    failedPlugins.add(plugin.getName());
                    plugin.setLifecycle(PluginLifecycle.ERROR);
                }
            }
        }
    }

    /**
     * Enables all pre-enabled plugins in dependency order.
     */
    public void enablePlugins() {
        logger.info("Enabling plugins...");
        for (Plugin plugin : getPluginsInOrder()) {
            if (plugin.getLifecycle() == PluginLifecycle.PRE_ENABLING && !failedPlugins.contains(plugin.getName())) {
                try {
                    executeLifecyclePhase(plugin, PluginLifecycle.ENABLING, Plugin::onEnable);
                } catch (Exception e) {
                    logger.error("Error enabling plugin: {}", plugin.getName(), e);
                    failedPlugins.add(plugin.getName());
                    plugin.setLifecycle(PluginLifecycle.ERROR);
                }
            }
        }
    }

    /**
     * Post-enables all enabled plugins in dependency order.
     */
    public void postEnablePlugins() {
        logger.info("Post-enabling plugins...");
        for (Plugin plugin : getPluginsInOrder()) {
            if (plugin.getLifecycle() == PluginLifecycle.ENABLING && !failedPlugins.contains(plugin.getName())) {
                try {
                    executeLifecyclePhase(plugin, PluginLifecycle.POST_ENABLING, Plugin::onPostEnable);

                    // Mark as fully enabled
                    PluginLifecycle oldLifecycle = plugin.getLifecycle();
                    plugin.setLifecycle(PluginLifecycle.ENABLED);
                    fireLifecycleChangeEvent(plugin, oldLifecycle, PluginLifecycle.ENABLED);

                    // Fire enabled event
                    if (eventManager != null) {
                        eventManager.fireEvent(new PluginEnabledEvent(plugin));
                    }

                    logger.info("Plugin fully enabled: {} v{}", plugin.getName(), plugin.getVersion());
                } catch (Exception e) {
                    logger.error("Error post-enabling plugin: {}", plugin.getName(), e);
                    failedPlugins.add(plugin.getName());
                    plugin.setLifecycle(PluginLifecycle.ERROR);
                }
            }
        }
    }

    /**
     * Pre-disables all enabled plugins in reverse dependency order.
     */
    public void preDisablePlugins() {
        logger.info("Pre-disabling plugins...");
        for (Plugin plugin : getPluginsInReverseOrder()) {
            if (plugin.getLifecycle() == PluginLifecycle.ENABLED) {
                try {
                    executeLifecyclePhase(plugin, PluginLifecycle.PRE_DISABLING, Plugin::onPreDisable);
                } catch (Exception e) {
                    logger.error("Error pre-disabling plugin: {}", plugin.getName(), e);
                    // Continue anyway
                }
            }
        }
    }

    /**
     * Disables all pre-disabled plugins in reverse dependency order.
     */
    public void disablePlugins() {
        logger.info("Disabling plugins...");
        for (Plugin plugin : getPluginsInReverseOrder()) {
            if (plugin.getLifecycle() == PluginLifecycle.PRE_DISABLING) {
                try {
                    executeLifecyclePhase(plugin, PluginLifecycle.DISABLING, Plugin::onDisable);
                } catch (Exception e) {
                    logger.error("Error disabling plugin: {}", plugin.getName(), e);
                    // Continue anyway
                }
            }
        }
    }

    /**
     * Post-disables all disabled plugins in reverse dependency order.
     */
    public void postDisablePlugins() {
        logger.info("Post-disabling plugins...");
        for (Plugin plugin : getPluginsInReverseOrder()) {
            if (plugin.getLifecycle() == PluginLifecycle.DISABLING) {
                try {
                    executeLifecyclePhase(plugin, PluginLifecycle.POST_DISABLING, Plugin::onPostDisable);

                    // Mark as fully disabled
                    PluginLifecycle oldLifecycle = plugin.getLifecycle();
                    plugin.setLifecycle(PluginLifecycle.DISABLED);
                    fireLifecycleChangeEvent(plugin, oldLifecycle, PluginLifecycle.DISABLED);

                    // Fire disabled event
                    if (eventManager != null) {
                        eventManager.fireEvent(new PluginDisabledEvent(plugin));
                    }

                    logger.info("Plugin fully disabled: {}", plugin.getName());
                } catch (Exception e) {
                    logger.error("Error post-disabling plugin: {}", plugin.getName(), e);
                    // Continue anyway
                }
            }
        }

        // Cleanup resources
        cleanupResources();
    }

    /**
     * Reloads a specific plugin.
     *
     * @param pluginName the name of the plugin to reload
     * @return true if successful, false otherwise
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

        // Disable the plugin
        if (plugin.getLifecycle() == PluginLifecycle.ENABLED) {
            if (!disablePlugin(plugin)) {
                logger.error("Failed to disable plugin {} for reload", pluginName);
                return false;
            }
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

                // Enable the plugin
                if (!enablePlugin(newPlugin)) {
                    logger.error("Failed to enable reloaded plugin: {}", pluginName);
                    return false;
                }

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

    /**
     * Reloads all plugins completely.
     * This will disable all plugins, reconnect to Discord, and re-enable all plugins.
     *
     * @return true if reload was successful, false otherwise
     */
    public boolean reloadPlugins() {
        logger.info("Starting complete plugin reload...");
        long startTime = System.currentTimeMillis();

        // Save plugin state if needed (e.g. configs)
        if (dataStorageManager != null) {
            dataStorageManager.saveAll();
        }

        // Step 1: Disable plugins in proper sequence
        logger.info("Disabling plugins...");
        preDisablePlugins();
        disablePlugins();
        postDisablePlugins();

        // Step 2: Disconnect from Discord
        logger.info("Disconnecting from Discord...");
        try {
            discordAPI.setShutdownPresence();
            discordAPI.disconnect().join();
        } catch (Exception e) {
            logger.error("Failed to disconnect from Discord during reload", e);
            logger.warn("Continuing reload, but Discord reconnection may fail");
        }

        // Step 3: Clear event handlers and clean up resources
        logger.info("Cleaning up resources...");
        if (eventManager != null) {
            for (Plugin plugin : plugins.values()) {
                eventManager.unregisterAll(plugin);
            }
        }

        // Step 4: Reload plugins
        logger.info("Reloading plugins...");
        try {
            // Scan and load all plugins
            loadPlugins();
        } catch (Exception e) {
            logger.error("Error during plugin loading phase", e);
            // Continue anyway, some plugins might have loaded successfully
        }

        // Step 5: Pre-enable plugins (configure Discord)
        logger.info("Pre-enabling plugins...");
        preEnablePlugins();

        // Step 6: Reconnect to Discord
        logger.info("Reconnecting to Discord...");
        boolean discordConnected = false;
        try {
            discordAPI.connect().join();
            discordAPI.setStartupPresence();
            discordConnected = true;
        } catch (Exception e) {
            logger.error("Failed to reconnect to Discord after reload", e);
            logger.warn("Plugins requiring Discord features may not function properly");
        }

        // Step 7: Complete enabling plugins
        if (discordConnected) {
            logger.info("Enabling plugins...");
            try {
                enablePlugins();
                postEnablePlugins();

                // Set default presence
                discordAPI.setDefaultPresence();
            } catch (Exception e) {
                logger.error("Error during plugin enabling phase", e);
                return false;
            }
        } else {
            logger.error("Skipping plugin enable phase due to Discord connection failure");
            return false;
        }

        // Step 8: Calculate and log statistics
        int totalPlugins = plugins.size();
        int enabledPlugins = (int) plugins.values().stream()
                .filter(p -> p.getLifecycle() == PluginLifecycle.ENABLED)
                .count();
        int failedPlugins = totalPlugins - enabledPlugins;

        long endTime = System.currentTimeMillis();
        double reloadTime = (endTime - startTime) / 1000.0;

        logger.info("Plugin reload completed in {:.2f} seconds: {} total plugins, {} enabled, {} failed",
                reloadTime, totalPlugins, enabledPlugins, failedPlugins);

        return enabledPlugins > 0 && discordConnected;
    }

    /**
     * Scans plugin JARs to collect metadata without loading them.
     */
    private void scanPlugins() {
        logger.info("Scanning plugins in {}", pluginsFolder.getAbsolutePath());

        // Clear previous state
        pluginDescriptors.clear();
        pluginJarPaths.clear();
        failedPlugins.clear();

        File[] files = pluginsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null) {
            logger.warn("Failed to list files in plugins folder");
            return;
        }

        for (File file : files) {
            try (JarFile jar = new JarFile(file)) {
                // Look for plugin.yml
                JarEntry entry = jar.getJarEntry("plugin.yml");
                if (entry == null) {
                    logger.error("Plugin does not contain plugin.yml: {}", file.getName());
                    continue;
                }

                // Parse plugin.yml
                PluginDescriptor descriptor = PluginDescriptor.fromYaml(jar.getInputStream(entry));

                // Store metadata
                pluginDescriptors.put(descriptor.name(), descriptor);
                pluginJarPaths.put(descriptor.name(), file.getAbsolutePath());

                logger.debug("Scanned plugin: {} v{}", descriptor.name(), descriptor.version());
            } catch (Exception e) {
                logger.error("Failed to scan plugin: {}", file.getName(), e);
            }
        }

        logger.info("Scanned {} plugins", pluginDescriptors.size());
    }

    /**
     * Executes a specific lifecycle phase for a plugin.
     *
     * @param plugin       the plugin
     * @param newLifecycle the new lifecycle state
     * @param action       the action to perform
     */
    private void executeLifecyclePhase(Plugin plugin, PluginLifecycle newLifecycle, PluginAction action) {
        PluginLifecycle oldLifecycle = plugin.getLifecycle();
        plugin.setLifecycle(newLifecycle);
        fireLifecycleChangeEvent(plugin, oldLifecycle, newLifecycle);
        action.execute(plugin);
    }

    /**
     * Fires a lifecycle change event.
     *
     * @param plugin       the plugin
     * @param oldLifecycle the old lifecycle state
     * @param newLifecycle the new lifecycle state
     */
    private void fireLifecycleChangeEvent(Plugin plugin, PluginLifecycle oldLifecycle, PluginLifecycle newLifecycle) {
        if (eventManager != null) {
            PluginLifecycleChangeEvent event = new PluginLifecycleChangeEvent(plugin, oldLifecycle, newLifecycle);
            eventManager.fireEvent(event);
        }
    }

    /**
     * Gets plugins in dependency order.
     *
     * @return ordered list of plugins
     */
    private List<Plugin> getPluginsInOrder() {
        // Create a map of plugin names to plugin instances
        Map<String, Plugin> pluginMap = new HashMap<>();
        for (Plugin plugin : plugins.values()) {
            pluginMap.put(plugin.getName(), plugin);
        }

        // Resolve dependencies
        DependencyResolver resolver = new DependencyResolver(pluginDescriptors);
        List<String> orderedNames = resolver.resolve();

        // Create ordered list of plugin instances
        List<Plugin> orderedPlugins = new ArrayList<>();
        for (String name : orderedNames) {
            Plugin plugin = pluginMap.get(name);
            if (plugin != null) {
                orderedPlugins.add(plugin);
            }
        }

        return orderedPlugins;
    }

    /**
     * Gets plugins in reverse dependency order.
     *
     * @return ordered list of plugins
     */
    private List<Plugin> getPluginsInReverseOrder() {
        List<Plugin> orderedPlugins = getPluginsInOrder();
        Collections.reverse(orderedPlugins);
        return orderedPlugins;
    }

    /**
     * Cleans up resources when shutting down.
     */
    private void cleanupResources() {
        // Unregister all event handlers
        if (eventManager != null) {
            for (Plugin plugin : plugins.values()) {
                eventManager.unregisterAll(plugin);
            }
        }

        // Close class loaders
        for (PluginClassLoader classLoader : classLoaders.values()) {
            try {
                classLoader.close();
            } catch (IOException e) {
                logger.error("Error closing classloader", e);
            }
        }

        // Clear collections
        plugins.clear();
        classLoaders.clear();
        pluginDescriptors.clear();
        pluginJarPaths.clear();
        failedPlugins.clear();
    }

    @Override
    public void close() {
        preDisablePlugins();
        disablePlugins();
        postDisablePlugins();
    }

    /**
     * Functional interface for plugin lifecycle actions.
     */
    @FunctionalInterface
    private interface PluginAction {
        void execute(Plugin plugin);
    }
}
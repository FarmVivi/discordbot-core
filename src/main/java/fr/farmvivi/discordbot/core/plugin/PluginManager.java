package fr.farmvivi.discordbot.core.plugin;

import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.api.permissions.PermissionManager;
import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import fr.farmvivi.discordbot.core.api.plugin.PluginLoader;
import fr.farmvivi.discordbot.core.api.plugin.PluginStatus;
import fr.farmvivi.discordbot.core.api.plugin.events.*;
import fr.farmvivi.discordbot.core.api.storage.DataStorageManager;
import fr.farmvivi.discordbot.core.api.storage.binary.BinaryStorageManager;
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
    private final Map<String, PluginDescriptionFile> pluginDescriptions = new HashMap<>();
    private final Map<String, String> pluginJarPaths = new HashMap<>();
    private final Set<String> failedPlugins = new HashSet<>();
    private final File pluginsFolder;
    private final EventManager eventManager;
    private final DiscordAPI discordAPI;
    private final LanguageManager languageManager;
    private final DataStorageManager dataStorageManager;
    private final BinaryStorageManager binaryStorageManager;
    private final PermissionManager permissionManager;

    //--------------------------------------------------------------------
    // CONSTRUCTORS AND INITIALIZATION
    //--------------------------------------------------------------------

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
    public PluginManager(File pluginsFolder, EventManager eventManager, DiscordAPI discordAPI,
                         LanguageManager languageManager, DataStorageManager dataStorageManager,
                         BinaryStorageManager binaryStorageManager, PermissionManager permissionManager) {
        this.pluginsFolder = pluginsFolder;
        this.eventManager = eventManager;
        this.discordAPI = discordAPI;
        this.languageManager = languageManager;
        this.dataStorageManager = dataStorageManager;
        this.binaryStorageManager = binaryStorageManager;
        this.permissionManager = permissionManager;

        if (!pluginsFolder.exists()) {
            pluginsFolder.mkdirs();
        }
    }

    //--------------------------------------------------------------------
    // PLUGIN LOADER INTERFACE IMPLEMENTATION
    //--------------------------------------------------------------------

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

            // Fire the plugin loading event
            if (eventManager != null) {
                PluginLoadingEvent loadingEvent = new PluginLoadingEvent(plugin);
                eventManager.fireEvent(loadingEvent);
            }

            // Create the plugin context
            PluginContextImpl context = new PluginContextImpl(
                    LoggerFactory.getLogger(description.getName()),
                    eventManager,
                    discordAPI,
                    new PluginConfiguration(description.getName()),
                    new File(pluginsFolder, description.getName()).getAbsolutePath(),
                    this,
                    classLoader,
                    languageManager,
                    dataStorageManager,
                    binaryStorageManager,
                    permissionManager
            );

            // Initialize the plugin
            plugin.onLoad(context);

            // Store the classloader
            classLoaders.put(description.getName(), classLoader);

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
        if (plugin.getStatus() != PluginStatus.LOADED) {
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
            // Follow the proper sequence
            PluginStatus oldStatus = plugin.getStatus();

            // Pre-enable phase
            plugin.setStatus(PluginStatus.PRE_ENABLE);
            firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.PRE_ENABLE);
            plugin.onPreEnable();

            // Enable phase
            oldStatus = plugin.getStatus();
            plugin.setStatus(PluginStatus.ENABLE);
            firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.ENABLE);
            plugin.onEnable();

            // Post-enable phase
            oldStatus = plugin.getStatus();
            plugin.setStatus(PluginStatus.POST_ENABLE);
            firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.POST_ENABLE);
            plugin.onPostEnable();

            // Mark as fully enabled
            oldStatus = plugin.getStatus();
            plugin.setStatus(PluginStatus.ENABLED);
            firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.ENABLED);

            // Fire the plugin enabled event
            if (eventManager != null) {
                PluginEnabledEvent enabledEvent = new PluginEnabledEvent(plugin);
                eventManager.fireEvent(enabledEvent);
            }

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
            // Follow the proper sequence
            PluginStatus oldStatus = plugin.getStatus();

            // Pre-disable phase
            plugin.setStatus(PluginStatus.PRE_DISABLE);
            firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.PRE_DISABLE);
            plugin.onPreDisable();

            // Disable phase
            oldStatus = plugin.getStatus();
            plugin.setStatus(PluginStatus.DISABLE);
            firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.DISABLE);
            plugin.onDisable();

            // Post-disable phase
            oldStatus = plugin.getStatus();
            plugin.setStatus(PluginStatus.POST_DISABLE);
            firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.POST_DISABLE);
            plugin.onPostDisable();

            // Unregister events and permissions
            if (eventManager != null) {
                eventManager.unregisterAll(plugin);
            }
            if (permissionManager != null) {
                permissionManager.unregisterPermissions(plugin);
            }

            // Mark as fully disabled
            oldStatus = plugin.getStatus();
            plugin.setStatus(PluginStatus.DISABLED);
            firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.DISABLED);

            // Fire the plugin disabled event
            if (eventManager != null) {
                PluginDisabledEvent disabledEvent = new PluginDisabledEvent(plugin);
                eventManager.fireEvent(disabledEvent);
            }

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

    //--------------------------------------------------------------------
    // PLUGIN DISCOVERY AND LOADING
    //--------------------------------------------------------------------

    /**
     * Scans plugin JARs to collect metadata without loading them.
     */
    private void scanPlugins() {
        logger.info("Scanning plugins in {}", pluginsFolder.getAbsolutePath());

        // Clear previous state
        pluginDescriptions.clear();
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

                // Parse plugin.yml to get the plugin metadata
                PluginDescriptionFile description = new PluginDescriptionFile(jar.getInputStream(entry));
                String pluginName = description.getName();

                // Store metadata
                pluginDescriptions.put(pluginName, description);
                pluginJarPaths.put(pluginName, file.getAbsolutePath());

                logger.debug("Scanned plugin: {} v{}", pluginName, description.getVersion());
            } catch (Exception e) {
                logger.error("Failed to scan plugin: {}", file.getName(), e);
            }
        }

        logger.info("Scanned {} plugins", pluginDescriptions.size());
    }

    /**
     * Loads all plugins from the plugins folder in dependency order.
     */
    public void loadPlugins() {
        // First, scan all plugins to get their metadata
        scanPlugins();

        // Resolve dependencies to determine load order
        PluginDependencyResolver resolver = new PluginDependencyResolver(pluginDescriptions);
        List<String> loadOrder = resolver.resolve();

        // Log plugins with issues
        Set<String> missingDeps = resolver.getMissingDependencies();
        if (!missingDeps.isEmpty()) {
            for (String plugin : missingDeps) {
                failedPlugins.add(plugin);
                logger.warn("Plugin {} has missing dependencies and won't be loaded", plugin);
            }
        }

        Set<String> circularDeps = resolver.getCircularDependencies();
        if (!circularDeps.isEmpty()) {
            logger.warn("Detected circular dependencies among plugins: {}", circularDeps);
            logger.warn("These plugins will be loaded but might not function correctly");
        }

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

    //--------------------------------------------------------------------
    // PLUGIN LIFECYCLE MANAGEMENT
    //--------------------------------------------------------------------

    /**
     * Pre-enables all loaded plugins in dependency order.
     * This is when plugins can configure the JDABuilder before Discord connection.
     */
    public void preEnablePlugins() {
        logger.info("Pre-enabling plugins...");

        // Get plugins in dependency order
        List<String> orderedPluginNames = getPluginsInDependencyOrder();

        for (String pluginName : orderedPluginNames) {
            Plugin plugin = plugins.get(pluginName);
            if (plugin == null || plugin.getStatus() != PluginStatus.LOADED) {
                continue;
            }

            // Verify dependencies for extra safety
            if (!hasAllDependenciesLoaded(pluginName)) {
                List<String> missing = getMissingDependencies(pluginName);
                logger.error("Cannot pre-enable plugin {}: missing dependencies {}", pluginName, missing);
                failedPlugins.add(pluginName);
                continue;
            }

            try {
                PluginStatus oldStatus = plugin.getStatus();
                plugin.setStatus(PluginStatus.PRE_ENABLE);
                firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.PRE_ENABLE);
                plugin.onPreEnable();
                logger.debug("Pre-enabled plugin: {}", pluginName);
            } catch (Exception e) {
                logger.error("Error during pre-enable of plugin: {}", pluginName, e);
                plugin.setStatus(PluginStatus.LOADED);
                failedPlugins.add(pluginName);
            }
        }

        logger.info("Pre-enabled {} plugins",
                orderedPluginNames.size() - failedPlugins.size());
    }

    /**
     * Enables all pre-enabled plugins in dependency order.
     * This is called after Discord connection is established.
     */
    public void enablePlugins() {
        logger.info("Enabling plugins...");

        // Get plugins in dependency order
        List<String> orderedPluginNames = getPluginsInDependencyOrder();

        for (String pluginName : orderedPluginNames) {
            Plugin plugin = plugins.get(pluginName);
            if (plugin == null ||
                    plugin.getStatus() != PluginStatus.PRE_ENABLE ||
                    failedPlugins.contains(pluginName)) {
                continue;
            }

            // Fire the plugin enable event
            if (eventManager != null) {
                PluginEnableEvent enableEvent = new PluginEnableEvent(plugin);
                eventManager.fireEvent(enableEvent);

                if (enableEvent.isCancelled()) {
                    logger.warn("Plugin {} enable cancelled by a listener", pluginName);
                    failedPlugins.add(pluginName);
                    continue;
                }
            }

            try {
                PluginStatus oldStatus = plugin.getStatus();
                plugin.setStatus(PluginStatus.ENABLE);
                firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.ENABLE);
                plugin.onEnable();
                logger.debug("Enabled plugin: {}", pluginName);
            } catch (Exception e) {
                logger.error("Error during enable of plugin: {}", pluginName, e);
                plugin.setStatus(PluginStatus.PRE_ENABLE);
                failedPlugins.add(pluginName);
            }
        }

        logger.info("Enabled {} plugins",
                orderedPluginNames.size() - failedPlugins.size());
    }

    /**
     * Post-enables all enabled plugins in dependency order.
     */
    public void postEnablePlugins() {
        logger.info("Post-enabling plugins...");

        // Get plugins in dependency order
        List<String> orderedPluginNames = getPluginsInDependencyOrder();

        for (String pluginName : orderedPluginNames) {
            Plugin plugin = plugins.get(pluginName);
            if (plugin == null ||
                    plugin.getStatus() != PluginStatus.ENABLE ||
                    failedPlugins.contains(pluginName)) {
                continue;
            }

            try {
                PluginStatus oldStatus = plugin.getStatus();
                plugin.setStatus(PluginStatus.POST_ENABLE);
                firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.POST_ENABLE);
                plugin.onPostEnable();
                logger.debug("Post-enabled plugin: {}", pluginName);
            } catch (Exception e) {
                logger.error("Error during post-enable of plugin: {}", pluginName, e);
                failedPlugins.add(pluginName);
            }
        }

        // Final pass: mark as enabled
        for (String pluginName : orderedPluginNames) {
            Plugin plugin = plugins.get(pluginName);
            if (plugin == null ||
                    plugin.getStatus() != PluginStatus.POST_ENABLE ||
                    failedPlugins.contains(pluginName)) {
                continue;
            }

            try {
                PluginStatus oldStatus = plugin.getStatus();
                plugin.setStatus(PluginStatus.ENABLED);
                firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.ENABLED);

                // Fire the plugin enabled event
                if (eventManager != null) {
                    PluginEnabledEvent enabledEvent = new PluginEnabledEvent(plugin);
                    eventManager.fireEvent(enabledEvent);
                }

                logger.info("Plugin fully enabled: {} v{}", pluginName, plugin.getVersion());
            } catch (Exception e) {
                logger.error("Error during final enable of plugin: {}", pluginName, e);
                failedPlugins.add(pluginName);
            }
        }

        // Log summary
        int enabledCount = (int) plugins.values().stream()
                .filter(p -> p.getStatus() == PluginStatus.ENABLED)
                .count();
        logger.info("Post-enable complete: {} plugins fully enabled", enabledCount);
    }

    /**
     * Pre-disables all enabled plugins in reverse dependency order.
     */
    public void preDisablePlugins() {
        logger.info("Pre-disabling plugins...");

        // Get plugins in reverse dependency order (dependents first)
        List<String> orderedPluginNames = getPluginsInReverseDependencyOrder();

        for (String pluginName : orderedPluginNames) {
            Plugin plugin = plugins.get(pluginName);
            if (plugin == null || plugin.getStatus() != PluginStatus.ENABLED) {
                continue;
            }

            // Fire the plugin disable event
            if (eventManager != null) {
                PluginDisableEvent disableEvent = new PluginDisableEvent(plugin);
                eventManager.fireEvent(disableEvent);

                if (disableEvent.isCancelled()) {
                    logger.warn("Plugin {} disable cancelled by a listener", pluginName);
                    continue;
                }
            }

            try {
                PluginStatus oldStatus = plugin.getStatus();
                plugin.setStatus(PluginStatus.PRE_DISABLE);
                firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.PRE_DISABLE);
                plugin.onPreDisable();
                logger.debug("Pre-disabled plugin: {}", pluginName);
            } catch (Exception e) {
                logger.error("Error during pre-disable of plugin: {}", pluginName, e);
                // Continue with disabling anyway
            }
        }
    }

    /**
     * Disables all pre-disabled plugins in reverse dependency order.
     */
    public void disablePlugins() {
        logger.info("Disabling plugins...");

        // Get plugins in reverse dependency order
        List<String> orderedPluginNames = getPluginsInReverseDependencyOrder();

        // Call onDisable
        for (String pluginName : orderedPluginNames) {
            Plugin plugin = plugins.get(pluginName);
            if (plugin == null || plugin.getStatus() != PluginStatus.PRE_DISABLE) {
                continue;
            }

            try {
                PluginStatus oldStatus = plugin.getStatus();
                plugin.setStatus(PluginStatus.DISABLE);
                firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.DISABLE);
                plugin.onDisable();
                logger.debug("Disabled plugin: {}", pluginName);
            } catch (Exception e) {
                logger.error("Error during disable of plugin: {}", pluginName, e);
                // Continue with disabling anyway
            }
        }
    }

    /**
     * Post-disables all disabled plugins in reverse dependency order.
     */
    public void postDisablePlugins() {
        logger.info("Post-disabling plugins...");

        // Get plugins in reverse dependency order
        List<String> orderedPluginNames = getPluginsInReverseDependencyOrder();

        // Post-disable phase
        for (String pluginName : orderedPluginNames) {
            Plugin plugin = plugins.get(pluginName);
            if (plugin == null || plugin.getStatus() != PluginStatus.DISABLE) {
                continue;
            }

            try {
                PluginStatus oldStatus = plugin.getStatus();
                plugin.setStatus(PluginStatus.POST_DISABLE);
                firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.POST_DISABLE);
                plugin.onPostDisable();
                logger.debug("Post-disabled plugin: {}", pluginName);
            } catch (Exception e) {
                logger.error("Error during post-disable of plugin: {}", pluginName, e);
                // Continue with disabling anyway
            }
        }

        // Final pass: mark as disabled and clean up
        for (String pluginName : orderedPluginNames) {
            Plugin plugin = plugins.get(pluginName);
            if (plugin == null || plugin.getStatus() != PluginStatus.POST_DISABLE) {
                continue;
            }

            try {
                PluginStatus oldStatus = plugin.getStatus();
                plugin.setStatus(PluginStatus.DISABLED);
                firePluginStatusChangeEvent(plugin, oldStatus, PluginStatus.DISABLED);

                // Fire the plugin disabled event
                if (eventManager != null) {
                    PluginDisabledEvent disabledEvent = new PluginDisabledEvent(plugin);
                    eventManager.fireEvent(disabledEvent);
                }

                logger.info("Plugin fully disabled: {}", pluginName);
            } catch (Exception e) {
                logger.error("Error during final disable of plugin: {}", pluginName, e);
                // Continue with disabling anyway
            }

            // Unregister event types for this plugin
            if (eventManager != null) {
                eventManager.unregisterEventTypes(plugin);
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
        pluginDescriptions.clear();
        pluginJarPaths.clear();
        failedPlugins.clear();
    }

    //--------------------------------------------------------------------
    // PLUGIN RELOAD OPERATIONS
    //--------------------------------------------------------------------

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

    //--------------------------------------------------------------------
    // DEPENDENCY MANAGEMENT
    //--------------------------------------------------------------------

    /**
     * Gets a list of plugin names in dependency order.
     *
     * @return list of plugin names
     */
    private List<String> getPluginsInDependencyOrder() {
        // For already loaded plugins, we can use the current order
        // This helps maintain consistency if this method is called multiple times
        List<String> pluginNames = new ArrayList<>(plugins.keySet());

        // Create a simplified dependency graph
        Map<String, Set<String>> dependencyGraph = new HashMap<>();
        for (String pluginName : pluginNames) {
            Set<String> dependencies = new HashSet<>();
            PluginDescriptionFile desc = pluginDescriptions.get(pluginName);
            if (desc != null) {
                dependencies.addAll(desc.getDependencies());
            }
            dependencyGraph.put(pluginName, dependencies);
        }

        // Perform a topological sort
        List<String> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (String pluginName : pluginNames) {
            if (!visited.contains(pluginName)) {
                topologicalVisit(pluginName, dependencyGraph, visited, visiting, result);
            }
        }

        // Reverse to get dependency order (dependencies first)
        Collections.reverse(result);
        return result;
    }

    /**
     * Gets a list of plugin names in reverse dependency order (dependents before dependencies).
     *
     * @return list of plugin names
     */
    private List<String> getPluginsInReverseDependencyOrder() {
        // Get the dependency order first
        List<String> dependencyOrder = getPluginsInDependencyOrder();

        // Reverse it to get the reverse dependency order
        List<String> reverseDependencyOrder = new ArrayList<>(dependencyOrder);
        Collections.reverse(reverseDependencyOrder);

        return reverseDependencyOrder;
    }

    /**
     * Helper method for topological sort.
     */
    private void topologicalVisit(
            String pluginName,
            Map<String, Set<String>> graph,
            Set<String> visited,
            Set<String> visiting,
            List<String> result
    ) {
        visiting.add(pluginName);

        Set<String> dependencies = graph.get(pluginName);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                if (!visited.contains(dependency) && plugins.containsKey(dependency)) {
                    if (visiting.contains(dependency)) {
                        // Circular dependency detected, but continue anyway
                        logger.warn("Circular dependency detected: {} <-> {}",
                                pluginName, dependency);
                        continue;
                    }
                    topologicalVisit(dependency, graph, visited, visiting, result);
                }
            }
        }

        visiting.remove(pluginName);
        visited.add(pluginName);
        result.add(pluginName);
    }

    /**
     * Checks if a plugin has all its dependencies loaded.
     *
     * @param pluginName the name of the plugin
     * @return true if all dependencies are loaded, false otherwise
     */
    public boolean hasAllDependenciesLoaded(String pluginName) {
        PluginDescriptionFile description = pluginDescriptions.get(pluginName);
        if (description == null) {
            return false;
        }

        // Check hard dependencies
        for (String dependency : description.getDependencies()) {
            if (!plugins.containsKey(dependency) || failedPlugins.contains(dependency)) {
                logger.warn("Plugin {} depends on {}, which is not loaded", pluginName, dependency);
                return false;
            }
        }

        return true;
    }

    //--------------------------------------------------------------------
    // UTILITY METHODS
    //--------------------------------------------------------------------

    /**
     * Gets the names of all missing dependencies for a plugin.
     *
     * @param pluginName the name of the plugin
     * @return list of missing dependency names
     */
    public List<String> getMissingDependencies(String pluginName) {
        PluginDescriptionFile description = pluginDescriptions.get(pluginName);
        if (description == null) {
            return List.of();
        }

        List<String> missing = new ArrayList<>();
        for (String dependency : description.getDependencies()) {
            if (!plugins.containsKey(dependency) || failedPlugins.contains(dependency)) {
                missing.add(dependency);
            }
        }

        return missing;
    }

    /**
     * Gets a set of all plugins that failed to load.
     *
     * @return set of failed plugin names
     */
    public Set<String> getFailedPlugins() {
        return Collections.unmodifiableSet(failedPlugins);
    }

    /**
     * Gets the dependencies of a plugin.
     *
     * @param pluginName the name of the plugin
     * @return list of dependency names, or empty list if the plugin doesn't exist
     */
    public List<String> getPluginDependencies(String pluginName) {
        PluginDescriptionFile description = pluginDescriptions.get(pluginName);
        if (description == null) {
            return List.of();
        }

        return description.getDependencies();
    }

    /**
     * Gets the soft dependencies of a plugin.
     *
     * @param pluginName the name of the plugin
     * @return list of soft dependency names, or empty list if the plugin doesn't exist
     */
    public List<String> getPluginSoftDependencies(String pluginName) {
        PluginDescriptionFile description = pluginDescriptions.get(pluginName);
        if (description == null) {
            return List.of();
        }

        return description.getSoftDependencies();
    }

    /**
     * Fires a plugin status change event.
     *
     * @param plugin    the plugin
     * @param oldStatus the old status
     * @param newStatus the new status
     */
    private void firePluginStatusChangeEvent(Plugin plugin, PluginStatus oldStatus, PluginStatus newStatus) {
        if (eventManager != null) {
            PluginStatusChangeEvent event = new PluginStatusChangeEvent(plugin, oldStatus, newStatus);
            eventManager.fireEvent(event);
        }
    }
}
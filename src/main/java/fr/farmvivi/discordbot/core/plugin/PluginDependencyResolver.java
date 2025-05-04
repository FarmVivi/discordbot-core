package fr.farmvivi.discordbot.core.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Resolves plugin dependencies to determine the correct loading order.
 */
public class PluginDependencyResolver {
    private static final Logger logger = LoggerFactory.getLogger(PluginDependencyResolver.class);

    private final Map<String, PluginDescriptionFile> pluginDescriptions;
    private final Map<String, Set<String>> dependencyGraph = new HashMap<>();
    private final Map<String, Set<String>> softDependencyGraph = new HashMap<>();
    private final List<String> loadOrder = new ArrayList<>();
    private final Set<String> loadedPlugins = new HashSet<>();
    private final Set<String> missingDependencies = new HashSet<>();
    private final Set<String> circularDependencies = new HashSet<>();

    /**
     * Creates a new plugin dependency resolver.
     *
     * @param pluginDescriptions the map of plugin names to their description files
     */
    public PluginDependencyResolver(Map<String, PluginDescriptionFile> pluginDescriptions) {
        this.pluginDescriptions = pluginDescriptions;
    }

    /**
     * Resolves dependencies and returns the sorted list of plugin names in the order they should be loaded.
     *
     * @return the sorted list of plugin names
     */
    public List<String> resolve() {
        // Clear previous state
        dependencyGraph.clear();
        softDependencyGraph.clear();
        loadOrder.clear();
        loadedPlugins.clear();
        missingDependencies.clear();
        circularDependencies.clear();

        // Build the dependency graph
        buildDependencyGraph();

        // Perform the topological sort
        for (String pluginName : pluginDescriptions.keySet()) {
            if (!loadedPlugins.contains(pluginName)) {
                resolveDependenciesFor(pluginName, new HashSet<>());
            }
        }

        // Log results
        if (!missingDependencies.isEmpty()) {
            logger.warn("The following plugins have missing dependencies: {}", missingDependencies);
        }

        if (!circularDependencies.isEmpty()) {
            logger.warn("Circular dependencies detected among these plugins: {}", circularDependencies);
        }

        logger.info("Resolved plugin load order: {}", loadOrder);
        return loadOrder;
    }

    /**
     * Gets the set of plugins with missing dependencies.
     *
     * @return the set of plugin names
     */
    public Set<String> getMissingDependencies() {
        return Collections.unmodifiableSet(missingDependencies);
    }

    /**
     * Gets the set of plugins involved in circular dependencies.
     *
     * @return the set of plugin names
     */
    public Set<String> getCircularDependencies() {
        return Collections.unmodifiableSet(circularDependencies);
    }

    /**
     * Builds the dependency graphs for hard and soft dependencies.
     */
    private void buildDependencyGraph() {
        for (Map.Entry<String, PluginDescriptionFile> entry : pluginDescriptions.entrySet()) {
            String pluginName = entry.getKey();
            PluginDescriptionFile description = entry.getValue();

            // Process hard dependencies
            Set<String> dependencies = new HashSet<>();
            for (String dependency : description.getDependencies()) {
                dependencies.add(dependency);
            }
            dependencyGraph.put(pluginName, dependencies);

            // Process soft dependencies
            Set<String> softDependencies = new HashSet<>();
            for (String softDependency : description.getSoftDependencies()) {
                softDependencies.add(softDependency);
            }
            softDependencyGraph.put(pluginName, softDependencies);
        }
    }

    /**
     * Recursively resolves dependencies for a plugin using depth-first search.
     *
     * @param pluginName the name of the plugin
     * @param visiting   the set of plugins currently being visited (used to detect cycles)
     * @return true if the plugin was loaded, false otherwise
     */
    private boolean resolveDependenciesFor(String pluginName, Set<String> visiting) {
        // Skip if already loaded
        if (loadedPlugins.contains(pluginName)) {
            return true;
        }

        // Check if the plugin exists
        if (!pluginDescriptions.containsKey(pluginName)) {
            return false;
        }

        // Check for circular dependency
        if (visiting.contains(pluginName)) {
            circularDependencies.addAll(visiting);
            circularDependencies.add(pluginName);
            logger.error("Circular dependency detected: {} is part of cycle {}", pluginName, visiting);
            return false;
        }

        // Mark as visiting
        visiting.add(pluginName);

        // First, process hard dependencies
        boolean missingDependency = false;
        Set<String> dependencies = dependencyGraph.get(pluginName);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                // Skip if already loaded
                if (loadedPlugins.contains(dependency)) {
                    continue;
                }

                // Check if dependency exists
                if (!pluginDescriptions.containsKey(dependency)) {
                    missingDependency = true;
                    missingDependencies.add(pluginName);
                    logger.error("Plugin {} depends on {} which is not installed", pluginName, dependency);
                    continue;
                }

                // Resolve dependency recursively
                if (!resolveDependenciesFor(dependency, new HashSet<>(visiting))) {
                    missingDependency = true;
                }
            }
        }

        // If any hard dependency is missing, skip this plugin
        if (missingDependency) {
            visiting.remove(pluginName);
            return false;
        }

        // Next, process soft dependencies (we don't fail if they're missing)
        Set<String> softDependencies = softDependencyGraph.get(pluginName);
        if (softDependencies != null) {
            for (String softDependency : softDependencies) {
                // Skip if already loaded or doesn't exist
                if (loadedPlugins.contains(softDependency) || !pluginDescriptions.containsKey(softDependency)) {
                    continue;
                }

                // Try to resolve soft dependency
                resolveDependenciesFor(softDependency, new HashSet<>(visiting));
            }
        }

        // Add to load order
        loadOrder.add(pluginName);
        loadedPlugins.add(pluginName);
        visiting.remove(pluginName);
        return true;
    }
}
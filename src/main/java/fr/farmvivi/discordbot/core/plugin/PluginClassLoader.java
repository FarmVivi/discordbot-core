package fr.farmvivi.discordbot.core.plugin;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Custom class loader for loading plugin classes.
 * This class loader has isolated dependencies for each plugin
 * while still allowing them to access the core API.
 */
public class PluginClassLoader extends URLClassLoader {
    private static final Set<String> CORE_PACKAGES = Set.of(
            "fr.farmvivi.discordbot.core",
            "org.slf4j",
            "org.yaml.snakeyaml",
            "net.dv8tion.jda.api"
    );

    private final Set<String> seenPackages = new CopyOnWriteArraySet<>();

    /**
     * Creates a new plugin class loader.
     *
     * @param urls   the URLs to load classes from
     * @param parent the parent class loader
     */
    public PluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Check if the class has already been loaded
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        // Get the package name
        String packageName = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : "";

        // Core packages are always loaded by the parent class loader
        for (String corePackage : CORE_PACKAGES) {
            if (packageName.startsWith(corePackage)) {
                return super.loadClass(name, resolve);
            }
        }

        try {
            // Try to load the class from this class loader first
            loadedClass = findClass(name);
            seenPackages.add(packageName);
        } catch (ClassNotFoundException ignored) {
            // If this class loader doesn't have the class, try the parent
            // if we've seen this package before
            if (seenPackages.contains(packageName)) {
                try {
                    loadedClass = super.loadClass(name, false);
                } catch (ClassNotFoundException ignored2) {
                    // Ignore
                }
            }

            // If we still haven't found the class, try the parent
            if (loadedClass == null) {
                loadedClass = super.loadClass(name, false);
            }
        }

        if (resolve) {
            resolveClass(loadedClass);
        }

        return loadedClass;
    }

    @Override
    public void close() throws IOException {
        super.close();
        seenPackages.clear();
    }
}
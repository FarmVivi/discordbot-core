package fr.farmvivi.fluxcord.api.language;

import fr.farmvivi.fluxcord.api.plugin.Plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Locale;

/**
 * A simplified language manager for plugins.
 * This class wraps the core language manager and handles namespace prefixing automatically.
 */
public class PluginLanguageAdapter {
    private static final Logger logger = LoggerFactory.getLogger(PluginLanguageAdapter.class);
    private final LanguageManager languageManager;
    private final String namespace;
    private final Plugin plugin;

    /**
     * Creates a new plugin language manager.
     *
     * @param plugin          the plugin
     * @param languageManager the core language manager
     */
    public PluginLanguageAdapter(Plugin plugin, LanguageManager languageManager) {
        this.plugin = plugin;
        this.languageManager = languageManager;
        this.namespace = plugin.getName().toLowerCase();

        // Register the namespace automatically
        boolean registered = languageManager.registerNamespace(namespace);
        if (registered) {
            logger.debug("Registered plugin namespace '{}' for plugin {}", namespace, plugin.getName());
        } else {
            logger.debug("Plugin namespace '{}' already registered for plugin {}", namespace, plugin.getName());
        }
    }

    /**
     * Gets a translated string for the specified key in the default language.
     * The namespace is automatically added.
     *
     * @param key the translation key (without namespace prefix)
     * @return the translated string, or the key itself if not found
     */
    public String getString(String key) {
        String fullKey = namespace + ":" + key;
        if (logger.isDebugEnabled()) {
            logger.debug("[{}] getString key='{}' -> '{}'", plugin.getName(), key, fullKey);
        }
        return languageManager.getString(fullKey);
    }

    /**
     * Gets a translated string with placeholder replacements.
     * The namespace is automatically added.
     *
     * @param key  the translation key (without namespace prefix)
     * @param args the arguments to replace placeholders
     * @return the translated string with replacements
     */
    public String getString(String key, Object... args) {
        String fullKey = namespace + ":" + key;
        if (logger.isDebugEnabled()) {
            logger.debug("[{}] getString key='{}' with {} arg(s) -> '{}'", plugin.getName(), key, args == null ? 0 : args.length, fullKey);
        }
        return languageManager.getString(fullKey, args);
    }

    /**
     * Gets a translated string for the specified locale.
     * The namespace is automatically added.
     *
     * @param locale the locale
     * @param key    the translation key (without namespace prefix)
     * @return the translated string, or the key itself if not found
     */
    public String getString(Locale locale, String key) {
        String fullKey = namespace + ":" + key;
        if (logger.isDebugEnabled()) {
            logger.debug("[{}] getString locale={}, key='{}' -> '{}'", plugin.getName(), locale.toLanguageTag(), key, fullKey);
        }
        return languageManager.getString(locale, fullKey);
    }

    /**
     * Gets a translated string for the specified locale with placeholder replacements.
     * The namespace is automatically added.
     *
     * @param locale the locale
     * @param key    the translation key (without namespace prefix)
     * @param args   the arguments to replace placeholders
     * @return the translated string with replacements
     */
    public String getString(Locale locale, String key, Object... args) {
        String fullKey = namespace + ":" + key;
        if (logger.isDebugEnabled()) {
            logger.debug("[{}] getString locale={}, key='{}' with {} arg(s) -> '{}'", plugin.getName(), locale.toLanguageTag(), key, args == null ? 0 : args.length, fullKey);
        }
        return languageManager.getString(locale, fullKey, args);
    }
}
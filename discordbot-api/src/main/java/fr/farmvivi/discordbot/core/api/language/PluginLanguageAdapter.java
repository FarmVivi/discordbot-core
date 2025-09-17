package fr.farmvivi.discordbot.core.api.language;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;

import java.util.Locale;

/**
 * A simplified language manager for plugins.
 * This class wraps the core language manager and handles namespace prefixing automatically.
 */
public class PluginLanguageAdapter {
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
        languageManager.registerNamespace(namespace);
    }

    /**
     * Gets a translated string for the specified key in the default language.
     * The namespace is automatically added.
     *
     * @param key the translation key (without namespace prefix)
     * @return the translated string, or the key itself if not found
     */
    public String getString(String key) {
        return languageManager.getString(namespace + ":" + key);
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
        return languageManager.getString(namespace + ":" + key, args);
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
        return languageManager.getString(locale, namespace + ":" + key);
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
        return languageManager.getString(locale, namespace + ":" + key, args);
    }
}
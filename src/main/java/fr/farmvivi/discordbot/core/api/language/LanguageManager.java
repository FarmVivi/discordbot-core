package fr.farmvivi.discordbot.core.api.language;

import java.util.Locale;
import java.util.Map;

/**
 * Interface for managing translations and languages.
 */
public interface LanguageManager {
    /**
     * Gets a translated string for the specified key in the default language.
     *
     * @param key the translation key
     * @return the translated string, or the key itself if not found
     */
    String getString(String key);

    /**
     * Gets a translated string for the specified key in the default language,
     * with placeholder replacements.
     *
     * @param key  the translation key
     * @param args the arguments to replace placeholders
     * @return the translated string with replacements, or the key itself if not found
     */
    String getString(String key, Object... args);

    /**
     * Gets a translated string for the specified key in the specified locale.
     *
     * @param locale the locale
     * @param key    the translation key
     * @return the translated string, or the key itself if not found
     */
    String getString(Locale locale, String key);

    /**
     * Gets a translated string for the specified key in the specified locale,
     * with placeholder replacements.
     *
     * @param locale the locale
     * @param key    the translation key
     * @param args   the arguments to replace placeholders
     * @return the translated string with replacements, or the key itself if not found
     */
    String getString(Locale locale, String key, Object... args);

    /**
     * Registers a namespace for a plugin.
     * This is used to prevent key conflicts between plugins.
     *
     * @param namespace the namespace
     * @return true if the namespace was registered, false if it already exists
     */
    boolean registerNamespace(String namespace);

    /**
     * Loads language strings from a file.
     *
     * @param namespace the namespace
     * @param locale    the locale
     * @param strings   the map of strings to load
     * @return the number of strings loaded
     */
    int loadLanguage(String namespace, Locale locale, Map<String, String> strings);

    /**
     * Gets the default locale.
     *
     * @return the default locale
     */
    Locale getDefaultLocale();

    /**
     * Gets all available locales.
     *
     * @return a map of locale codes to locale objects
     */
    Map<String, Locale> getAvailableLocales();
}
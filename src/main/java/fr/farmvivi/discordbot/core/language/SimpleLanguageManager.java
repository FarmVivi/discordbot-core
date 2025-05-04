package fr.farmvivi.discordbot.core.language;

import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the LanguageManager interface.
 */
public class SimpleLanguageManager implements LanguageManager {
    private static final Logger logger = LoggerFactory.getLogger(SimpleLanguageManager.class);

    // The default locale
    private final Locale defaultLocale;

    // Map of locale code to Locale object
    private final Map<String, Locale> availableLocales = new ConcurrentHashMap<>();

    // Map of namespace to a map of locale to a map of key to value
    private final Map<String, Map<Locale, Map<String, String>>> translations = new ConcurrentHashMap<>();

    // Set of registered namespaces
    private final Map<String, Boolean> registeredNamespaces = new ConcurrentHashMap<>();

    /**
     * Creates a new language manager with the specified default locale.
     *
     * @param defaultLocale the default locale
     */
    public SimpleLanguageManager(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        availableLocales.put(defaultLocale.toLanguageTag(), defaultLocale);

        // Always register the "core" namespace
        registerNamespace("core");
    }

    @Override
    public String getString(String key) {
        return getString(defaultLocale, key);
    }

    @Override
    public String getString(String key, Object... args) {
        return getString(defaultLocale, key, args);
    }

    @Override
    public String getString(Locale locale, String key) {
        // Parse the key to get the namespace and the actual key
        String namespace = "core";
        String actualKey = key;

        if (key.contains(":")) {
            String[] parts = key.split(":", 2);
            namespace = parts[0];
            actualKey = parts[1];
        }

        // Check if the namespace is registered
        if (!registeredNamespaces.containsKey(namespace)) {
            logger.warn("Namespace not registered: {}", namespace);
            return key;
        }

        // Get the translations for the namespace
        Map<Locale, Map<String, String>> namespaceTranslations = translations.get(namespace);
        if (namespaceTranslations == null) {
            return key;
        }

        // Try to get the translation for the specified locale
        Map<String, String> localeTranslations = namespaceTranslations.get(locale);
        if (localeTranslations != null && localeTranslations.containsKey(actualKey)) {
            return localeTranslations.get(actualKey);
        }

        // If the locale is not the default locale, try to fall back to the default locale
        if (!locale.equals(defaultLocale)) {
            localeTranslations = namespaceTranslations.get(defaultLocale);
            if (localeTranslations != null && localeTranslations.containsKey(actualKey)) {
                return localeTranslations.get(actualKey);
            }
        }

        // If all else fails, return the key
        return key;
    }

    @Override
    public String getString(Locale locale, String key, Object... args) {
        String value = getString(locale, key);
        if (value.equals(key)) {
            return key;
        }

        // Replace placeholders using MessageFormat
        try {
            return MessageFormat.format(value, args);
        } catch (Exception e) {
            logger.warn("Failed to format string: {} with args: {}", value, args, e);
            return value;
        }
    }

    @Override
    public boolean registerNamespace(String namespace) {
        if (registeredNamespaces.containsKey(namespace)) {
            return false;
        }

        registeredNamespaces.put(namespace, true);
        translations.put(namespace, new ConcurrentHashMap<>());
        return true;
    }

    @Override
    public int loadLanguage(String namespace, Locale locale, Map<String, String> strings) {
        // Check if the namespace is registered
        if (!registeredNamespaces.containsKey(namespace)) {
            logger.warn("Cannot load language for unregistered namespace: {}", namespace);
            return 0;
        }

        // Add the locale to available locales if it's not already there
        availableLocales.putIfAbsent(locale.toLanguageTag(), locale);

        // Get or create the translations for the namespace
        Map<Locale, Map<String, String>> namespaceTranslations = translations.computeIfAbsent(
                namespace, k -> new ConcurrentHashMap<>());

        // Get or create the translations for the locale
        Map<String, String> localeTranslations = namespaceTranslations.computeIfAbsent(
                locale, k -> new ConcurrentHashMap<>());

        // Add the strings
        localeTranslations.putAll(strings);

        logger.info("Loaded {} strings for namespace {} and locale {}",
                strings.size(), namespace, locale.toLanguageTag());
        return strings.size();
    }

    @Override
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    @Override
    public Map<String, Locale> getAvailableLocales() {
        return new HashMap<>(availableLocales);
    }
}
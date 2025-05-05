package fr.farmvivi.discordbot.core.language;

import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the LanguageManager interface with fallback to default resources.
 */
public class SimpleLanguageManager implements LanguageManager {
    private static final Logger logger = LoggerFactory.getLogger(SimpleLanguageManager.class);

    // The default locale
    private final Locale defaultLocale;

    // Map of locale code to Locale object
    private final Map<String, Locale> availableLocales = new ConcurrentHashMap<>();

    // Map of namespace to a map of locale to a map of key to value
    private final Map<String, Map<Locale, Map<String, String>>> translations = new ConcurrentHashMap<>();

    // Map of namespace to a map of locale to a map of key to value (default resources)
    private final Map<String, Map<Locale, Map<String, String>>> defaultTranslations = new ConcurrentHashMap<>();

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

        // Load default resources
        loadDefaultResources();
    }

    /**
     * Loads default language resources from the JAR
     */
    private void loadDefaultResources() {
        // Load English and French by default
        loadDefaultResource("core", Locale.forLanguageTag("en-US"), "/lang/en-US.yml");
        loadDefaultResource("core", Locale.forLanguageTag("fr-FR"), "/lang/fr-FR.yml");
    }

    /**
     * Loads a default language resource from the JAR
     *
     * @param namespace    the namespace
     * @param locale       the locale
     * @param resourcePath the path to the resource
     */
    private void loadDefaultResource(String namespace, Locale locale, String resourcePath) {
        try {
            InputStream inputStream = getClass().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                logger.warn("Default language resource not found: {}", resourcePath);
                return;
            }

            // Add the locale to available locales if it's not already there
            availableLocales.putIfAbsent(locale.toLanguageTag(), locale);

            // Load YAML from the resource
            Yaml yaml = new Yaml();
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                Map<String, Object> langData = yaml.load(reader);
                if (langData != null) {
                    // Flatten the map
                    Map<String, String> flatMap = flattenMap(langData, "");

                    // Store the translations
                    Map<Locale, Map<String, String>> namespaceDefaultTranslations =
                            defaultTranslations.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>());
                    namespaceDefaultTranslations.put(locale, flatMap);

                    logger.info("Loaded {} default strings for namespace {} and locale {} from resource",
                            flatMap.size(), namespace, locale.toLanguageTag());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load default language resource: {}", resourcePath, e);
        }
    }

    /**
     * Flattens a nested map into a flat map with dot notation keys.
     *
     * @param map    the nested map
     * @param prefix the key prefix
     * @return a flattened map
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> flattenMap(Map<String, Object> map, String prefix) {
        Map<String, String> flatMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();

            if (entry.getValue() instanceof Map) {
                // Recursive flattening of nested maps
                flatMap.putAll(flattenMap((Map<String, Object>) entry.getValue(), key));
            } else {
                // Add leaf value
                flatMap.put(key, String.valueOf(entry.getValue()));
            }
        }

        return flatMap;
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

        // Cascade de recherche de traduction:
        String translation;

        // 1. Essayer dans la locale spécifiée dans le dossier runtime
        translation = getTranslationFromRuntime(namespace, locale, actualKey);
        if (translation != null) return translation;

        // 2. Essayer dans la locale spécifiée dans les ressources par défaut
        translation = getTranslationFromResources(namespace, locale, actualKey);
        if (translation != null) return translation;

        // 3. Si la locale n'est pas l'anglais, essayer dans la locale anglaise du dossier runtime
        if (!locale.getLanguage().equals("en")) {
            Locale englishLocale = Locale.forLanguageTag("en-US");
            translation = getTranslationFromRuntime(namespace, englishLocale, actualKey);
            if (translation != null) return translation;

            // 4. Essayer dans la locale anglaise des ressources par défaut
            translation = getTranslationFromResources(namespace, englishLocale, actualKey);
            if (translation != null) return translation;
        }

        // 5. Si la locale n'est pas la locale par défaut, essayer dans la locale par défaut du runtime
        if (!locale.equals(defaultLocale)) {
            translation = getTranslationFromRuntime(namespace, defaultLocale, actualKey);
            if (translation != null) return translation;

            // 6. Essayer dans la locale par défaut des ressources
            translation = getTranslationFromResources(namespace, defaultLocale, actualKey);
            if (translation != null) return translation;
        }

        // 7. Si tout échoue, retourner la clé elle-même et logger un avertissement
        logger.debug("Translation not found for key: {} in locale: {} (namespace: {})",
                actualKey, locale.toLanguageTag(), namespace);
        return key;
    }

    /**
     * Recherche une traduction dans les fichiers du dossier runtime.
     *
     * @param namespace le namespace
     * @param locale    la locale
     * @param key       la clé
     * @return la traduction, ou null si non trouvée
     */
    private String getTranslationFromRuntime(String namespace, Locale locale, String key) {
        Map<Locale, Map<String, String>> namespaceTranslations = translations.get(namespace);
        if (namespaceTranslations != null) {
            Map<String, String> localeTranslations = namespaceTranslations.get(locale);
            if (localeTranslations != null && localeTranslations.containsKey(key)) {
                return localeTranslations.get(key);
            }
        }
        return null;
    }

    /**
     * Recherche une traduction dans les ressources par défaut.
     *
     * @param namespace le namespace
     * @param locale    la locale
     * @param key       la clé
     * @return la traduction, ou null si non trouvée
     */
    private String getTranslationFromResources(String namespace, Locale locale, String key) {
        Map<Locale, Map<String, String>> namespaceDefaultTranslations = defaultTranslations.get(namespace);
        if (namespaceDefaultTranslations != null) {
            Map<String, String> localeDefaultTranslations = namespaceDefaultTranslations.get(locale);
            if (localeDefaultTranslations != null && localeDefaultTranslations.containsKey(key)) {
                return localeDefaultTranslations.get(key);
            }
        }
        return null;
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
        defaultTranslations.put(namespace, new ConcurrentHashMap<>());
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
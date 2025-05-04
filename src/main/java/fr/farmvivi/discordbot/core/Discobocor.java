package fr.farmvivi.discordbot.core;

import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.config.EnvAwareYamlConfiguration;
import fr.farmvivi.discordbot.core.discord.JDADiscordAPI;
import fr.farmvivi.discordbot.core.event.SimpleEventManager;
import fr.farmvivi.discordbot.core.language.SimpleLanguageManager;
import fr.farmvivi.discordbot.core.plugin.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Main class for the Discord bot core.
 * This class is responsible for loading and managing plugins.
 */
public class Discobocor {
    public static final String NAME;
    public static final String VERSION;
    public static final boolean PRODUCTION;
    private static final Logger logger;

    // Instances for global access/management
    private static PluginManager pluginManager;
    private static DiscordAPI discordAPI;
    private static SimpleEventManager eventManager;
    private static Configuration coreConfig;
    private static LanguageManager languageManager;

    static {
        Properties properties = new Properties();
        try {
            properties.load(Discobocor.class.getClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException e) {
            System.out.println("ERROR: Cannot read properties file !");
            System.exit(1);
        }

        NAME = properties.getProperty("name");
        VERSION = properties.getProperty("version");
        PRODUCTION = !VERSION.contains("-SNAPSHOT");

        logger = LoggerFactory.getLogger(NAME);
    }

    /**
     * Main entry point.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        long startTimeMillis = System.currentTimeMillis();

        logger.info("Démarrage de " + NAME + " v" + VERSION + " en cours...");

        // Log system information
        logSystemInfo();

        // Initialize bot components
        if (!initializeComponents()) {
            return;
        }

        // Register shutdown hook for clean shutdown
        registerShutdownHook();

        // Start the bot using the proper sequence
        startBot();

        long startFinishedTimeMillis = System.currentTimeMillis();
        logger.info("Started in " + (float) (startFinishedTimeMillis - startTimeMillis) / 1000 + "s!");

        // Keep the main thread alive
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Logs system information.
     */
    private static void logSystemInfo() {
        logger.info("System.getProperty('os.name') == '" + System.getProperty("os.name") + "'");
        logger.info("System.getProperty('os.version') == '" + System.getProperty("os.version") + "'");
        logger.info("System.getProperty('os.arch') == '" + System.getProperty("os.arch") + "'");
        logger.info("System.getProperty('java.version') == '" + System.getProperty("java.version") + "'");
        logger.info("System.getProperty('java.vendor') == '" + System.getProperty("java.vendor") + "'");
        logger.info("System.getProperty('sun.arch.data.model') == '" + System.getProperty("sun.arch.data.model") + "'");
        logger.info("System.getProperty('user.timezone') == '" + System.getProperty("user.timezone") + "'");
        logger.info("System.getProperty('user.country') == '" + System.getProperty("user.country") + "'");
        logger.info("System.getProperty('user.language') == '" + System.getProperty("user.language") + "'");
    }

    /**
     * Initializes all components needed for the bot.
     *
     * @return true if initialization was successful, false otherwise
     */
    private static boolean initializeComponents() {
        // Create the plugins folder if it doesn't exist
        File pluginsFolder = new File("plugins");
        if (!pluginsFolder.exists()) {
            pluginsFolder.mkdirs();
        }

        // Create the config file if it doesn't exist
        File configFile = new File("config.yml");
        if (!configFile.exists()) {
            if (!createDefaultConfig(configFile)) {
                return false;
            }
        }

        // Load the configuration
        try {
            coreConfig = new EnvAwareYamlConfiguration(configFile);
        } catch (Exception e) {
            logger.error("Failed to load config.yml", e);
            System.exit(1);
            return false;
        }

        // Check if the token is set
        String token;
        try {
            token = coreConfig.getString("discord.token");
            if (token == null || token.equals("YOUR_BOT_TOKEN")) {
                logger.error("Please set your bot token in config.yml");
                System.exit(1);
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to get discord.token from config.yml", e);
            System.exit(1);
            return false;
        }

        // Create the language manager with the default locale from config
        String defaultLanguage = coreConfig.getString("language.default", "en-US");
        Locale defaultLocale = Locale.forLanguageTag(defaultLanguage);
        if (defaultLocale.getLanguage().isEmpty()) {
            logger.warn("Invalid default language '{}', falling back to en-US", defaultLanguage);
            defaultLocale = Locale.US;
        }

        languageManager = new SimpleLanguageManager(defaultLocale);

        // Load core language files
        loadCoreLanguages();

        // Create the event manager
        eventManager = new SimpleEventManager();

        // Create the Discord API
        discordAPI = new JDADiscordAPI(token);

        // Create the plugin manager
        pluginManager = new PluginManager(pluginsFolder, eventManager, discordAPI, languageManager);

        return true;
    }

    /**
     * Creates the default configuration file.
     *
     * @param configFile the configuration file
     * @return true if creation was successful, false otherwise
     */
    private static boolean createDefaultConfig(File configFile) {
        try {
            Files.writeString(Path.of(configFile.getAbsolutePath()),
                    "# Discord Bot Configuration\n" +
                            "discord:\n" +
                            "  token: YOUR_BOT_TOKEN\n\n" +
                            "# Language settings\n" +
                            "language:\n" +
                            "  default: en-US\n"
            );
            logger.info("Created default config.yml");
            logger.info("Please edit config.yml and restart the bot");
            System.exit(0);
            return false;
        } catch (IOException e) {
            logger.error("Failed to create config.yml", e);
            System.exit(1);
            return false;
        }
    }

    /**
     * Registers the shutdown hook for clean shutdown.
     */
    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down...");

            // Shutdown sequence
            shutdownBot();

            logger.info("Goodbye!");
        }));
    }

    /**
     * Starts the bot using the proper sequence.
     */
    private static void startBot() {
        // 1. Load plugins
        pluginManager.loadPlugins();

        // 2. Pre-enable plugins (they can now configure JDABuilder)
        pluginManager.preEnablePlugins();

        // 3. Connect to Discord
        try {
            discordAPI.connect().join();

            // Set the startup presence right after connecting
            discordAPI.setStartupPresence();
        } catch (Exception e) {
            logger.error("Failed to connect to Discord", e);
            System.exit(1);
            return;
        }

        // 4. Enable plugins
        pluginManager.enablePlugins();

        // 5. Post-enable plugins
        pluginManager.postEnablePlugins();

        // 6. Set the default presence after all plugins are enabled
        discordAPI.setDefaultPresence();
    }

    /**
     * Shuts down the bot using the proper sequence.
     */
    private static void shutdownBot() {
        // 1. Pre-disable plugins
        pluginManager.preDisablePlugins();

        // 2. Disable plugins
        pluginManager.disablePlugins();

        // 3. Post-disable plugins
        pluginManager.postDisablePlugins();

        // 4. Disconnect from Discord (setShutdownPresence is called inside disconnect)
        try {
            discordAPI.disconnect().join();
        } catch (Exception e) {
            logger.error("Failed to disconnect from Discord", e);
        }

        // 5. Shutdown the event manager
        eventManager.shutdown();
    }

    /**
     * Reloads all plugins.
     * This will disable and re-enable all plugins.
     */
    public static void reloadPlugins() {
        logger.info("Starting complete plugin reload...");

        // Disable plugins
        pluginManager.preDisablePlugins();
        pluginManager.disablePlugins();
        pluginManager.postDisablePlugins();

        // Disconnect from Discord
        try {
            discordAPI.disconnect().join();
        } catch (Exception e) {
            logger.error("Failed to disconnect from Discord during reload", e);
            return;
        }

        // Load and pre-enable plugins
        pluginManager.loadPlugins();
        pluginManager.preEnablePlugins();

        // Connect to Discord again
        try {
            discordAPI.connect().join();

            // Set the startup presence during reload
            discordAPI.setStartupPresence();
        } catch (Exception e) {
            logger.error("Failed to reconnect to Discord during reload", e);
            return;
        }

        // Complete plugin enabling
        pluginManager.enablePlugins();
        pluginManager.postEnablePlugins();

        // Set the default presence after reload is complete
        discordAPI.setDefaultPresence();

        logger.info("Complete plugin reload finished successfully!");
    }

    /**
     * Loads the core language files.
     */
    private static void loadCoreLanguages() {
        File langFolder = new File("lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();

            // Create default en-US language file
            createDefaultEnglishFile(new File(langFolder, "en-US.yml"));

            // Create default fr-FR language file
            createDefaultFrenchFile(new File(langFolder, "fr-FR.yml"));
        }

        // Load all .yml files in the lang folder
        File[] langFiles = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles != null) {
            for (File langFile : langFiles) {
                try {
                    // Parse locale from filename
                    String localeCode = langFile.getName().replace(".yml", "");
                    Locale locale = Locale.forLanguageTag(localeCode);

                    // Load the language file
                    Yaml yaml = new Yaml();
                    try (FileReader reader = new FileReader(langFile)) {
                        Map<String, Object> langData = yaml.load(reader);
                        if (langData != null) {
                            // Convert the nested map to a flat map with keys like "section.key"
                            Map<String, String> flatMap = flattenMap(langData, "");

                            // Load the language strings
                            languageManager.loadLanguage("core", locale, flatMap);
                            logger.info("Loaded language file: {}", langFile.getName());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to load language file: {}", langFile.getName(), e);
                }
            }
        }
    }

    /**
     * Flattens a nested map into a flat map with dot notation keys.
     *
     * @param map    the nested map
     * @param prefix the prefix for keys
     * @return a flattened map
     */
    @SuppressWarnings("unchecked")
    private static Map<String, String> flattenMap(Map<String, Object> map, String prefix) {
        Map<String, String> flatMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();

            if (entry.getValue() instanceof Map) {
                // Recursively flatten nested maps
                flatMap.putAll(flattenMap((Map<String, Object>) entry.getValue(), key));
            } else {
                // Add the leaf value
                flatMap.put(key, String.valueOf(entry.getValue()));
            }
        }

        return flatMap;
    }

    /**
     * Creates the default English language file.
     *
     * @param file the file to create
     */
    private static void createDefaultEnglishFile(File file) {
        try {
            Files.writeString(file.toPath(),
                    "# English language file (en-US)\n" +
                            "general:\n" +
                            "  yes: \"Yes\"\n" +
                            "  no: \"No\"\n" +
                            "  enabled: \"Enabled\"\n" +
                            "  disabled: \"Disabled\"\n" +
                            "  success: \"Success\"\n" +
                            "  error: \"Error\"\n" +
                            "  warning: \"Warning\"\n" +
                            "  info: \"Information\"\n" +
                            "\n" +
                            "bot:\n" +
                            "  startup: \"Bot is starting up...\"\n" +
                            "  shutdown: \"Bot is shutting down...\"\n" +
                            "  connected: \"Connected to Discord as {0}\"\n" +
                            "  disconnected: \"Disconnected from Discord\"\n" +
                            "\n" +
                            "plugins:\n" +
                            "  loading: \"Loading plugins...\"\n" +
                            "  loaded: \"Loaded {0} plugins\"\n" +
                            "  enabling: \"Enabling plugins...\"\n" +
                            "  enabled: \"Enabled {0} plugins\"\n" +
                            "  disabling: \"Disabling plugins...\"\n" +
                            "  disabled: \"Disabled {0} plugins\"\n" +
                            "  reloading: \"Reloading plugins...\"\n" +
                            "  reloaded: \"Reloaded {0} plugins\"\n" +
                            "\n" +
                            "commands:\n" +
                            "  help: \"Displays help for commands\"\n" +
                            "  reload: \"Reloads the bot or specific plugins\"\n" +
                            "  version: \"Displays the bot version\"\n" +
                            "  plugins: \"Lists all loaded plugins\"\n" +
                            "  language: \"Changes the bot language\"\n" +
                            "\n" +
                            "errors:\n" +
                            "  command_not_found: \"Command not found: {0}\"\n" +
                            "  plugin_not_found: \"Plugin not found: {0}\"\n" +
                            "  no_permission: \"You don't have permission to use this command\"\n" +
                            "  unknown: \"An unknown error occurred\"\n"
            );
            logger.info("Created default English language file");
        } catch (IOException e) {
            logger.error("Failed to create default English language file", e);
        }
    }

    /**
     * Creates the default French language file.
     *
     * @param file the file to create
     */
    private static void createDefaultFrenchFile(File file) {
        try {
            Files.writeString(file.toPath(),
                    "# Fichier de langue français (fr-FR)\n" +
                            "general:\n" +
                            "  yes: \"Oui\"\n" +
                            "  no: \"Non\"\n" +
                            "  enabled: \"Activé\"\n" +
                            "  disabled: \"Désactivé\"\n" +
                            "  success: \"Succès\"\n" +
                            "  error: \"Erreur\"\n" +
                            "  warning: \"Avertissement\"\n" +
                            "  info: \"Information\"\n" +
                            "\n" +
                            "bot:\n" +
                            "  startup: \"Le bot démarre...\"\n" +
                            "  shutdown: \"Le bot s'arrête...\"\n" +
                            "  connected: \"Connecté à Discord en tant que {0}\"\n" +
                            "  disconnected: \"Déconnecté de Discord\"\n" +
                            "\n" +
                            "plugins:\n" +
                            "  loading: \"Chargement des plugins...\"\n" +
                            "  loaded: \"{0} plugins chargés\"\n" +
                            "  enabling: \"Activation des plugins...\"\n" +
                            "  enabled: \"{0} plugins activés\"\n" +
                            "  disabling: \"Désactivation des plugins...\"\n" +
                            "  disabled: \"{0} plugins désactivés\"\n" +
                            "  reloading: \"Rechargement des plugins...\"\n" +
                            "  reloaded: \"{0} plugins rechargés\"\n" +
                            "\n" +
                            "commands:\n" +
                            "  help: \"Affiche l'aide des commandes\"\n" +
                            "  reload: \"Recharge le bot ou des plugins spécifiques\"\n" +
                            "  version: \"Affiche la version du bot\"\n" +
                            "  plugins: \"Liste tous les plugins chargés\"\n" +
                            "  language: \"Change la langue du bot\"\n" +
                            "\n" +
                            "errors:\n" +
                            "  command_not_found: \"Commande introuvable : {0}\"\n" +
                            "  plugin_not_found: \"Plugin introuvable : {0}\"\n" +
                            "  no_permission: \"Vous n'avez pas la permission d'utiliser cette commande\"\n" +
                            "  unknown: \"Une erreur inconnue s'est produite\"\n"
            );
            logger.info("Created default French language file");
        } catch (IOException e) {
            logger.error("Failed to create default French language file", e);
        }
    }

    /**
     * Gets the language manager.
     *
     * @return the language manager
     */
    public static LanguageManager getLanguageManager() {
        return languageManager;
    }

    /**
     * Gets the plugin manager.
     *
     * @return the plugin manager
     */
    public static PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * Gets the Discord API.
     *
     * @return the Discord API
     */
    public static DiscordAPI getDiscordAPI() {
        return discordAPI;
    }

    /**
     * Gets the event manager.
     *
     * @return the event manager
     */
    public static SimpleEventManager getEventManager() {
        return eventManager;
    }

    /**
     * Gets the core configuration.
     *
     * @return the core configuration
     */
    public static Configuration getCoreConfig() {
        return coreConfig;
    }
}
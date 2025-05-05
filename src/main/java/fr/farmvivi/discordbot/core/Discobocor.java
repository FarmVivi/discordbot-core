package fr.farmvivi.discordbot.core;

import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.data.DataStorageProvider;
import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.config.EnvAwareYamlConfiguration;
import fr.farmvivi.discordbot.core.data.DataStorageFactory;
import fr.farmvivi.discordbot.core.discord.JDADiscordAPI;
import fr.farmvivi.discordbot.core.event.SimpleEventManager;
import fr.farmvivi.discordbot.core.language.LanguageFileManager;
import fr.farmvivi.discordbot.core.language.SimpleLanguageManager;
import fr.farmvivi.discordbot.core.plugin.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
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
    private static DataStorageProvider dataStorageProvider;

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

        // Create the language manager
        languageManager = new SimpleLanguageManager(defaultLocale);

        // Create the language file manager
        LanguageFileManager languageFileManager = new LanguageFileManager(languageManager, new File("lang"));
        languageFileManager.loadLanguageFiles();

        // Create the event manager
        eventManager = new SimpleEventManager();

        // Create the Discord API
        discordAPI = new JDADiscordAPI(token);

        // Create the data storage provider
        dataStorageProvider = DataStorageFactory.createStorageProvider(
                coreConfig,
                DataStorageProvider.StorageType.FILE
        );

        // Create the plugin manager
        pluginManager = new PluginManager(pluginsFolder, eventManager, discordAPI, languageManager, dataStorageProvider);

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
                            "  default: en-US\n\n" +
                            "# Data storage settings\n" +
                            "data:\n" +
                            "  storage:\n" +
                            "    type: FILE  # Options: FILE, DB\n" +
                            "    db:\n" +
                            "      url: jdbc:mysql://localhost:3306/discordbot\n" +
                            "      username: username\n" +
                            "      password: password\n"
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

        // 6. Close the data storage provider
        if (dataStorageProvider != null && !dataStorageProvider.close()) {
            logger.error("Failed to close data storage provider");
        }
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
     * Gets the data storage provider.
     *
     * @return the data storage provider
     */
    public static DataStorageProvider getDataStorageProvider() {
        return dataStorageProvider;
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
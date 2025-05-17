package fr.farmvivi.discordbot.core;

import fr.farmvivi.discordbot.core.api.audio.AudioService;
import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.api.permissions.PermissionManager;
import fr.farmvivi.discordbot.core.api.storage.DataStorageManager;
import fr.farmvivi.discordbot.core.api.storage.binary.BinaryStorageManager;
import fr.farmvivi.discordbot.core.audio.AudioServiceImpl;
import fr.farmvivi.discordbot.core.config.EnvAwareYamlConfiguration;
import fr.farmvivi.discordbot.core.discord.JDADiscordAPI;
import fr.farmvivi.discordbot.core.event.SimpleEventManager;
import fr.farmvivi.discordbot.core.language.LanguageFileLoader;
import fr.farmvivi.discordbot.core.language.SimpleLanguageManager;
import fr.farmvivi.discordbot.core.permissions.SimplePermissionManager;
import fr.farmvivi.discordbot.core.plugin.PluginManager;
import fr.farmvivi.discordbot.core.storage.StorageFactory;
import fr.farmvivi.discordbot.core.storage.binary.BinaryStorageFactory;
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
    private static DataStorageManager dataStorageManager;
    private static BinaryStorageManager binaryStorageManager;
    private static SimplePermissionManager permissionManager;
    private static AudioService audioService;

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
        LanguageFileLoader languageFileLoader = new LanguageFileLoader(languageManager, new File("lang"));
        languageFileLoader.loadLanguageFiles();

        // Create the event manager
        eventManager = new SimpleEventManager();

        // Create the Discord API
        discordAPI = new JDADiscordAPI(token);

        // Create the data storage manager
        dataStorageManager = StorageFactory.createStorageManager(coreConfig, eventManager);

        // Create the binary storage manager
        binaryStorageManager = BinaryStorageFactory.createBinaryStorageManager(coreConfig, eventManager);

        // Create the permission manager
        permissionManager = new SimplePermissionManager(eventManager, dataStorageManager);

        // Create the audio service
        audioService = new AudioServiceImpl(eventManager);

        // Create the plugin manager
        pluginManager = new PluginManager(
                pluginsFolder,
                eventManager,
                discordAPI,
                languageManager,
                dataStorageManager,
                binaryStorageManager,
                permissionManager,
                audioService
        );

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
                            "      password: password\n\n" +
                            "  # Binary storage settings for large files\n" +
                            "  binary:\n" +
                            "    storage:\n" +
                            "      type: FILE  # Options: FILE, S3\n" +
                            "      file:\n" +
                            "        folder: binary\n" +
                            "      s3:\n" +
                            "        bucket: your-bucket-name\n" +
                            "        region: eu-west-3\n" +
                            "        access_key: your-access-key\n" +
                            "        secret_key: your-secret-key\n" +
                            "        endpoint: https://s3.amazonaws.com  # Optional, for S3-compatible services\n" +
                            "        prefix: discordbot  # Optional, folder prefix in bucket\n" +
                            "\n" +
                            "# Audio settings\n" +
                            "audio:\n" +
                            "  # Default speaking mode (VOICE, SOUNDSHARE, PRIORITY_SPEAKER)\n" +
                            "  speaking_mode: VOICE\n"
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
        // Use the new close() method which handles the full shutdown sequence
        if (pluginManager != null) {
            try {
                pluginManager.close();
            } catch (Exception e) {
                logger.error("Error during plugin shutdown", e);
            }
        }

        // Disconnect from Discord
        if (discordAPI != null) {
            try {
                discordAPI.disconnect().join();
            } catch (Exception e) {
                logger.error("Failed to disconnect from Discord", e);
            }
        }

        // Shutdown other services
        if (eventManager != null) {
            eventManager.shutdown();
        }

        if (dataStorageManager != null) {
            dataStorageManager.close();
        }

        if (binaryStorageManager != null) {
            binaryStorageManager.close();
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
     * Gets the data storage manager.
     *
     * @return the data storage manager
     */
    public static DataStorageManager getDataStorageManager() {
        return dataStorageManager;
    }

    /**
     * Gets the binary storage manager.
     *
     * @return the binary storage manager
     */
    public static BinaryStorageManager getBinaryStorageManager() {
        return binaryStorageManager;
    }

    /**
     * Gets the core configuration.
     *
     * @return the core configuration
     */
    public static Configuration getCoreConfig() {
        return coreConfig;
    }

    /**
     * Gets the permission manager.
     *
     * @return the permission manager
     */
    public static PermissionManager getPermissionManager() {
        return permissionManager;
    }

    /**
     * Gets the audio service.
     *
     * @return the audio service, or null if audio is disabled
     */
    public static AudioService getAudioService() {
        return audioService;
    }
}
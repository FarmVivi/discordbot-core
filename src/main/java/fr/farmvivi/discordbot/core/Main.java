package fr.farmvivi.discordbot.core;

import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.config.YamlConfiguration;
import fr.farmvivi.discordbot.core.discord.JDADiscordAPI;
import fr.farmvivi.discordbot.core.event.SimpleEventManager;
import fr.farmvivi.discordbot.core.plugin.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Main class for the Discord bot core.
 * This class is responsible for loading and managing plugins.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Main entry point.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Create the plugins folder if it doesn't exist
        File pluginsFolder = new File("plugins");
        if (!pluginsFolder.exists()) {
            pluginsFolder.mkdirs();
        }

        // Create the config file if it doesn't exist
        File configFile = new File("config.yml");
        if (!configFile.exists()) {
            try {
                Files.writeString(Path.of(configFile.getAbsolutePath()),
                        "# Discord Bot Configuration\n" +
                                "discord:\n" +
                                "  token: YOUR_BOT_TOKEN\n" +
                                "  prefix: '!'\n" +
                                "  admins:\n" +
                                "    - '123456789012345678' # Discord user ID\n"
                );
                logger.info("Created default config.yml");
                logger.info("Please edit config.yml and restart the bot");
                System.exit(0);
            } catch (IOException e) {
                logger.error("Failed to create config.yml", e);
                System.exit(1);
            }
        }

        // Load the configuration
        Configuration config;
        try {
            config = new YamlConfiguration(configFile);
        } catch (Exception e) {
            logger.error("Failed to load config.yml", e);
            System.exit(1);
            return;
        }

        // Check if the token is set
        String token;
        try {
            token = config.getString("discord.token");
            if (token == null || token.equals("YOUR_BOT_TOKEN")) {
                logger.error("Please set your bot token in config.yml");
                System.exit(1);
                return;
            }
        } catch (Exception e) {
            logger.error("Failed to get discord.token from config.yml", e);
            System.exit(1);
            return;
        }

        // Create the event manager
        SimpleEventManager eventManager = new SimpleEventManager();

        // Create the Discord API
        DiscordAPI discordAPI = new JDADiscordAPI(token, eventManager);

        // Create the plugin manager
        PluginManager pluginManager = new PluginManager(pluginsFolder, eventManager, discordAPI, config);

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down...");

            // Disable plugins
            pluginManager.disablePlugins();

            // Shutdown the event manager
            eventManager.shutdown();

            // Disconnect from Discord
            try {
                discordAPI.disconnect().join();
            } catch (Exception e) {
                logger.error("Failed to disconnect from Discord", e);
            }

            logger.info("Goodbye!");
        }));

        // Connect to Discord
        try {
            discordAPI.connect().join();
        } catch (Exception e) {
            logger.error("Failed to connect to Discord", e);
            System.exit(1);
            return;
        }

        // Load and enable plugins
        pluginManager.loadPlugins();
        pluginManager.enablePlugins();

        // Keep the main thread alive
//        CountDownLatch latch = new CountDownLatch(1);
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
    }
}
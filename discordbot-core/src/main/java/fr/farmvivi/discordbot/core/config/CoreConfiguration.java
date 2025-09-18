package fr.farmvivi.discordbot.core.config;

import fr.farmvivi.discordbot.api.config.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Core configuration manager for DiscordBot Core.
 */
public class CoreConfiguration extends EnvAwareYamlConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(CoreConfiguration.class);

    /**
     * Creates a new core configuration.
     *
     * @param configFile the configuration file
     * @throws ConfigurationException if there's an error loading the configuration
     */
    public CoreConfiguration(File configFile) throws ConfigurationException {
        super(configFile);
        
        // Create default config if it doesn't exist
        if (!configFile.exists()) {
            createDefaultConfig(configFile);
            // Exit after creating default config to let user edit it
            logger.info("Created default config.yml");
            logger.info("Please edit config.yml and restart the bot");
            System.exit(0);
        }
    }

    /**
     * Creates the default configuration file with current version.
     *
     * @param configFile the configuration file to create
     * @throws ConfigurationException if creation fails
     */
    private void createDefaultConfig(File configFile) throws ConfigurationException {
        try {
            String defaultConfig = generateDefaultConfigContent();
            Files.writeString(Path.of(configFile.getAbsolutePath()), defaultConfig);
            logger.info("Created default core configuration at {}", configFile.getAbsolutePath());
            
            // Reload to parse the created configuration
            reload();
        } catch (IOException e) {
            throw new ConfigurationException("Failed to create default configuration", e);
        }
    }
    
    /**
     * Generates the default configuration content.
     *
     * @return the default configuration as a string
     */
    private String generateDefaultConfigContent() {
        return "# Discord Bot Configuration\n" +
               "discord:\n" +
               "  token: YOUR_BOT_TOKEN\n\n" +
               "# Language settings\n" +
               "language:\n" +
               "  default: en-US\n\n" +
               "# Command system settings\n" +
               "commands:\n" +
               "  default-prefix: !  # Default prefix for text commands\n" +
               "  cooldown: 3  # Global default cooldown in seconds\n" +
               "  system:\n" +
               "    help: true      # Enable/disable help command\n" +
               "    version: true   # Enable/disable version command\n" +
               "    shutdown: true  # Enable/disable shutdown command\n" +
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
               "        prefix: discordbot  # Optional, folder prefix in bucket\n";
    }

    /**
     * Validates that the configuration has all required fields.
     *
     * @throws ConfigurationException if required fields are missing
     */
    public void validateConfiguration() throws ConfigurationException {
        // Check required discord configuration
        if (!contains("discord.token") || getString("discord.token", "").equals("YOUR_BOT_TOKEN")) {
            throw new ConfigurationException("Discord token is required. Please set discord.token in config.yml");
        }
        
        // Validate other critical settings
        if (!contains("language.default")) {
            throw new ConfigurationException("Default language is required. Please set language.default in config.yml");
        }
        
        if (!contains("commands.default-prefix")) {
            throw new ConfigurationException("Default command prefix is required. Please set commands.default-prefix in config.yml");
        }
    }
}
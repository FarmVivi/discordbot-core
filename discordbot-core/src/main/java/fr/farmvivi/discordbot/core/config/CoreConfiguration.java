package fr.farmvivi.discordbot.core.config;

import fr.farmvivi.discordbot.api.config.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
     * Creates the default configuration file.
     *
     * @param configFile the configuration file to create
     * @throws ConfigurationException if creation fails
     */
    private void createDefaultConfig(File configFile) throws ConfigurationException {
        try {
            // Copy default config from resources
            try (InputStream defaultConfigStream = getClass().getResourceAsStream("/config.yml")) {
                if (defaultConfigStream == null) {
                    throw new ConfigurationException("Default config.yml not found in core resources");
                }
                
                Files.copy(defaultConfigStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.info("Created default core configuration at {}", configFile.getAbsolutePath());
            }
            
            // Reload to parse the created configuration
            reload();
        } catch (IOException e) {
            throw new ConfigurationException("Failed to create default configuration", e);
        }
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
package fr.farmvivi.discordbot.core.config.migrations;

import fr.farmvivi.discordbot.api.config.Configuration;
import fr.farmvivi.discordbot.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.config.ConfigurationMigrator;

/**
 * Migration from core configuration version 2.3.0 to 2.3.27.
 * Adds new plugin configuration management and security settings.
 */
public class CoreMigration_2_3_0_to_2_3_27 implements ConfigurationMigrator {
    
    @Override
    public String getFromVersion() {
        return "2.3.0";
    }
    
    @Override
    public String getToVersion() {
        return "2.3.27";
    }
    
    @Override
    public void migrate(Configuration configuration) throws ConfigurationException {
        // Add new plugin system configuration
        if (!configuration.contains("plugins.config.auto_migrate")) {
            configuration.set("plugins.config.auto_migrate", true);
            configuration.set("plugins.config.backup_before_migration", true);
            configuration.set("plugins.config.max_backup_files", 5);
        }
        
        // Add security settings
        if (!configuration.contains("security.rate_limiting.enabled")) {
            configuration.set("security.rate_limiting.enabled", true);
            configuration.set("security.rate_limiting.requests_per_minute", 60);
        }
        
        // Add performance settings
        if (!configuration.contains("performance.thread_pool.core_size")) {
            configuration.set("performance.thread_pool.core_size", 4);
            configuration.set("performance.thread_pool.max_size", 16);
            configuration.set("performance.thread_pool.queue_size", 1000);
        }
        
        // Add cache settings
        if (!configuration.contains("performance.cache.max_size")) {
            configuration.set("performance.cache.max_size", 10000);
            configuration.set("performance.cache.expire_after_write", "1h");
        }
        
        // Migrate old logging configuration if present
        if (configuration.contains("log.level") && !configuration.contains("logging.level")) {
            String oldLevel = configuration.getString("log.level", "info");
            configuration.set("logging.level", oldLevel);
            configuration.set("logging.file.enabled", true);
            configuration.set("logging.file.path", "logs/bot.log");
            configuration.set("logging.console.enabled", true);
        }
        
        // Add extended language support
        if (!configuration.contains("language_extended.supported")) {
            configuration.set("language_extended.supported", 
                java.util.Arrays.asList("en-US", "fr-FR", "es-ES", "de-DE"));
        }
    }
    
    @Override
    public boolean canMigrate(Configuration configuration) {
        // Check if we have the basic structure we expect
        return configuration.contains("discord.token");
    }
    
    @Override
    public String getDescription() {
        return "Add plugin configuration management, security settings, and performance tuning";
    }
}
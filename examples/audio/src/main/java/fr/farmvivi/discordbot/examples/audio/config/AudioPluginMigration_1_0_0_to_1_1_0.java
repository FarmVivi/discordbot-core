package fr.farmvivi.discordbot.examples.audio.config;

import fr.farmvivi.discordbot.api.config.Configuration;
import fr.farmvivi.discordbot.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.config.ConfigurationMigrator;

/**
 * Example migration for audio plugin from version 1.0.0 to 1.1.0.
 * This migration adds new audio enhancement settings.
 */
public class AudioPluginMigration_1_0_0_to_1_1_0 implements ConfigurationMigrator {
    
    @Override
    public String getFromVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getToVersion() {
        return "1.1.0";
    }
    
    @Override
    public void migrate(Configuration configuration) throws ConfigurationException {
        // Add new audio enhancement settings
        if (!configuration.contains("audio.enable_enhancement")) {
            configuration.set("audio.enable_enhancement", true);
        }
        
        if (!configuration.contains("audio.enhancement_level")) {
            configuration.set("audio.enhancement_level", "medium");
        }
        
        // Migrate old volume settings format if needed
        if (configuration.contains("volume") && !configuration.contains("audio.default_volume")) {
            int oldVolume = configuration.getInt("volume", 50);
            configuration.set("audio.default_volume", oldVolume);
            // Note: We don't remove the old key to maintain backward compatibility
        }
        
        // Add new voice channel timeout settings
        if (!configuration.contains("voice.auto_leave_timeout")) {
            configuration.set("voice.auto_leave_timeout", 30);
        }
    }
    
    @Override
    public boolean canMigrate(Configuration configuration) {
        // Check if we have the basic structure we expect
        return configuration.contains("config_version");
    }
    
    @Override
    public String getDescription() {
        return "Add audio enhancement settings and migrate volume configuration";
    }
}
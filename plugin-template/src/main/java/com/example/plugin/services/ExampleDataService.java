package com.example.plugin.services;

import fr.farmvivi.discordbot.api.plugin.AbstractPlugin;

/**
 * Example service class demonstrating data management and plugin services.
 * Shows how to organize plugin business logic.
 */
public class ExampleDataService {
    
    private final AbstractPlugin plugin;
    
    public ExampleDataService(AbstractPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Example method demonstrating user data storage.
     */
    public void saveUserPreference(String userId, String key, Object value) {
        if (!plugin.getConfiguration().getBoolean("storage.enabled", true)) {
            plugin.logger.warn("Storage is disabled, cannot save user preference");
            return;
        }
        
        try {
            plugin.getPluginDataStorage().getUserStorage(userId).set(key, value);
            plugin.getPluginDataStorage().saveAll();
            
            plugin.logger.debug("Saved user preference: {} = {} for user {}", 
                               key, value, userId);
        } catch (Exception e) {
            plugin.logger.error("Failed to save user preference", e);
        }
    }
    
    /**
     * Example method demonstrating user data retrieval.
     */
    public <T> T getUserPreference(String userId, String key, T defaultValue) {
        if (!plugin.getConfiguration().getBoolean("storage.enabled", true)) {
            return defaultValue;
        }
        
        try {
            return plugin.getPluginDataStorage().getUserStorage(userId).get(key, defaultValue);
        } catch (Exception e) {
            plugin.logger.error("Failed to get user preference", e);
            return defaultValue;
        }
    }
    
    /**
     * Example method demonstrating guild data management.
     */
    public void updateGuildSetting(String guildId, String setting, Object value) {
        try {
            plugin.getPluginDataStorage().getGuildStorage(guildId).set("settings." + setting, value);
            plugin.getPluginDataStorage().saveAll();
            
            plugin.logger.info("Updated guild setting {} = {} for guild {}", 
                              setting, value, guildId);
        } catch (Exception e) {
            plugin.logger.error("Failed to update guild setting", e);
        }
    }
    
    /**
     * Example method demonstrating plugin statistics.
     */
    public void incrementUsageCounter(String feature) {
        try {
            long currentCount = plugin.getPluginDataStorage().getGlobalStorage()
                    .get("stats." + feature, 0L);
            plugin.getPluginDataStorage().getGlobalStorage()
                    .set("stats." + feature, currentCount + 1);
            
            // Save periodically (not every increment for performance)
            if (currentCount % 10 == 0) {
                plugin.getPluginDataStorage().saveAll();
            }
        } catch (Exception e) {
            plugin.logger.error("Failed to increment usage counter", e);
        }
    }
    
    /**
     * Cleanup method to be called on plugin disable.
     */
    public void cleanup() {
        try {
            plugin.getPluginDataStorage().saveAll();
            plugin.logger.info("Data service cleanup completed");
        } catch (Exception e) {
            plugin.logger.error("Failed to cleanup data service", e);
        }
    }
}
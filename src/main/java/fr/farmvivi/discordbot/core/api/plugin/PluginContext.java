package fr.farmvivi.discordbot.core.api.plugin;

import fr.farmvivi.discordbot.core.api.audio.AudioService;
import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.api.permissions.PermissionManager;
import fr.farmvivi.discordbot.core.api.storage.DataStorageManager;
import fr.farmvivi.discordbot.core.api.storage.binary.BinaryStorageManager;
import org.slf4j.Logger;

/**
 * Provides access to core functionality and services for plugins.
 */
public interface PluginContext {
    /**
     * Gets the logger for the plugin.
     *
     * @return the logger instance
     */
    Logger getLogger();

    /**
     * Gets the event manager for registering and handling events.
     *
     * @return the event manager
     */
    EventManager getEventManager();

    /**
     * Gets the Discord API for interacting with Discord.
     *
     * @return the Discord API
     */
    DiscordAPI getDiscordAPI();

    /**
     * Gets the configuration for the plugin.
     *
     * @return the configuration
     */
    Configuration getConfiguration();

    /**
     * Gets the data folder path for the plugin to store its files.
     *
     * @return the plugin's data folder path
     */
    String getDataFolder();

    /**
     * Gets the plugin loader, which can be used to access other plugins.
     *
     * @return the plugin loader
     */
    PluginLoader getPluginLoader();

    /**
     * Gets the language manager for internationalization.
     *
     * @return the language manager
     */
    LanguageManager getLanguageManager();

    /**
     * Gets the data storage manager for persistent data.
     *
     * @return the data storage manager
     */
    DataStorageManager getDataStorageManager();

    /**
     * Gets the binary storage manager for large file storage.
     *
     * @return the binary storage manager
     */
    BinaryStorageManager getBinaryStorageManager();

    /**
     * Gets the permission manager for registering and checking permissions.
     *
     * @return the permission manager
     */
    PermissionManager getPermissionManager();
    
    /**
     * Gets the audio service for managing audio connections.
     *
     * @return the audio service, or null if audio is disabled
     */
    AudioService getAudioService();
}
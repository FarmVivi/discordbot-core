package fr.farmvivi.discordbot.core.api.plugin;

import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.data.DataStorageProvider;
import fr.farmvivi.discordbot.core.api.data.binary.BinaryStorageProvider;
import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import org.slf4j.Logger;

/**
 * Provides access to core functionality and services for plugins.
 * This is passed to plugins during initialization to provide them
 * with access to the bot's core services.
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
     * Gets a data folder for the plugin to store its files.
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
     * Gets the class loader for this plugin.
     *
     * @return the plugin's class loader
     */
    ClassLoader getClassLoader();

    /**
     * Gets the language manager for internationalization.
     *
     * @return the language manager
     */
    LanguageManager getLanguageManager();

    /**
     * Gets the data storage provider for persistent data.
     *
     * @return the data storage provider
     */
    DataStorageProvider getDataStorageProvider();

    /**
     * Gets the binary storage provider for large file storage.
     *
     * @return the binary storage provider
     */
    BinaryStorageProvider getBinaryStorageProvider();
}
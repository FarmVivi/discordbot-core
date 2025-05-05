package fr.farmvivi.discordbot.core.plugin;

import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.data.DataStorageProvider;
import fr.farmvivi.discordbot.core.api.data.binary.BinaryStorageProvider;
import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.api.plugin.PluginContext;
import fr.farmvivi.discordbot.core.api.plugin.PluginLoader;
import org.slf4j.Logger;

/**
 * Implementation of PluginContext.
 */
record PluginContextImpl(Logger getLogger, EventManager getEventManager, DiscordAPI getDiscordAPI,
                         Configuration getConfiguration, String getDataFolder, PluginLoader getPluginLoader,
                         ClassLoader getClassLoader, LanguageManager getLanguageManager,
                         DataStorageProvider getDataStorageProvider, BinaryStorageProvider getBinaryStorageProvider)
        implements PluginContext {
}
package fr.farmvivi.discordbot.core.plugin;

import fr.farmvivi.discordbot.api.audio.AudioService;
import fr.farmvivi.discordbot.api.command.CommandService;
import fr.farmvivi.discordbot.api.config.Configuration;
import fr.farmvivi.discordbot.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.api.event.EventManager;
import fr.farmvivi.discordbot.api.language.LanguageManager;
import fr.farmvivi.discordbot.api.permissions.PermissionManager;
import fr.farmvivi.discordbot.api.plugin.PluginContext;
import fr.farmvivi.discordbot.api.plugin.PluginLoader;
import fr.farmvivi.discordbot.api.storage.DataStorageManager;
import fr.farmvivi.discordbot.api.storage.binary.BinaryStorageManager;
import org.slf4j.Logger;

/**
 * Implementation of PluginContext that provides access to core services.
 */
public record PluginContextImpl(
        Logger getLogger,
        EventManager getEventManager,
        DiscordAPI getDiscordAPI,
        Configuration getConfiguration,
        String getDataFolder,
        PluginLoader getPluginLoader,
        ClassLoader getClassLoader,
        LanguageManager getLanguageManager,
        DataStorageManager getDataStorageManager,
        BinaryStorageManager getBinaryStorageManager,
        PermissionManager getPermissionManager,
        AudioService getAudioService,
        CommandService getCommandService
) implements PluginContext {
}
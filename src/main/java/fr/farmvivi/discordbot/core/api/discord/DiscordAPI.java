package fr.farmvivi.discordbot.core.api.discord;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for interacting with Discord.
 */
public interface DiscordAPI {
    /**
     * Gets the underlying JDA instance.
     *
     * @return the JDA instance
     */
    JDA getJDA();

    /**
     * Registers a Discord event listener.
     *
     * @param listener the listener to register
     * @param plugin   the plugin registering the listener
     */
    void registerListener(ListenerAdapter listener, Plugin plugin);

    /**
     * Unregisters a Discord event listener.
     *
     * @param listener the listener to unregister
     */
    void unregisterListener(ListenerAdapter listener);

    /**
     * Unregisters all Discord event listeners from a specific plugin.
     *
     * @param plugin the plugin whose listeners should be unregistered
     */
    void unregisterAllListeners(Plugin plugin);

    /**
     * Registers a slash command.
     *
     * @param command the command to register
     * @param plugin  the plugin registering the command
     * @return a CompletableFuture that completes when the command is registered
     */
    CompletableFuture<Void> registerCommand(SlashCommandData command, Plugin plugin);

    /**
     * Unregisters a slash command.
     *
     * @param name the name of the command to unregister
     * @return a CompletableFuture that completes when the command is unregistered
     */
    CompletableFuture<Void> unregisterCommand(String name);

    /**
     * Unregisters all slash commands from a specific plugin.
     *
     * @param plugin the plugin whose commands should be unregistered
     * @return a CompletableFuture that completes when all commands are unregistered
     */
    CompletableFuture<Void> unregisterAllCommands(Plugin plugin);

    /**
     * Gets all registered slash commands.
     *
     * @return a list of all registered slash commands
     */
    List<SlashCommandData> getRegisteredCommands();

    /**
     * Connects to Discord.
     *
     * @return a CompletableFuture that completes when connected
     */
    CompletableFuture<Void> connect();

    /**
     * Disconnects from Discord.
     *
     * @return a CompletableFuture that completes when disconnected
     */
    CompletableFuture<Void> disconnect();

    /**
     * Checks if connected to Discord.
     *
     * @return true if connected
     */
    boolean isConnected();
}
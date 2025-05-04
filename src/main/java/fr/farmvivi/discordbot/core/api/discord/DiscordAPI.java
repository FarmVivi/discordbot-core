package fr.farmvivi.discordbot.core.api.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for interacting with Discord.
 */
public interface DiscordAPI {
    /**
     * Gets the underlying JDA instance.
     *
     * @return the JDA instance or null if not connected
     */
    JDA getJDA();

    /**
     * Gets the JDABuilder instance that will be used to connect.
     * This can be modified by plugins in their pre-enable phase.
     *
     * @return the JDABuilder instance
     */
    JDABuilder getBuilder();

    /**
     * Connects to Discord using the configured JDABuilder.
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

    /**
     * Gets the default activity for the bot.
     *
     * @return the default activity
     */
    Activity getDefaultActivity();

    /**
     * Sets the bot's activity to the default activity.
     */
    void setDefaultActivity();

    /**
     * Checks if the bot is currently displaying the default activity.
     *
     * @return true if the current activity is the default activity
     */
    boolean isDefaultActivity();
}
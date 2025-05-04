package fr.farmvivi.discordbot.core.api.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
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
     * Gets the startup activity for the bot.
     *
     * @return the startup activity
     */
    Activity getStartupActivity();

    /**
     * Gets the startup online status for the bot.
     *
     * @return the startup online status
     */
    OnlineStatus getStartupStatus();

    /**
     * Sets the bot's presence to a startup state (activity + online status).
     */
    void setStartupPresence();

    /**
     * Sets the bot's startup presence with the specified activity and status.
     *
     * @param activity the activity to set
     * @param status   the online status to set
     */
    void setStartupPresence(Activity activity, OnlineStatus status);

    /**
     * Checks if the bot is currently displaying the startup presence.
     *
     * @return true if the current presence matches the startup presence
     */
    boolean isStartupPresence();

    /**
     * Gets the default activity for the bot.
     *
     * @return the default activity
     */
    Activity getDefaultActivity();

    /**
     * Gets the default online status for the bot.
     *
     * @return the default online status
     */
    OnlineStatus getDefaultStatus();

    /**
     * Sets the bot's presence to the default state (activity + online status).
     */
    void setDefaultPresence();

    /**
     * Sets the bot's default presence with the specified activity and status.
     *
     * @param activity the activity to set
     * @param status   the online status to set
     */
    void setDefaultPresence(Activity activity, OnlineStatus status);

    /**
     * Checks if the bot is currently displaying the default presence.
     *
     * @return true if the current presence matches the default presence
     */
    boolean isDefaultPresence();

    /**
     * Gets the shutdown activity for the bot.
     *
     * @return the shutdown activity
     */
    Activity getShutdownActivity();

    /**
     * Gets the shutdown online status for the bot.
     *
     * @return the shutdown online status
     */
    OnlineStatus getShutdownStatus();

    /**
     * Sets the bot's presence to a shutdown state (activity + online status).
     */
    void setShutdownPresence();

    /**
     * Sets the bot's shutdown presence with the specified activity and status.
     *
     * @param activity the activity to set
     * @param status   the online status to set
     */
    void setShutdownPresence(Activity activity, OnlineStatus status);

    /**
     * Checks if the bot is currently displaying the shutdown presence.
     *
     * @return true if the current presence matches the shutdown presence
     */
    boolean isShutdownPresence();
}
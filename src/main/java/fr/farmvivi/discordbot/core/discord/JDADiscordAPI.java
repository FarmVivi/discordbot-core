package fr.farmvivi.discordbot.core.discord;

import fr.farmvivi.discordbot.core.Discobocor;
import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Implementation of DiscordAPI using JDA.
 */
public class JDADiscordAPI implements DiscordAPI {
    private static final Logger logger = LoggerFactory.getLogger(JDADiscordAPI.class);

    private final String token;
    private JDA jda;
    private JDABuilder builder;
    private Activity startupActivity = Activity.playing("starting up...");
    private OnlineStatus startupStatus = OnlineStatus.IDLE;
    private Activity defaultActivity = Activity.playing("v" + Discobocor.VERSION);
    private OnlineStatus defaultStatus = OnlineStatus.ONLINE;
    private Activity shutdownActivity = Activity.playing("shutting down...");
    private OnlineStatus shutdownStatus = OnlineStatus.DO_NOT_DISTURB;

    /**
     * Creates a new JDADiscordAPI.
     *
     * @param token the Discord bot token
     */
    public JDADiscordAPI(String token) {
        this.token = token;

        // Initialize the builder with default settings
        initializeBuilder();
    }

    /**
     * Initializes the JDABuilder with default settings.
     */
    private void initializeBuilder() {
        builder = JDABuilder.createDefault(token)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_VOICE_STATES
                )
                .enableCache(
                        CacheFlag.MEMBER_OVERRIDES,
                        CacheFlag.VOICE_STATE,
                        CacheFlag.EMOJI,
                        CacheFlag.ACTIVITY
                )
                .setAutoReconnect(true);
    }

    @Override
    public JDABuilder getBuilder() {
        return builder;
    }

    @Override
    public JDA getJDA() {
        return jda;
    }

    @Override
    public CompletableFuture<Void> connect() {
        if (jda != null) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            // Use the builder that might have been modified by plugins
            jda = builder.build().awaitReady();
            logger.info("Connected to Discord as {}", jda.getSelfUser().getAsTag());
            future.complete(null);
        } catch (Exception e) {
            logger.error("Failed to connect to Discord", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        if (jda == null) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            // Set shutdown presence before disconnecting
            setShutdownPresence();

            // Give a short delay for the presence to update
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }

            jda.shutdown();
            jda = null;

            // Reinitialize the builder for future connections
            initializeBuilder();

            logger.info("Disconnected from Discord");
            future.complete(null);
        } catch (Exception e) {
            logger.error("Failed to disconnect from Discord", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public boolean isConnected() {
        return jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    @Override
    public Activity getStartupActivity() {
        return startupActivity;
    }

    @Override
    public OnlineStatus getStartupStatus() {
        return startupStatus;
    }

    @Override
    public void setStartupPresence() {
        if (isConnected()) {
            jda.getPresence().setPresence(startupStatus, startupActivity);
            logger.debug("Set startup presence: {} with status {}",
                    startupActivity.getName(), startupStatus);
        }
    }

    @Override
    public void setStartupPresence(Activity activity, OnlineStatus status) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        this.startupActivity = activity;
        this.startupStatus = status;

        setStartupPresence();
    }

    @Override
    public boolean isStartupPresence() {
        if (!isConnected()) {
            return false;
        }

        Activity currentActivity = jda.getPresence().getActivity();
        OnlineStatus currentStatus = jda.getPresence().getStatus();

        return currentStatus == startupStatus &&
                (currentActivity != null && currentActivity.equals(startupActivity));
    }

    @Override
    public Activity getDefaultActivity() {
        return defaultActivity;
    }

    @Override
    public OnlineStatus getDefaultStatus() {
        return defaultStatus;
    }

    @Override
    public void setDefaultPresence() {
        if (isConnected()) {
            jda.getPresence().setPresence(defaultStatus, defaultActivity);
            logger.debug("Set default presence: {} with status {}",
                    defaultActivity.getName(), defaultStatus);
        }
    }

    @Override
    public void setDefaultPresence(Activity activity, OnlineStatus status) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        this.defaultActivity = activity;
        this.defaultStatus = status;

        setDefaultPresence();
    }

    @Override
    public boolean isDefaultPresence() {
        if (!isConnected()) {
            return false;
        }

        Activity currentActivity = jda.getPresence().getActivity();
        OnlineStatus currentStatus = jda.getPresence().getStatus();

        return currentStatus == defaultStatus &&
                (currentActivity != null && currentActivity.equals(defaultActivity));
    }

    @Override
    public Activity getShutdownActivity() {
        return shutdownActivity;
    }

    @Override
    public OnlineStatus getShutdownStatus() {
        return shutdownStatus;
    }

    @Override
    public void setShutdownPresence() {
        if (isConnected()) {
            jda.getPresence().setPresence(shutdownStatus, shutdownActivity);
            logger.debug("Set shutdown presence: {} with status {}",
                    shutdownActivity.getName(), shutdownStatus);
        }
    }

    @Override
    public void setShutdownPresence(Activity activity, OnlineStatus status) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        this.shutdownActivity = activity;
        this.shutdownStatus = status;

        setShutdownPresence();
    }

    @Override
    public boolean isShutdownPresence() {
        if (!isConnected()) {
            return false;
        }

        Activity currentActivity = jda.getPresence().getActivity();
        OnlineStatus currentStatus = jda.getPresence().getStatus();

        return currentStatus == shutdownStatus &&
                (currentActivity != null && currentActivity.equals(shutdownActivity));
    }
}
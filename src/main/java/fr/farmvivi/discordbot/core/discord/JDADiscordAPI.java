package fr.farmvivi.discordbot.core.discord;

import fr.farmvivi.discordbot.core.Discobocor;
import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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
    public Activity getDefaultActivity() {
        return Activity.playing("v" + Discobocor.VERSION);
    }

    @Override
    public void setDefaultActivity() {
        if (isConnected()) {
            jda.getPresence().setActivity(getDefaultActivity());
        }
    }

    @Override
    public boolean isDefaultActivity() {
        if (!isConnected() || jda.getPresence().getActivity() == null) {
            return false;
        }
        return jda.getPresence().getActivity().equals(getDefaultActivity());
    }
}
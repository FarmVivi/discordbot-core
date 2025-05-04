package fr.farmvivi.discordbot.core.discord;

import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of DiscordAPI using JDA.
 */
public class JDADiscordAPI implements DiscordAPI {
    private static final Logger logger = LoggerFactory.getLogger(JDADiscordAPI.class);

    private final String token;
    private final EventManager eventManager;
    private JDA jda;

    private final Map<ListenerAdapter, Object> listeners = new ConcurrentHashMap<>();

    /**
     * Creates a new JDADiscordAPI.
     *
     * @param token        the Discord bot token
     * @param eventManager the event manager
     */
    public JDADiscordAPI(String token, EventManager eventManager) {
        this.token = token;
        this.eventManager = eventManager;
    }

    @Override
    public JDA getJDA() {
        return jda;
    }

    @Override
    public void registerListener(ListenerAdapter listener, Plugin plugin) {
        if (jda != null) {
            jda.addEventListener(listener);
            listeners.put(listener, plugin);
            logger.debug("Registered listener: {}", listener.getClass().getName());
        }
    }

    @Override
    public void unregisterListener(ListenerAdapter listener) {
        if (jda != null) {
            jda.removeEventListener(listener);
            listeners.remove(listener);
            logger.debug("Unregistered listener: {}", listener.getClass().getName());
        }
    }

    @Override
    public void unregisterAllListeners(Plugin plugin) {
        List<ListenerAdapter> toRemove = new ArrayList<>();
        for (Map.Entry<ListenerAdapter, Object> entry : listeners.entrySet()) {
            if (entry.getValue() == plugin) {
                toRemove.add(entry.getKey());
            }
        }

        for (ListenerAdapter listener : toRemove) {
            unregisterListener(listener);
        }

        logger.debug("Unregistered all listeners for plugin: {}", plugin.getClass().getName());
    }

    @Override
    public CompletableFuture<Void> connect() {
        if (jda != null) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            JDABuilder builder = JDABuilder.createDefault(token)
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
            listeners.clear();
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
}
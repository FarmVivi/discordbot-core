package fr.farmvivi.discordbot.core.discord;

import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
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
    private final Map<String, Object> commands = new ConcurrentHashMap<>();
    private final Map<String, SlashCommandData> commandsData = new ConcurrentHashMap<>();

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
    public CompletableFuture<Void> registerCommand(SlashCommandData command, Plugin plugin) {
        if (jda != null) {
            commands.put(command.getName(), plugin);
            commandsData.put(command.getName(), command);
            logger.debug("Registered command: {}", command.getName());

            return updateCommands();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> unregisterCommand(String name) {
        if (jda != null && commands.containsKey(name)) {
            commands.remove(name);
            commandsData.remove(name);
            logger.debug("Unregistered command: {}", name);

            return updateCommands();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> unregisterAllCommands(Plugin plugin) {
        if (jda != null) {
            List<String> toRemove = new ArrayList<>();
            for (Map.Entry<String, Object> entry : commands.entrySet()) {
                if (entry.getValue() == plugin) {
                    toRemove.add(entry.getKey());
                }
            }

            for (String name : toRemove) {
                commands.remove(name);
                commandsData.remove(name);
                logger.debug("Unregistered command: {}", name);
            }

            return updateCommands();
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Updates the commands with Discord.
     *
     * @return a CompletableFuture that completes when the commands are updated
     */
    private CompletableFuture<Void> updateCommands() {
        if (jda != null) {
            return jda.updateCommands().addCommands(new ArrayList<>(commandsData.values())).submit()
                    .thenApply(v -> null);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public List<SlashCommandData> getRegisteredCommands() {
        return new ArrayList<>(commandsData.values());
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
            commands.clear();
            commandsData.clear();
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
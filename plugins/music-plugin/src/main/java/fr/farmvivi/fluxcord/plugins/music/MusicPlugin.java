package fr.farmvivi.fluxcord.plugins.music;

import fr.farmvivi.fluxcord.api.event.EventHandler;
import fr.farmvivi.fluxcord.api.event.EventPriority;
import fr.farmvivi.fluxcord.api.permissions.Permission;
import fr.farmvivi.fluxcord.api.permissions.PermissionDefault;
import fr.farmvivi.fluxcord.api.plugin.AbstractPlugin;
import fr.farmvivi.fluxcord.plugins.music.audio.FluxcordAudioManager;
import fr.farmvivi.fluxcord.plugins.music.commands.*;
import fr.farmvivi.fluxcord.plugins.music.config.MusicConfig;
import fr.farmvivi.fluxcord.plugins.music.player.GuildMusicManager;
import fr.farmvivi.fluxcord.plugins.music.player.PersistentMusicPlayerMessage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import java.util.Map;

/**
 * Advanced music bot plugin for Fluxcord.
 * <p>
 * Features:
 * - Play music from YouTube, Spotify, SoundCloud
 * - Playlist management and queue controls
 * - Audio effects and volume control
 * - Rich embeds with now playing information
 * - Search functionality and favorites
 * - Persistent music player message with interactive controls
 */
public class MusicPlugin extends AbstractPlugin {
    
    private MusicManager musicManager;
    private PlaylistManager playlistManager;
    private FluxcordAudioManager audioManager;
    private MusicConfig musicConfig;
    
    // Per-guild music managers
    private final Map<String, GuildMusicManager> guildMusicManagers = new ConcurrentHashMap<>();
    
    // Persistent player messages
    private final Map<String, PersistentMusicPlayerMessage> playerMessages = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        logger.info("Music Plugin enabling...");

        // Load configuration
        musicConfig = new MusicConfig(this);
        
        // Register permissions
        registerPermissions();

        // Initialize managers
        this.audioManager = new FluxcordAudioManager(this, audioService);
        this.musicManager = new MusicManager(this);
        this.playlistManager = new PlaylistManager(this);

        // Register commands
        registerCommands();

        // Load persistent player messages
        loadPersistentMessages();

        logger.info("Music Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        logger.info("Music Plugin disabling...");

        // Save persistent player messages
        savePersistentMessages();

        // Cleanup audio connections
        if (musicManager != null) {
            musicManager.shutdown();
        }

        if (playlistManager != null) {
            playlistManager.saveAllPlaylists();
        }

        // Clear guild managers
        guildMusicManagers.clear();
        playerMessages.clear();

        logger.info("Music Plugin disabled!");
    }

    private void registerPermissions() {
        // Register music permissions
        registerPermission("music.play", "Allow playing music", PermissionDefault.TRUE);
        registerPermission("music.skip", "Allow skipping tracks", PermissionDefault.TRUE);
        registerPermission("music.volume", "Allow changing volume", PermissionDefault.TRUE);
        registerPermission("music.queue", "Allow viewing and managing queue", PermissionDefault.TRUE);
        registerPermission("music.playlist", "Allow playlist management", PermissionDefault.TRUE);
        registerPermission("music.admin", "Allow administrative music controls", PermissionDefault.FALSE);
    }

    private void registerCommands() {
        // Basic playback commands
        pluginCommandAdapter.registerCommand(builder -> {
            builder.name("play")
                   .description("Play music from various sources")
                   .category("Music")
                   .permission("music.play")
                   .stringOption("query", "Song name, URL, or search term", true)
                   .executor(new PlayCommand(this));
        });

        pluginCommandAdapter.registerCommand(builder -> {
            builder.name("pause")
                   .description("Pause/resume music playback")
                   .category("Music")
                   .permission("music.play")
                   .executor(new PauseCommand(this));
        });

        pluginCommandAdapter.registerCommand(builder -> {
            builder.name("skip")
                   .description("Skip the current track")
                   .category("Music")
                   .permission("music.skip")
                   .executor(new SkipCommand(this));
        });

        pluginCommandAdapter.registerCommand(builder -> {
            builder.name("stop")
                   .description("Stop music and clear queue")
                   .category("Music")
                   .permission("music.admin")
                   .executor(new StopCommand(this));
        });

        pluginCommandAdapter.registerCommand(builder -> {
            builder.name("nowplaying")
                   .description("Show current playing track")
                   .category("Music")
                   .permission("music.play")
                   .aliases("np", "current")
                   .executor(new NowPlayingCommand(this));
        });

        // Queue management commands
        pluginCommandAdapter.registerCommand(builder -> {
            builder.name("queue")
                   .description("Show the music queue")
                   .category("Music")
                   .permission("music.queue")
                   .integerOption("page", "Page number", false)
                   .executor(new QueueCommand(this));
        });

        pluginCommandAdapter.registerCommand(builder -> {
            builder.name("clear")
                   .description("Clear the music queue")
                   .category("Music")
                   .permission("music.admin")
                   .executor(new ClearCommand(this));
        });

        pluginCommandAdapter.registerCommand(builder -> {
            builder.name("shuffle")
                   .description("Toggle shuffle mode")
                   .category("Music")
                   .permission("music.queue")
                   .executor(new ShuffleCommand(this));
        });

        pluginCommandAdapter.registerCommand(builder -> {
            builder.name("loop")
                   .description("Toggle loop mode")
                   .category("Music")
                   .permission("music.queue")
                   .stringOption("mode", "Loop mode: off, track, queue", false)
                   .executor(new LoopCommand(this));
        });

        // Volume and audio commands
        pluginCommandAdapter.registerCommand(builder -> {
            builder.name("volume")
                   .description("Set or view the volume")
                   .category("Music")
                   .permission("music.volume")
                   .integerOption("level", "Volume level (0-100)", false)
                   .executor(new VolumeCommand(this));
        });

        // Additional commands will be added as needed
    }

    private void loadPersistentMessages() {
        // Load persistent message data from storage
        Optional<Map> persistentData = getPluginDataStorage().getGlobalStorage().get("persistent_messages", Map.class);
        if (persistentData.isPresent()) {
            persistentData.get().forEach((guildId, data) -> {
                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> messageData = (Map<String, Object>) data;
                    PersistentMusicPlayerMessage playerMessage = new PersistentMusicPlayerMessage(this, (String) guildId);
                    playerMessage.loadFromData(messageData);
                    playerMessages.put((String) guildId, playerMessage);
                }
            });
        }
    }

    private void savePersistentMessages() {
        // Save persistent message data to storage
        Map<String, Map<String, Object>> persistentData = new ConcurrentHashMap<>();
        playerMessages.forEach((guildId, playerMessage) -> {
            persistentData.put(guildId, playerMessage.saveToData());
        });
        getPluginDataStorage().getGlobalStorage().set("persistent_messages", persistentData);
    }

    private void registerPermission(String permission, String description, PermissionDefault defaultValue) {
        Permission perm = new SimplePermission(permission, description, defaultValue);
        getPluginPermissionManager().registerPermission(perm);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (musicManager != null) {
            musicManager.handleVoiceUpdate(event);
        }
    }

    /**
     * Get or create a guild music manager for the specified guild.
     *
     * @param guild the guild
     * @return the guild music manager
     */
    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return guildMusicManagers.computeIfAbsent(guild.getId(), 
            id -> new GuildMusicManager(this, guild));
    }

    /**
     * Get or create a persistent player message for the specified guild.
     *
     * @param guild the guild
     * @return the persistent player message
     */
    public PersistentMusicPlayerMessage getPersistentPlayerMessage(Guild guild) {
        return playerMessages.computeIfAbsent(guild.getId(),
            id -> new PersistentMusicPlayerMessage(this, id));
    }

    // Getters for managers
    public MusicManager getMusicManager() {
        return musicManager;
    }

    public PlaylistManager getPlaylistManager() {
        return playlistManager;
    }

    public FluxcordAudioManager getAudioManager() {
        return audioManager;
    }

    public MusicConfig getMusicConfig() {
        return musicConfig;
    }
}

// Internal simple permission implementation (mirrors template approach)
class SimplePermission implements Permission {
    private final String name;
    private final String description;
    private final PermissionDefault defaultValue;

    public SimplePermission(String name, String description, PermissionDefault defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public PermissionDefault getDefault() {
        return defaultValue;
    }
}
package fr.farmvivi.discordbot.plugins.music;

import fr.farmvivi.discordbot.api.event.EventHandler;
import fr.farmvivi.discordbot.api.event.EventPriority;
import fr.farmvivi.discordbot.api.permissions.Permission;
import fr.farmvivi.discordbot.api.permissions.PermissionDefault;
import fr.farmvivi.discordbot.api.plugin.AbstractPlugin;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

/**
 * Advanced music bot plugin for DiscordBot Core.
 * 
 * Features:
 * - Play music from YouTube, Spotify, SoundCloud
 * - Playlist management and queue controls
 * - Audio effects and volume control
 * - Rich embeds with now playing information
 * - Search functionality and favorites
 */
public class MusicPlugin extends AbstractPlugin {

    private MusicManager musicManager;
    private PlaylistManager playlistManager;

    @Override
    public void onEnable() {
        logger.info("Music Plugin enabling...");
        
        // Register permissions
        registerPermissions();
        
        // Initialize managers
        this.musicManager = new MusicManager(this);
        this.playlistManager = new PlaylistManager(this);
        
        // Load configuration
        loadConfiguration();
        
        logger.info("Music Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        logger.info("Music Plugin disabling...");
        
        if (musicManager != null) {
            musicManager.shutdown();
        }
        
        if (playlistManager != null) {
            playlistManager.saveAllPlaylists();
        }
        
        logger.info("Music Plugin disabled!");
    }

    private void registerPermissions() {
        registerPermissionNode("play", "Allows playing tracks", PermissionDefault.TRUE);
        registerPermissionNode("skip", "Allows skipping current track", PermissionDefault.TRUE);
        registerPermissionNode("queue", "Allows viewing the queue", PermissionDefault.TRUE);
        registerPermissionNode("volume", "Allows changing playback volume", PermissionDefault.OP);
        registerPermissionNode("playlist", "Allows managing playlists", PermissionDefault.TRUE);
        registerPermissionNode("admin", "Allows moderator music actions", PermissionDefault.OP);
    }

    private void registerPermissionNode(String node, String description, PermissionDefault def) {
        getPluginPermissionManager().registerPermission(new SimplePermission(permissionKey(node), description, def));
    }

    private String permissionKey(String node) {
        return getName().toLowerCase() + "." + node;
    }

    private void loadConfiguration() {
        int defaultVolume = getConfiguration().getInt("music.default_volume", 50);
        int maxQueue = getConfiguration().getInt("music.max_queue_size", 100);
        int maxTrackDurationMs = getConfiguration().getInt("music.max_track_duration", 600_000);
        boolean enableSpotify = getConfiguration().getBoolean("music.enable_spotify", true);
        boolean enableSoundcloud = getConfiguration().getBoolean("music.enable_soundcloud", true);
        int autoLeaveTimeoutMs = getConfiguration().getInt("music.auto_leave_timeout", 300_000);
        logger.info("Music config loaded: vol={}, queue={}, maxTrackMs={}, spotify={}, soundcloud={}, autoLeaveMs={}",
                defaultVolume, maxQueue, maxTrackDurationMs, enableSpotify, enableSoundcloud, autoLeaveTimeoutMs);
    }

    // TODO: Implement commands when command API is available
    /*
    @Command(name = "play", description = "Play music from a URL or search query")
    public CommandResult playCommand(CommandContext ctx) {
        // TODO: Implement play command
        ctx.reply("🎵 Play command - Implementation coming soon!");
        return CommandResult.SUCCESS;
    }
    */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVoiceUpdate(GuildVoiceUpdateEvent event) {
        // TODO: Handle voice channel events for auto-leave functionality
        if (musicManager != null) {
            musicManager.handleVoiceUpdate(event);
        }
    }

    // Getters for managers (used by other classes)
    public MusicManager getMusicManager() {
        return musicManager;
    }

    public PlaylistManager getPlaylistManager() {
        return playlistManager;
    }
}

// Internal simple permission implementation (mirrors template approach)
class SimplePermission implements Permission {
    private final String name;
    private final String description;
    private final PermissionDefault def;

    public SimplePermission(String name, String description, PermissionDefault def) {
        this.name = name;
        this.description = description;
        this.def = def;
    }

    @Override
    public String getName() { return name; }

    @Override
    public String getDescription() { return description; }

    @Override
    public PermissionDefault getDefault() { return def; }
}
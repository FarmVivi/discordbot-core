package fr.farmvivi.discordbot.plugins.music;

import fr.farmvivi.discordbot.core.api.event.EventHandler;
import fr.farmvivi.discordbot.core.api.permissions.PermissionDefault;
import fr.farmvivi.discordbot.core.api.plugin.AbstractPlugin;
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
    public String getName() {
        return "MusicBot";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

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
        getPluginPermissionManager().registerPermission("music.play", PermissionDefault.TRUE);
        getPluginPermissionManager().registerPermission("music.skip", PermissionDefault.TRUE);
        getPluginPermissionManager().registerPermission("music.queue", PermissionDefault.TRUE);
        getPluginPermissionManager().registerPermission("music.volume", PermissionDefault.OPERATOR);
        getPluginPermissionManager().registerPermission("music.playlist", PermissionDefault.TRUE);
        getPluginPermissionManager().registerPermission("music.admin", PermissionDefault.OPERATOR);
    }

    private void loadConfiguration() {
        // Set default configuration values
        getConfiguration().set("music.default_volume", 50);
        getConfiguration().set("music.max_queue_size", 100);
        getConfiguration().set("music.max_track_duration", 600000); // 10 minutes in milliseconds
        getConfiguration().set("music.enable_spotify", true);
        getConfiguration().set("music.enable_soundcloud", true);
        getConfiguration().set("music.auto_leave_timeout", 300000); // 5 minutes
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

    @EventHandler
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
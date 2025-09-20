package fr.farmvivi.fluxcord.plugins.music.config;

import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;

/**
 * Configuration management for the music plugin.
 */
public class MusicConfig {
    private final MusicPlugin plugin;
    
    // Configuration values with defaults
    private final int defaultVolume;
    private final int maxQueueSize;
    private final int maxTrackDuration;
    private final boolean enableSpotify;
    private final boolean enableSoundCloud;
    private final int autoLeaveTimeout;
    private final String audioQuality;
    
    public MusicConfig(MusicPlugin plugin) {
        this.plugin = plugin;
        
        // Load configuration values with defaults
        this.defaultVolume = plugin.getConfiguration().getInt("music.default_volume", 50);
        this.maxQueueSize = plugin.getConfiguration().getInt("music.max_queue_size", 100);
        this.maxTrackDuration = plugin.getConfiguration().getInt("music.max_track_duration", 600_000); // 10 minutes
        this.enableSpotify = plugin.getConfiguration().getBoolean("music.enable_spotify", true);
        this.enableSoundCloud = plugin.getConfiguration().getBoolean("music.enable_soundcloud", true);
        this.autoLeaveTimeout = plugin.getConfiguration().getInt("music.auto_leave_timeout", 300_000); // 5 minutes
        this.audioQuality = plugin.getConfiguration().getString("music.audio_quality", "medium");
        
        plugin.getLogger().info("Music configuration loaded - volume: {}, maxQueue: {}, maxDuration: {}ms, " +
            "spotify: {}, soundcloud: {}, autoLeave: {}ms, quality: {}", 
            defaultVolume, maxQueueSize, maxTrackDuration, enableSpotify, enableSoundCloud, 
            autoLeaveTimeout, audioQuality);
    }
    
    public int getDefaultVolume() {
        return defaultVolume;
    }
    
    public int getMaxQueueSize() {
        return maxQueueSize;
    }
    
    public int getMaxTrackDuration() {
        return maxTrackDuration;
    }
    
    public boolean isSpotifyEnabled() {
        return enableSpotify;
    }
    
    public boolean isSoundCloudEnabled() {
        return enableSoundCloud;
    }
    
    public int getAutoLeaveTimeout() {
        return autoLeaveTimeout;
    }
    
    public String getAudioQuality() {
        return audioQuality;
    }
    
    // API Keys and configuration (from environment or config)
    public String getSpotifyClientId() {
        return plugin.getConfiguration().getString("SPOTIFY_ID", null);
    }
    
    public String getSpotifyClientSecret() {
        return plugin.getConfiguration().getString("SPOTIFY_TOKEN", null);
    }
    
    public String getYouTubeEmail() {
        return plugin.getConfiguration().getString("YOUTUBE_EMAIL", null);
    }
    
    public String getYouTubePassword() {
        return plugin.getConfiguration().getString("YOUTUBE_PASSWORD", null);
    }
    
    public String getDeezerMasterKey() {
        return plugin.getConfiguration().getString("DEEZER_MASTER_DECRYPTION_KEY", null);
    }
}
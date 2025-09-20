package fr.farmvivi.fluxcord.plugins.music.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import fr.farmvivi.fluxcord.api.audio.AudioService;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Audio manager that integrates LavaPlayer with Fluxcord's AudioService.
 * Provides a bridge between the music plugin and the core audio system.
 */
public class FluxcordAudioManager {
    private final MusicPlugin plugin;
    private final AudioService audioService;
    private final AudioPlayerManager audioPlayerManager;
    
    public FluxcordAudioManager(MusicPlugin plugin, AudioService audioService) {
        this.plugin = plugin;
        this.audioService = audioService;
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        
        initializeAudioSources();
    }
    
    private void initializeAudioSources() {
        plugin.getLogger().info("Initializing audio sources...");
        
        // YouTube source with multiple clients for reliability
        YoutubeAudioSourceManager youtubeManager = new YoutubeAudioSourceManager(true,
                new MusicWithThumbnail(),
                new AndroidVrWithThumbnail(),
                new WebWithThumbnail(),
                new WebEmbeddedWithThumbnail(),
                new MWebWithThumbnail(),
                new IosWithThumbnail(),
                new AndroidWithThumbnail(),
                new AndroidMusicWithThumbnail(),
                new TvHtml5EmbeddedWithThumbnail()
        );
        audioPlayerManager.registerSourceManager(youtubeManager);
        
        // SoundCloud (if enabled)
        if (plugin.getMusicConfig().isSoundCloudEnabled()) {
            audioPlayerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        }
        
        // Other audio sources
        audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
        audioPlayerManager.registerSourceManager(new VimeoAudioSourceManager());
        audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        audioPlayerManager.registerSourceManager(new HttpAudioSourceManager());
        audioPlayerManager.registerSourceManager(new LocalAudioSourceManager());
        
        // Enable hot-swapping of audio filters
        audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
        
        plugin.getLogger().info("Audio sources initialized successfully");
    }
    
    /**
     * Register an audio handler for a guild using Fluxcord's AudioService.
     *
     * @param guild the guild
     * @param handler the audio send handler
     * @param volume initial volume (0-100)
     * @param priority audio priority
     */
    public void registerAudioHandler(Guild guild, AudioSendHandler handler, int volume, int priority) {
        audioService.registerSendHandler(guild, plugin, handler, volume, priority);
    }
    
    /**
     * Deregister audio handler for a guild.
     *
     * @param guild the guild
     */
    public void deregisterAudioHandler(Guild guild) {
        audioService.deregisterSendHandler(guild, plugin);
    }
    
    /**
     * Set volume for a guild's audio.
     *
     * @param guild the guild
     * @param volume volume level (0-100)
     */
    public void setVolume(Guild guild, int volume) {
        audioService.setVolume(guild, plugin, volume);
    }
    
    /**
     * Get the LavaPlayer AudioPlayerManager instance.
     *
     * @return the audio player manager
     */
    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }
    
    /**
     * Shutdown the audio manager and cleanup resources.
     */
    public void shutdown() {
        plugin.getLogger().info("Shutting down audio manager...");
        if (audioPlayerManager != null) {
            audioPlayerManager.shutdown();
        }
    }
}
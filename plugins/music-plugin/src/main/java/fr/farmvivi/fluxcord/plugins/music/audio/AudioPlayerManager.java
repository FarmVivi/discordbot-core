package fr.farmvivi.fluxcord.plugins.music.audio;

import com.github.topi314.lavasrc.applemusic.AppleMusicSourceManager;
import com.github.topi314.lavasrc.deezer.DeezerAudioSourceManager;
import com.github.topi314.lavasrc.flowerytts.FloweryTTSSourceManager;
import com.github.topi314.lavasrc.mirror.DefaultMirroringAudioTrackResolver;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.*;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import fr.farmvivi.fluxcord.api.config.Configuration;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.source.SearchSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages audio players and sources for the music plugin.
 * Configures and registers all available audio sources.
 */
public class AudioPlayerManager {
    private static final Logger logger = LoggerFactory.getLogger(AudioPlayerManager.class);
    
    private final MusicPlugin plugin;
    private final com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager playerManager;
    
    public AudioPlayerManager(MusicPlugin plugin) {
        this.plugin = plugin;
        this.playerManager = new DefaultAudioPlayerManager();
        initializeSources();
    }
    
    private void initializeSources() {
        logger.info("Initializing audio sources...");
        
        Configuration config = plugin.getPluginConfig();
        
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
        playerManager.registerSourceManager(youtubeManager);
        
        // Spotify source
        String spotifyClientId = config.getString("providers.spotify.client_id", null);
        String spotifyClientSecret = config.getString("providers.spotify.client_secret", null);
        if (spotifyClientId != null && spotifyClientSecret != null) {
            logger.info("Enabling Spotify source provider");
            playerManager.registerSourceManager(new SpotifySourceManager(
                    spotifyClientId, 
                    spotifyClientSecret, 
                    config.getString("providers.spotify.country_code", "US"),
                    playerManager,
                    new DefaultMirroringAudioTrackResolver(null)
            ));
        }
        
        // Deezer source
        String deezerKey = config.getString("providers.deezer.master_decryption_key", null);
        if (deezerKey != null) {
            logger.info("Enabling Deezer source provider");
            playerManager.registerSourceManager(new DeezerAudioSourceManager(deezerKey));
        }
        
        // Apple Music source
        String appleMusicToken = config.getString("providers.apple_music.token", null);
        if (appleMusicToken != null) {
            logger.info("Enabling Apple Music source provider");
            playerManager.registerSourceManager(new AppleMusicSourceManager(
                    appleMusicToken,
                    config.getString("providers.apple_music.country_code", "US"),
                    playerManager,
                    new DefaultMirroringAudioTrackResolver(null)
            ));
        }
        
        // Flowery TTS source
        String floweryVoice = config.getString("providers.flowery_tts.voice", null);
        if (floweryVoice != null) {
            logger.info("Enabling Flowery TTS source provider");
            playerManager.registerSourceManager(new FloweryTTSSourceManager(floweryVoice));
        }
        
        // SoundCloud source
        if (config.getBoolean("providers.soundcloud.enabled", true)) {
            logger.info("Enabling SoundCloud source provider");
            SoundCloudDataReader dataReader = new DefaultSoundCloudDataReader();
            SoundCloudDataLoader dataLoader = new DefaultSoundCloudDataLoader();
            SoundCloudFormatHandler formatHandler = new DefaultSoundCloudFormatHandler();
            SoundCloudPlaylistLoader playlistLoader = new DefaultSoundCloudPlaylistLoader(
                    dataLoader, dataReader, formatHandler
            );
            playerManager.registerSourceManager(new SoundCloudAudioSourceManager(
                    true, dataReader, dataLoader, formatHandler, playlistLoader
            ));
        }
        
        // Other sources
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new GetyarnAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));
        playerManager.registerSourceManager(new LocalAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));
        
        // Search source manager
        playerManager.registerSourceManager(new SearchSourceManager(youtubeManager, "ytsearch:"));
        
        // Enable filter hot swap for effects
        playerManager.getConfiguration().setFilterHotSwapEnabled(true);
        
        logger.info("Audio sources initialized successfully");
    }
    
    public com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager getPlayerManager() {
        return playerManager;
    }
    
    public void shutdown() {
        logger.info("Shutting down audio player manager...");
        playerManager.shutdown();
    }
}
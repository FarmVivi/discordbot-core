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

        Configuration config = plugin.getConfiguration();

        // YouTube source with multiple clients for reliability
        boolean youtubeEnabled = config.getBoolean("providers.youtube.enabled", true);
        YoutubeAudioSourceManager youtubeSourceManager = null;
        if (youtubeEnabled) {
            logger.info("Enabling YouTube source provider");
            youtubeSourceManager = new YoutubeAudioSourceManager(true,
                    new MusicWithThumbnail(),
                    new AndroidVrWithThumbnail(),
                    new WebWithThumbnail(),
                    new WebEmbeddedWithThumbnail(),
                    new MWebWithThumbnail(),
                    new IosWithThumbnail(),
                    new AndroidWithThumbnail(),
                    new AndroidMusicWithThumbnail(),
                    new TvHtml5SimplyWithThumbnail()
            );
            playerManager.registerSourceManager(youtubeSourceManager);
        }

        // Spotify source
        boolean spotifyEnabled = config.getBoolean("providers.spotify.enabled", false);
        String spotifyClientId = config.getString("providers.spotify.client_id", null);
        String spotifyClientSecret = config.getString("providers.spotify.client_secret", null);
        String spotifyCountryCode = config.getString("providers.spotify.country_code", "US");
        if (spotifyEnabled && spotifyClientId != null && spotifyClientSecret != null) {
            logger.info("Enabling Spotify source provider");
            SpotifySourceManager spotifySourceManager = new SpotifySourceManager(
                    spotifyClientId,
                    spotifyClientSecret,
                    spotifyCountryCode,
                    playerManager,
                    new DefaultMirroringAudioTrackResolver(null)
            );
            playerManager.registerSourceManager(spotifySourceManager);
        }

        // Deezer source
        boolean deezerEnabled = config.getBoolean("providers.deezer.enabled", false);
        String deezerKey = config.getString("providers.deezer.master_decryption_key", null);
        String deezerARL = config.getString("providers.deezer.arl_cookie", null);
        if (deezerEnabled && deezerKey != null && deezerARL != null) {
            logger.info("Enabling Deezer source provider");
            DeezerAudioSourceManager deezerSourceManager = new DeezerAudioSourceManager(deezerKey, deezerARL);
            playerManager.registerSourceManager(deezerSourceManager);
        }

        // Apple Music source
        boolean appleMusicEnabled = config.getBoolean("providers.apple_music.enabled", false);
        String appleMusicToken = config.getString("providers.apple_music.token", null);
        String appleMusicCountryCode = config.getString("providers.apple_music.country_code", "US");
        if (appleMusicEnabled && appleMusicToken != null) {
            logger.info("Enabling Apple Music source provider");
            AppleMusicSourceManager appleMusicSourceManager = new AppleMusicSourceManager(
                    appleMusicToken,
                    appleMusicCountryCode,
                    playerManager,
                    new DefaultMirroringAudioTrackResolver(null)
            );
            playerManager.registerSourceManager(appleMusicSourceManager);
        }

        // Flowery TTS source
        boolean floweryEnabled = config.getBoolean("providers.flowery_tts.enabled", false);
        String floweryVoice = config.getString("providers.flowery_tts.voice", null);
        if (floweryEnabled && floweryVoice != null) {
            logger.info("Enabling Flowery TTS source provider");
            FloweryTTSSourceManager floweryTTSSourceManager = new FloweryTTSSourceManager(floweryVoice);
            playerManager.registerSourceManager(floweryTTSSourceManager);
        }

        // SoundCloud source
        boolean soundcloudEnabled = config.getBoolean("providers.soundcloud.enabled", false);
        if (soundcloudEnabled) {
            logger.info("Enabling SoundCloud source provider");
            SoundCloudDataReader dataReader = new DefaultSoundCloudDataReader();
            SoundCloudDataLoader dataLoader = new DefaultSoundCloudDataLoader();
            SoundCloudFormatHandler formatHandler = new DefaultSoundCloudFormatHandler();
            SoundCloudPlaylistLoader playlistLoader = new DefaultSoundCloudPlaylistLoader(
                    dataLoader, dataReader, formatHandler
            );
            SoundCloudAudioSourceManager soundCloudSourceManager = new SoundCloudAudioSourceManager(
                    true, dataReader, dataLoader, formatHandler, playlistLoader
            );
            playerManager.registerSourceManager(soundCloudSourceManager);
        }

        // Bandcamp source
        boolean bandcampEnabled = config.getBoolean("providers.bandcamp.enabled", false);
        if (bandcampEnabled) {
            logger.info("Enabling Bandcamp source provider");
            BandcampAudioSourceManager bandcampSourceManager = new BandcampAudioSourceManager();
            playerManager.registerSourceManager(bandcampSourceManager);
        }

        // Vimeo source
        boolean vimeoEnabled = config.getBoolean("providers.vimeo.enabled", false);
        if (vimeoEnabled) {
            logger.info("Enabling Vimeo source provider");
            VimeoAudioSourceManager vimeoSourceManager = new VimeoAudioSourceManager();
            playerManager.registerSourceManager(vimeoSourceManager);
        }

        // Twitch source
        boolean twitchEnabled = config.getBoolean("providers.twitch.enabled", false);
        if (twitchEnabled) {
            logger.info("Enabling Twitch source provider");
            TwitchStreamAudioSourceManager twitchSourceManager = new TwitchStreamAudioSourceManager();
            playerManager.registerSourceManager(twitchSourceManager);
        }

        // Getyarn source
        boolean getyarnEnabled = config.getBoolean("providers.getyarn.enabled", false);
        if (getyarnEnabled) {
            logger.info("Enabling Getyarn source provider");
            GetyarnAudioSourceManager getyarnSourceManager = new GetyarnAudioSourceManager();
            playerManager.registerSourceManager(getyarnSourceManager);
        }

        // HTTP source
        boolean httpEnabled = config.getBoolean("providers.http.enabled", false);
        if (httpEnabled) {
            logger.info("Enabling HTTP source provider");
            HttpAudioSourceManager httpSourceManager = new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY);
            playerManager.registerSourceManager(httpSourceManager);
        }

        // Local source
        boolean localEnabled = config.getBoolean("providers.local.enabled", false);
        if (localEnabled) {
            logger.info("Enabling Local source provider");
            LocalAudioSourceManager localSourceManager = new LocalAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY);
            playerManager.registerSourceManager(localSourceManager);
        }

        // Search source manager
        if (youtubeSourceManager != null) {
            logger.info("Enabling Search source provider");
            SearchSourceManager searchSourceManager = new SearchSourceManager(youtubeSourceManager, "ytsearch:");
            playerManager.registerSourceManager(searchSourceManager);
        } else {
            logger.warn("YouTube source manager is not initialized; Search source provider will have limited functionality.");
        }

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
package fr.farmvivi.fluxcord.plugins.music.service;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.model.GuildMusicPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Service for managing audio players and LavaPlayer integration.
 * Handles audio source management, track loading, and per-guild player instances.
 */
public class AudioPlayerService {
    private static final Logger logger = LoggerFactory.getLogger(AudioPlayerService.class);

    private final MusicPlugin plugin;
    private final AudioPlayerManager audioPlayerManager;
    private final ConcurrentHashMap<Long, GuildMusicPlayer> guildPlayers = new ConcurrentHashMap<>();
    
    public AudioPlayerService(MusicPlugin plugin) {
        this.plugin = plugin;
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        
        // Initialize audio sources
        initializeAudioSources();
    }
    
    private void initializeAudioSources() {
        // Register default sources (YouTube, SoundCloud, etc.)
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        
        logger.info("Audio sources initialized successfully");
    }
    
    /**
     * Gets or creates a music player for the specified guild.
     */
    public GuildMusicPlayer getGuildPlayer(Guild guild) {
        return guildPlayers.computeIfAbsent(guild.getIdLong(), 
            id -> new GuildMusicPlayer(plugin, audioPlayerManager.createPlayer(), guild));
    }
    
    /**
     * Removes and cleanup a guild player.
     */
    public void removeGuildPlayer(Guild guild) {
        GuildMusicPlayer player = guildPlayers.remove(guild.getIdLong());
        if (player != null) {
            player.cleanup();
        }
    }
    
    /**
     * Loads and plays a track from URL or search query.
     */
    public Future<Void> loadAndPlay(Guild guild, String query, TextChannel channel, boolean insertNext) {
        return audioPlayerManager.loadItemOrdered(guild, query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                GuildMusicPlayer musicPlayer = getGuildPlayer(guild);
                musicPlayer.queueTrack(track, insertNext);
                
                String message = String.format("🎵 Added to queue: **%s** by %s", 
                    track.getInfo().title, track.getInfo().author);
                channel.sendMessage(message).queue();
            }
            
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                GuildMusicPlayer musicPlayer = getGuildPlayer(guild);
                int tracksAdded = 0;
                
                for (AudioTrack track : playlist.getTracks()) {
                    musicPlayer.queueTrack(track.makeClone(), false);
                    tracksAdded++;
                    
                    // Limit playlist size to prevent spam
                    if (tracksAdded >= plugin.getConfiguration().getInt("music.max_playlist_import", 50)) {
                        break;
                    }
                }
                
                String message = String.format("📋 Loaded playlist: **%s** (%d tracks added)", 
                    playlist.getName(), tracksAdded);
                channel.sendMessage(message).queue();
            }
            
            @Override
            public void noMatches() {
                channel.sendMessage("❌ No matches found for: `" + query + "`").queue();
            }
            
            @Override
            public void loadFailed(FriendlyException exception) {
                String message = "❌ Failed to load track: " + exception.getMessage();
                channel.sendMessage(message).queue();
                logger.warn("Failed to load track '{}' for guild {}: {}", 
                    query, guild.getId(), exception.getMessage());
            }
        });
    }
    
    /**
     * Shutdown all players and cleanup resources.
     */
    public void shutdown() {
        logger.info("Shutting down audio player service...");
        
        guildPlayers.values().forEach(GuildMusicPlayer::cleanup);
        guildPlayers.clear();
        
        audioPlayerManager.shutdown();
        logger.info("Audio player service shutdown complete");
    }
    
    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }
}
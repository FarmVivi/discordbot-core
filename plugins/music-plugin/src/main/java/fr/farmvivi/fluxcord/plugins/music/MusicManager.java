package fr.farmvivi.fluxcord.plugins.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.fluxcord.plugins.music.player.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Manages music playback, queue operations, and audio processing.
 */
public class MusicManager {

    private final MusicPlugin plugin;

    public MusicManager(MusicPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Load and queue a track for playback.
     *
     * @param guild the guild where to play the track
     * @param query the search query or URL
     * @param messageChannel the channel to send feedback to
     * @param playNow whether to play immediately (skip current track)
     * @return a future that completes when the track is loaded
     */
    public CompletableFuture<String> loadTrack(Guild guild, String query, MessageChannelUnion messageChannel, boolean playNow) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        GuildMusicManager guildManager = plugin.getGuildMusicManager(guild);
        
        // Ensure the bot is connected to audio
        guildManager.connect();
        
        // Add "ytsearch:" prefix for non-URL queries
        String searchQuery = query;
        if (!query.startsWith("http://") && !query.startsWith("https://") && !query.startsWith("ytsearch:")) {
            searchQuery = "ytsearch:" + query;
        }
        
        plugin.getAudioManager().getAudioPlayerManager().loadItemOrdered(guildManager, searchQuery, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                plugin.getLogger().info("[{}] Track loaded: \"{}\" ({})", 
                    guild.getName(), track.getInfo().title, track.getInfo().uri);
                
                if (playNow) {
                    guildManager.playTrackNow(track);
                } else {
                    guildManager.queueTrack(track);
                }
                
                String message = String.format("🎵 Added to queue: **%s** by %s", 
                    track.getInfo().title, track.getInfo().author);
                future.complete(message);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult()) {
                    // For search results, take the first track
                    AudioTrack track = playlist.getTracks().get(0);
                    
                    plugin.getLogger().info("[{}] Track loaded (search): \"{}\" ({})", 
                        guild.getName(), track.getInfo().title, track.getInfo().uri);
                    
                    if (playNow) {
                        guildManager.playTrackNow(track);
                    } else {
                        guildManager.queueTrack(track);
                    }
                    
                    String message = String.format("🎵 Added to queue: **%s** by %s", 
                        track.getInfo().title, track.getInfo().author);
                    future.complete(message);
                } else {
                    // Load entire playlist
                    plugin.getLogger().info("[{}] Playlist loaded: \"{}\" ({} tracks)", 
                        guild.getName(), playlist.getName(), playlist.getTracks().size());
                    
                    for (AudioTrack track : playlist.getTracks()) {
                        if (playNow && track == playlist.getTracks().get(0)) {
                            guildManager.playTrackNow(track);
                        } else {
                            guildManager.queueTrack(track);
                        }
                    }
                    
                    String message = String.format("🎵 Added playlist: **%s** (%d tracks)", 
                        playlist.getName(), playlist.getTracks().size());
                    future.complete(message);
                }
            }

            @Override
            public void noMatches() {
                plugin.getLogger().warn("[{}] Track not found: {}", guild.getName(), query);
                future.complete("❌ No tracks found for: " + query);
            }

            @Override
            public void loadFailed(FriendlyException throwable) {
                plugin.getLogger().error("[{}] Track load failed: {} (Reason: {})", 
                    guild.getName(), query, throwable.getMessage());
                future.complete("❌ Failed to load track: " + throwable.getMessage());
            }
        });
        
        return future;
    }

    public void handleVoiceUpdate(GuildVoiceUpdateEvent event) {
        // TODO: Implement auto-leave logic when bot is alone
    }

    public void shutdown() {
        // Clean up all guild managers
        plugin.getLogger().info("Shutting down music manager...");
    }
}
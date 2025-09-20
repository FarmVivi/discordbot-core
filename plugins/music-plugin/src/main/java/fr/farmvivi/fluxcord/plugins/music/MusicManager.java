package fr.farmvivi.fluxcord.plugins.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.plugins.music.audio.AudioPlayerManager;
import fr.farmvivi.fluxcord.plugins.music.player.MusicPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages music players for different guilds.
 */
public class MusicManager {
    private static final Logger logger = LoggerFactory.getLogger(MusicManager.class);
    
    private final MusicPlugin plugin;
    private final AudioPlayerManager audioPlayerManager;
    private final Map<Long, MusicPlayer> players;
    
    public MusicManager(MusicPlugin plugin) {
        this.plugin = plugin;
        this.audioPlayerManager = new AudioPlayerManager(plugin);
        this.players = new ConcurrentHashMap<>();
    }
    
    /**
     * Gets or creates a music player for a guild.
     */
    public synchronized MusicPlayer getPlayer(Guild guild) {
        return players.computeIfAbsent(guild.getIdLong(), id -> {
            AudioPlayer audioPlayer = audioPlayerManager.getPlayerManager().createPlayer();
            return new MusicPlayer(plugin, guild, audioPlayer);
        });
    }
    
    /**
     * Destroys a music player for a guild.
     */
    public synchronized void destroyPlayer(Guild guild) {
        MusicPlayer player = players.remove(guild.getIdLong());
        if (player != null) {
            player.destroy();
        }
    }
    
    /**
     * Loads and plays a track.
     */
    public void loadTrack(CommandContext ctx, String query, boolean playNow) {
        Guild guild = ctx.getGuild();
        MusicPlayer player = getPlayer(guild);
        MessageChannel channel = ctx.getChannel();
        
        // Set the message channel for the player
        player.setMessageChannel(channel);
        
        // Connect to voice channel if not connected
        AudioManager audioManager = guild.getAudioManager();
        if (!audioManager.isConnected()) {
            AudioChannel voiceChannel = ctx.getMember().getVoiceState().getChannel();
            if (voiceChannel == null) {
                ctx.replyError(plugin.getPluginLanguageAdapter().getString(
                    guild, "music.error.not_in_voice"
                ));
                return;
            }
            audioManager.openAudioConnection(voiceChannel);
        }
        
        // Defer reply for long loading
        ctx.deferReply();
        
        // Load the track
        audioPlayerManager.getPlayerManager().loadItemOrdered(player, query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                logger.info("[{}] Track loaded: {} ({})", 
                    guild.getName(), track.getInfo().title, track.getInfo().uri);
                
                EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle(plugin.getPluginLanguageAdapter().getString(guild, "music.track_added"))
                    .addField(
                        plugin.getPluginLanguageAdapter().getString(guild, "music.title"),
                        String.format("[%s](%s)", track.getInfo().title, track.getInfo().uri),
                        false
                    );
                
                if (track.getInfo().artworkUrl != null) {
                    embed.setThumbnail(track.getInfo().artworkUrl);
                }
                
                ctx.replyEmbed(embed);
                
                if (playNow) {
                    player.playTrackNow(track);
                } else {
                    player.playTrack(track);
                }
            }
            
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                
                if (playlist.isSearchResult() && !tracks.isEmpty()) {
                    // For search results, play the first track
                    AudioTrack track = tracks.get(0);
                    trackLoaded(track);
                } else {
                    // Queue the entire playlist
                    logger.info("[{}] Playlist loaded: {} ({} tracks)", 
                        guild.getName(), playlist.getName(), tracks.size());
                    
                    EmbedBuilder embed = new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle(plugin.getPluginLanguageAdapter().getString(guild, "music.playlist_added"))
                        .addField(
                            plugin.getPluginLanguageAdapter().getString(guild, "music.name"),
                            playlist.getName(),
                            false
                        )
                        .addField(
                            plugin.getPluginLanguageAdapter().getString(guild, "music.tracks"),
                            String.valueOf(tracks.size()),
                            false
                        );
                    
                    ctx.replyEmbed(embed);
                    
                    for (AudioTrack track : tracks) {
                        if (playNow && tracks.indexOf(track) == 0) {
                            player.playTrackNow(track);
                        } else {
                            player.playTrack(track);
                        }
                    }
                }
            }
            
            @Override
            public void noMatches() {
                logger.warn("[{}] No matches found for: {}", guild.getName(), query);
                ctx.replyError(plugin.getPluginLanguageAdapter().getString(
                    guild, "music.error.no_matches"
                ));
            }
            
            @Override
            public void loadFailed(FriendlyException exception) {
                logger.error("[{}] Failed to load track: {}", guild.getName(), query, exception);
                ctx.replyError(plugin.getPluginLanguageAdapter().getString(
                    guild, "music.error.load_failed", exception.getMessage()
                ));
            }
        });
    }
    
    /**
     * Handles voice channel updates.
     */
    public void handleVoiceUpdate(GuildVoiceUpdateEvent event) {
        // Check if bot was disconnected
        if (event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
            if (event.getChannelLeft() != null && event.getChannelJoined() == null) {
                MusicPlayer player = players.get(event.getGuild().getIdLong());
                if (player != null) {
                    player.handleDisconnect();
                }
            }
        }
    }
    
    /**
     * Shuts down all players and the audio player manager.
     */
    public void shutdown() {
        logger.info("Shutting down music manager...");
        
        // Destroy all players
        for (MusicPlayer player : players.values()) {
            player.destroy();
        }
        players.clear();
        
        // Shut down audio player manager
        audioPlayerManager.shutdown();
    }
}
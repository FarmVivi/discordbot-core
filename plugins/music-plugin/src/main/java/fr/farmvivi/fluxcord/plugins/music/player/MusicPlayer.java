package fr.farmvivi.fluxcord.plugins.music.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.audio.AudioPlayerSendHandler;
import fr.farmvivi.fluxcord.plugins.music.ui.MusicPlayerMessage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents a music player instance for a specific guild.
 * Manages audio playback, queue, and player state.
 */
public class MusicPlayer {
    private static final Logger logger = LoggerFactory.getLogger(MusicPlayer.class);
    public static final int DEFAULT_VOLUME = 50;
    public static final int QUIT_TIMEOUT_SECONDS = 300; // 5 minutes
    
    private final MusicPlugin plugin;
    private final Guild guild;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private final AudioPlayerSendHandler sendHandler;
    private final MusicPlayerMessage playerMessage;
    
    private ScheduledFuture<?> quitTask;
    private int volume = DEFAULT_VOLUME;
    
    public MusicPlayer(MusicPlugin plugin, Guild guild, AudioPlayer audioPlayer) {
        this.plugin = plugin;
        this.guild = guild;
        this.audioPlayer = audioPlayer;
        this.trackScheduler = new TrackScheduler(this, audioPlayer);
        this.sendHandler = new AudioPlayerSendHandler(audioPlayer);
        this.playerMessage = new MusicPlayerMessage(this);
        
        audioPlayer.addListener(trackScheduler);
        audioPlayer.setVolume(volume);
    }
    
    /**
     * Plays a track, adding it to the queue if something is already playing.
     */
    public void playTrack(AudioTrack track) {
        cancelQuitTask();
        boolean playing = trackScheduler.queue(track);
        
        if (playing) {
            // Register our send handler with the AudioService
            plugin.getAudioService().registerSendHandler(
                guild, 
                plugin, 
                sendHandler, 
                volume, 
                50 // Normal priority
            );
        }
        
        playerMessage.refresh();
    }
    
    /**
     * Plays a track immediately, skipping the current track if any.
     */
    public void playTrackNow(AudioTrack track) {
        cancelQuitTask();
        trackScheduler.playNow(track);
        
        // Register our send handler with the AudioService
        plugin.getAudioService().registerSendHandler(
            guild, 
            plugin, 
            sendHandler, 
            volume, 
            50 // Normal priority
        );
        
        playerMessage.refresh();
    }
    
    /**
     * Skips the current track.
     */
    public void skipTrack() {
        trackScheduler.nextTrack();
        playerMessage.refresh();
        
        if (audioPlayer.getPlayingTrack() == null && trackScheduler.getQueueSize() == 0) {
            scheduleQuit();
        }
    }
    
    /**
     * Stops playback and clears the queue.
     */
    public void stop() {
        trackScheduler.clear();
        audioPlayer.stopTrack();
        playerMessage.refresh();
        scheduleQuit();
    }
    
    /**
     * Pauses or resumes playback.
     */
    public void setPaused(boolean paused) {
        audioPlayer.setPaused(paused);
        playerMessage.refresh();
    }
    
    /**
     * Sets the playback volume.
     */
    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
        audioPlayer.setVolume(this.volume);
        
        // Update volume in AudioService
        plugin.getAudioService().setVolume(guild, plugin, this.volume);
        
        playerMessage.refresh();
    }
    
    /**
     * Sets the message channel for player messages.
     */
    public void setMessageChannel(MessageChannel channel) {
        playerMessage.setMessageChannel(channel);
    }
    
    /**
     * Handles voice channel disconnection.
     */
    public void handleDisconnect() {
        logger.info("[{}] Handling disconnect, stopping playback", guild.getName());
        stop();
        plugin.getAudioService().deregisterSendHandler(guild, plugin);
        playerMessage.delete();
    }
    
    /**
     * Schedules automatic quit after inactivity.
     */
    private void scheduleQuit() {
        cancelQuitTask();
        
        ScheduledExecutorService scheduler = plugin.getScheduler();
        quitTask = scheduler.schedule(() -> {
            if (audioPlayer.getPlayingTrack() == null && trackScheduler.getQueueSize() == 0) {
                logger.info("[{}] Auto-leaving voice channel after {} seconds of inactivity", 
                    guild.getName(), QUIT_TIMEOUT_SECONDS);
                
                guild.getAudioManager().closeAudioConnection();
                plugin.getAudioService().deregisterSendHandler(guild, plugin);
                playerMessage.delete();
            }
        }, QUIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
    
    /**
     * Cancels the scheduled quit task.
     */
    private void cancelQuitTask() {
        if (quitTask != null && !quitTask.isDone()) {
            quitTask.cancel(false);
            quitTask = null;
        }
    }
    
    /**
     * Cleans up resources when the player is destroyed.
     */
    public void destroy() {
        cancelQuitTask();
        trackScheduler.clear();
        audioPlayer.destroy();
        playerMessage.delete();
    }
    
    // Getters
    public Guild getGuild() {
        return guild;
    }
    
    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }
    
    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }
    
    public MusicPlayerMessage getPlayerMessage() {
        return playerMessage;
    }
    
    public MusicPlugin getPlugin() {
        return plugin;
    }
    
    public int getVolume() {
        return volume;
    }
    
    public boolean isPaused() {
        return audioPlayer.isPaused();
    }
    
    public AudioTrack getPlayingTrack() {
        return audioPlayer.getPlayingTrack();
    }
}
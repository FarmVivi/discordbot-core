package fr.farmvivi.fluxcord.plugins.music.model;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a music player instance for a specific guild.
 * Manages the audio queue, playback state, and provides controls.
 */
public class GuildMusicPlayer extends AudioEventAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GuildMusicPlayer.class);

    private final MusicPlugin plugin;
    private final AudioPlayer audioPlayer;
    private final Guild guild;
    private final BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();
    
    // Playback state
    private AudioTrack currentTrack;
    private TextChannel textChannel;
    private boolean loopTrack = false;
    private boolean loopQueue = false;
    private boolean shuffle = false;
    private int volume = 50;
    
    // Auto-leave functionality
    private long lastActivity = System.currentTimeMillis();
    
    public GuildMusicPlayer(MusicPlugin plugin, AudioPlayer audioPlayer, Guild guild) {
        this.plugin = plugin;
        this.audioPlayer = audioPlayer;
        this.guild = guild;
        
        // Set initial volume from config
        this.volume = plugin.getConfiguration().getInt("music.default_volume", 50);
        audioPlayer.setVolume(volume);
        
        // Register event listener
        audioPlayer.addListener(this);
        
        logger.debug("Created music player for guild: {}", guild.getName());
    }
    
    /**
     * Adds a track to the queue and starts playback if nothing is playing.
     */
    public void queueTrack(AudioTrack track, boolean insertNext) {
        updateActivity();
        
        if (audioPlayer.getPlayingTrack() == null) {
            // Start playing immediately
            audioPlayer.startTrack(track, false);
            currentTrack = track;
            logger.debug("Started playing track: {} in guild: {}", track.getInfo().title, guild.getName());
        } else {
            // Add to queue
            if (insertNext) {
                // Insert at the beginning of queue
                List<AudioTrack> tempList = new ArrayList<>(queue);
                queue.clear();
                queue.offer(track);
                queue.addAll(tempList);
            } else {
                queue.offer(track);
            }
            logger.debug("Queued track: {} in guild: {} (queue size: {})", 
                track.getInfo().title, guild.getName(), queue.size());
        }
    }
    
    /**
     * Skips the current track and plays the next one.
     */
    public boolean skipTrack() {
        updateActivity();
        
        if (audioPlayer.getPlayingTrack() != null) {
            audioPlayer.stopTrack();
            return true;
        }
        return false;
    }
    
    /**
     * Pauses or resumes playback.
     */
    public void pauseResume() {
        updateActivity();
        audioPlayer.setPaused(!audioPlayer.isPaused());
    }
    
    /**
     * Sets the playback volume (0-100).
     */
    public void setVolume(int volume) {
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("Volume must be between 0 and 100");
        }
        
        this.volume = volume;
        audioPlayer.setVolume(volume);
        updateActivity();
        logger.debug("Set volume to {} in guild: {}", volume, guild.getName());
    }
    
    /**
     * Clears the entire queue.
     */
    public void clearQueue() {
        queue.clear();
        updateActivity();
        logger.debug("Cleared queue in guild: {}", guild.getName());
    }
    
    /**
     * Shuffles the current queue.
     */
    public void shuffleQueue() {
        List<AudioTrack> tracks = new ArrayList<>(queue);
        Collections.shuffle(tracks, ThreadLocalRandom.current());
        queue.clear();
        queue.addAll(tracks);
        updateActivity();
        logger.debug("Shuffled queue in guild: {} (size: {})", guild.getName(), tracks.size());
    }
    
    /**
     * Stops playback and clears the queue.
     */
    public void stop() {
        audioPlayer.stopTrack();
        queue.clear();
        currentTrack = null;
        updateActivity();
        logger.debug("Stopped playback in guild: {}", guild.getName());
    }
    
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        logger.debug("Track ended: {} in guild: {} (reason: {})", 
            track.getInfo().title, guild.getName(), endReason);
        
        // Handle different end reasons
        if (endReason.mayStartNext) {
            AudioTrack nextTrack = null;
            
            if (loopTrack && !endReason.equals(AudioTrackEndReason.REPLACED)) {
                // Loop current track
                nextTrack = track.makeClone();
                logger.debug("Looping track: {} in guild: {}", track.getInfo().title, guild.getName());
            } else if (loopQueue && queue.isEmpty() && currentTrack != null) {
                // Loop the track that just ended if queue is empty
                nextTrack = track.makeClone();
                logger.debug("Looping queue with single track: {} in guild: {}", track.getInfo().title, guild.getName());
            } else {
                // Get next track from queue
                nextTrack = queue.poll();
                
                if (nextTrack == null && loopQueue && currentTrack != null) {
                    // If queue is empty and loop queue is enabled, restart with current track
                    nextTrack = currentTrack.makeClone();
                    logger.debug("Looping queue, restarting: {} in guild: {}", currentTrack.getInfo().title, guild.getName());
                }
            }
            
            if (nextTrack != null) {
                currentTrack = nextTrack;
                player.startTrack(nextTrack, false);
            } else {
                currentTrack = null;
                logger.debug("No more tracks to play in guild: {}", guild.getName());
                // Could trigger auto-leave here if enabled
            }
        }
        
        updateActivity();
    }
    
    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        logger.debug("Track started: {} in guild: {}", track.getInfo().title, guild.getName());
        updateActivity();
        
        // Send now playing message if text channel is set
        if (textChannel != null) {
            String message = String.format("🎵 Now playing: **%s** by %s", 
                track.getInfo().title, track.getInfo().author);
            textChannel.sendMessage(message).queue();
        }
    }
    
    /**
     * Updates the last activity timestamp for auto-leave functionality.
     */
    private void updateActivity() {
        lastActivity = System.currentTimeMillis();
    }
    
    /**
     * Cleanup resources and stop playback.
     */
    public void cleanup() {
        logger.debug("Cleaning up music player for guild: {}", guild.getName());
        audioPlayer.destroy();
        queue.clear();
        currentTrack = null;
    }
    
    // Getters and setters
    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }
    
    public Guild getGuild() {
        return guild;
    }
    
    public AudioTrack getCurrentTrack() {
        return currentTrack;
    }
    
    public List<AudioTrack> getQueue() {
        return new ArrayList<>(queue);
    }
    
    public int getQueueSize() {
        return queue.size();
    }
    
    public boolean isLoopTrack() {
        return loopTrack;
    }
    
    public void setLoopTrack(boolean loopTrack) {
        this.loopTrack = loopTrack;
        updateActivity();
    }
    
    public boolean isLoopQueue() {
        return loopQueue;
    }
    
    public void setLoopQueue(boolean loopQueue) {
        this.loopQueue = loopQueue;
        updateActivity();
    }
    
    public boolean isShuffle() {
        return shuffle;
    }
    
    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
        if (shuffle) {
            shuffleQueue();
        }
        updateActivity();
    }
    
    public int getVolume() {
        return volume;
    }
    
    public TextChannel getTextChannel() {
        return textChannel;
    }
    
    public void setTextChannel(TextChannel textChannel) {
        this.textChannel = textChannel;
    }
    
    public long getLastActivity() {
        return lastActivity;
    }
    
    public boolean isPlaying() {
        return audioPlayer.getPlayingTrack() != null && !audioPlayer.isPaused();
    }
    
    public boolean isPaused() {
        return audioPlayer.isPaused();
    }
}
package fr.farmvivi.fluxcord.plugins.music.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;

/**
 * Track scheduler that handles audio track lifecycle events and queue management.
 */
public class TrackScheduler extends AudioEventAdapter {
    private final GuildMusicManager guildManager;
    private final AudioPlayer audioPlayer;
    private final BlockingQueue<AudioTrack> queue;
    
    public TrackScheduler(GuildMusicManager guildManager) {
        this.guildManager = guildManager;
        this.audioPlayer = guildManager.getAudioPlayer();
        this.queue = guildManager.getQueue();
    }
    
    /**
     * Add a track to the queue. If nothing is playing, start playing immediately.
     *
     * @param track the track to queue
     */
    public void queue(AudioTrack track) {
        if (!audioPlayer.startTrack(track, true)) {
            queue.offer(track);
        }
    }
    
    /**
     * Play a track immediately, stopping the current track if playing.
     *
     * @param track the track to play now
     */
    public void playNow(AudioTrack track) {
        audioPlayer.startTrack(track, false);
    }
    
    /**
     * Start the next track, either from queue or looping current track.
     *
     * @return true if a track was started
     */
    public boolean nextTrack() {
        AudioTrack currentTrack = audioPlayer.getPlayingTrack();
        
        // Handle loop modes
        if (currentTrack != null && guildManager.getLoopMode() == GuildMusicManager.LoopMode.TRACK) {
            // Loop current track
            audioPlayer.startTrack(currentTrack.makeClone(), false);
            return true;
        }
        
        // Get next track from queue
        AudioTrack nextTrack = queue.poll();
        
        // Handle queue loop mode
        if (nextTrack == null && guildManager.getLoopMode() == GuildMusicManager.LoopMode.QUEUE 
            && currentTrack != null) {
            // Re-queue current track and continue with queue
            queue.offer(currentTrack.makeClone());
            nextTrack = queue.poll();
        }
        
        if (nextTrack != null) {
            audioPlayer.startTrack(nextTrack, false);
            return true;
        }
        
        return false;
    }
    
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start next track if this track ended normally or was replaced
        if (endReason.mayStartNext) {
            if (!nextTrack()) {
                // No more tracks, consider disconnecting after timeout
                scheduleAutoLeave();
            }
        }
        
        // Update persistent message when track ends
        updatePersistentMessage();
    }
    
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        guildManager.getPlugin().getLogger().debug("Started playing: {} in guild: {}", 
            track.getInfo().title, guildManager.getGuild().getName());
        
        // Update persistent message when track starts
        updatePersistentMessage();
    }
    
    public void onTrackException(AudioPlayer player, AudioTrack track, Exception exception) {
        guildManager.getPlugin().getLogger().warn("Track exception in guild {}: {}", 
            guildManager.getGuild().getName(), exception.getMessage());
        
        // Try next track on exception
        nextTrack();
        updatePersistentMessage();
    }
    
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        guildManager.getPlugin().getLogger().warn("Track stuck in guild {}: {} ({}ms)", 
            guildManager.getGuild().getName(), track.getInfo().title, thresholdMs);
        
        // Skip stuck track
        nextTrack();
        updatePersistentMessage();
    }
    
    private void updatePersistentMessage() {
        PersistentMusicPlayerMessage playerMessage = 
            guildManager.getPlugin().getPersistentPlayerMessage(guildManager.getGuild());
        if (playerMessage != null) {
            playerMessage.refresh();
        }
    }
    
    private void scheduleAutoLeave() {
        // TODO: Implement auto-leave functionality after timeout
        // This should check if bot is alone in voice channel and schedule disconnect
        guildManager.getPlugin().getLogger().debug("Queue empty in guild: {}, considering auto-leave", 
            guildManager.getGuild().getName());
    }
}
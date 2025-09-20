package fr.farmvivi.fluxcord.plugins.music.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * Per-guild music manager that handles audio playback, queue management,
 * and integration with Fluxcord's audio system.
 */
public class GuildMusicManager {
    private final MusicPlugin plugin;
    private final Guild guild;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private final BlockingQueue<AudioTrack> queue;
    private final FluxcordAudioSendHandler audioSendHandler;
    
    // Playback state
    private boolean shuffleMode = false;
    private LoopMode loopMode = LoopMode.OFF;
    
    public enum LoopMode {
        OFF, TRACK, QUEUE
    }
    
    public GuildMusicManager(MusicPlugin plugin, Guild guild) {
        this.plugin = plugin;
        this.guild = guild;
        this.audioPlayer = plugin.getAudioManager().getAudioPlayerManager().createPlayer();
        this.queue = new LinkedBlockingQueue<>();
        this.trackScheduler = new TrackScheduler(this);
        this.audioSendHandler = new FluxcordAudioSendHandler(audioPlayer);
        
        // Set up player listener
        audioPlayer.addListener(trackScheduler);
        
        plugin.getLogger().debug("Created GuildMusicManager for guild: {} ({})", 
            guild.getName(), guild.getId());
    }
    
    /**
     * Queue a track for playback.
     *
     * @param track the track to queue
     */
    public void queueTrack(AudioTrack track) {
        trackScheduler.queue(track);
    }
    
    /**
     * Play a track immediately (skip current if playing).
     *
     * @param track the track to play
     */
    public void playTrackNow(AudioTrack track) {
        trackScheduler.playNow(track);
    }
    
    /**
     * Skip the current track.
     *
     * @return true if a track was skipped
     */
    public boolean skipTrack() {
        return trackScheduler.nextTrack();
    }
    
    /**
     * Stop playback and clear the queue.
     */
    public void stop() {
        audioPlayer.stopTrack();
        queue.clear();
    }
    
    /**
     * Pause or resume playback.
     *
     * @param paused true to pause, false to resume
     */
    public void setPaused(boolean paused) {
        audioPlayer.setPaused(paused);
        updatePersistentMessage();
    }
    
    /**
     * Set the volume.
     *
     * @param volume volume level (0-100)
     */
    public void setVolume(int volume) {
        audioPlayer.setVolume(Math.max(0, Math.min(100, volume)));
        plugin.getAudioManager().setVolume(guild, volume);
        updatePersistentMessage();
    }
    
    /**
     * Toggle shuffle mode.
     *
     * @return the new shuffle state
     */
    public boolean toggleShuffle() {
        shuffleMode = !shuffleMode;
        if (shuffleMode) {
            // Shuffle the current queue
            List<AudioTrack> tracks = new ArrayList<>(queue);
            queue.clear();
            Collections.shuffle(tracks);
            queue.addAll(tracks);
        }
        updatePersistentMessage();
        return shuffleMode;
    }
    
    /**
     * Set or toggle loop mode.
     *
     * @param mode the loop mode to set, or null to cycle through modes
     * @return the current loop mode
     */
    public LoopMode setLoopMode(LoopMode mode) {
        if (mode == null) {
            // Cycle through modes
            switch (loopMode) {
                case OFF:
                    loopMode = LoopMode.TRACK;
                    break;
                case TRACK:
                    loopMode = LoopMode.QUEUE;
                    break;
                case QUEUE:
                    loopMode = LoopMode.OFF;
                    break;
            }
        } else {
            loopMode = mode;
        }
        updatePersistentMessage();
        return loopMode;
    }
    
    /**
     * Clear the queue.
     */
    public void clearQueue() {
        queue.clear();
        updatePersistentMessage();
    }
    
    /**
     * Get a copy of the current queue.
     *
     * @return list of tracks in queue
     */
    public List<AudioTrack> getQueueCopy() {
        return new ArrayList<>(queue);
    }
    
    /**
     * Connect to a voice channel and register audio handler.
     */
    public void connect() {
        // Register with Fluxcord's audio service
        plugin.getAudioManager().registerAudioHandler(guild, audioSendHandler, 
            plugin.getMusicConfig().getDefaultVolume(), 10);
    }
    
    /**
     * Disconnect from voice channel.
     */
    public void disconnect() {
        plugin.getAudioManager().deregisterAudioHandler(guild);
        stop();
    }
    
    /**
     * Update the persistent music player message.
     */
    private void updatePersistentMessage() {
        PersistentMusicPlayerMessage playerMessage = plugin.getPersistentPlayerMessage(guild);
        if (playerMessage != null) {
            playerMessage.refresh();
        }
    }
    
    // Getters
    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }
    
    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }
    
    public boolean isShuffleMode() {
        return shuffleMode;
    }
    
    public LoopMode getLoopMode() {
        return loopMode;
    }
    
    public Guild getGuild() {
        return guild;
    }
    
    public MusicPlugin getPlugin() {
        return plugin;
    }
    
    public FluxcordAudioSendHandler getAudioSendHandler() {
        return audioSendHandler;
    }
}
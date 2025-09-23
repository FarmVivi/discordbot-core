package fr.farmvivi.fluxcord.plugins.music.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Manages the track queue and playback behavior for a music player.
 */
public class TrackScheduler extends AudioEventAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);

    private final MusicPlayer musicPlayer;
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private final List<AudioTrack> queueList; // For indexed access

    private boolean loopMode = false;
    private boolean loopQueueMode = false;
    private boolean shuffleMode = false;

    public TrackScheduler(MusicPlayer musicPlayer, AudioPlayer player) {
        this.musicPlayer = musicPlayer;
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.queueList = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Adds a track to the queue or plays it immediately if nothing is playing.
     *
     * @return true if the track started playing immediately
     */
    public boolean queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
            queueList.add(track);
            return false;
        }
        return true;
    }

    /**
     * Plays a track immediately, interrupting current playback.
     */
    public void playNow(AudioTrack track) {
        AudioTrack currentTrack = player.getPlayingTrack();
        if (currentTrack != null) {
            // Add current track back to the front of the queue
            queue.offer(currentTrack.makeClone());
            queueList.add(0, currentTrack.makeClone());
        }
        player.startTrack(track, false);
    }

    /**
     * Adds a track clone at the end of the queue (keeping internal structures in sync).
     */
    public void addToQueueEnd(AudioTrack track) {
        if (track == null) {
            return;
        }
        queue.offer(track);
        queueList.add(track);
    }

    /**
     * Starts the next track in the queue.
     */
    public void nextTrack() {
        AudioTrack next = null;

        if (shuffleMode && !queue.isEmpty()) {
            // Pick a random track from the queue
            int randomIndex = new Random().nextInt(queueList.size());
            next = queueList.remove(randomIndex);
            // Rebuild queue without the selected track
            queue.clear();
            queue.addAll(queueList);
        } else {
            next = queue.poll();
            if (next != null) {
                // Remove the first occurrence from queueList
                queueList.remove(0);
            }
        }

        if (next != null) {
            player.startTrack(next, false);
        } else if (loopQueueMode && !queueList.isEmpty()) {
            // Re-add all tracks to queue for loop queue mode
            queue.addAll(queueList);
            nextTrack();
        }
    }

    /**
     * Skip current track. If loop (single track) is active, keep current by re-adding it to the end of the queue.
     */
    public void skip() {
        AudioTrack current = player.getPlayingTrack();
        if (current != null && loopMode) {
            // En mode loop, on ajoute la piste actuelle à la fin de la queue
            // pour qu'elle soit rejouée plus tard, pas immédiatement
            addToQueueEnd(current.makeClone());
        }
        nextTrack();
    }

    /**
     * Clears the queue.
     */
    public void clear() {
        queue.clear();
        queueList.clear();
    }

    /**
     * Gets the queue size.
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Gets the queue as a list for display purposes.
     */
    public List<AudioTrack> getQueue() {
        return new ArrayList<>(queueList);
    }

    /**
     * Removes a track at the specified index.
     */
    public boolean removeTrack(int index) {
        if (index < 0 || index >= queueList.size()) {
            return false;
        }
        queueList.remove(index);
        queue.clear();
        queue.addAll(queueList);
        return true;
    }

    /**
     * Moves a track from one position to another.
     */
    public boolean moveTrack(int from, int to) {
        if (from < 0 || from >= queueList.size() || to < 0 || to >= queueList.size()) {
            return false;
        }

        AudioTrack track = queueList.remove(from);
        queueList.add(to, track);
        queue.clear();
        queue.addAll(queueList);
        return true;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        String guildName = musicPlayer.getGuild().getName();
        logger.debug("[{}] Track ended: {} - Reason: {}", guildName, track.getInfo().title, endReason);

        // Only start the next track if the end reason is suitable for it
        if (endReason.mayStartNext) {
            if (loopMode && endReason != AudioTrackEndReason.STOPPED) {
                // Loop current track
                player.startTrack(track.makeClone(), false);
            } else {
                nextTrack();
            }
        }

        musicPlayer.refreshUi();
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        String guildName = musicPlayer.getGuild().getName();
        logger.error("[{}] Track exception for {}: {}", guildName, track.getInfo().title, exception.getMessage());

        // Skip to next track on exception
        nextTrack();
        musicPlayer.refreshUi();
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        String guildName = musicPlayer.getGuild().getName();
        logger.info("[{}] Started playing: {}", guildName, track.getInfo().title);

        musicPlayer.refreshUi();
    }

    // Getters and setters for playback modes
    public boolean isLoopMode() {
        return loopMode;
    }

    public void setLoopMode(boolean loopMode) {
        this.loopMode = loopMode;
    }

    public boolean isLoopQueueMode() {
        return loopQueueMode;
    }

    public void setLoopQueueMode(boolean loopQueueMode) {
        this.loopQueueMode = loopQueueMode;
    }

    public boolean isShuffleMode() {
        return shuffleMode;
    }

    public void setShuffleMode(boolean shuffleMode) {
        this.shuffleMode = shuffleMode;
    }
}
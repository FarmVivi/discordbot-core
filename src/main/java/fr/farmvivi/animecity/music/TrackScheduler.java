package fr.farmvivi.animecity.music;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.jda.JDAManager;
import net.dv8tion.jda.api.entities.Activity;

public class TrackScheduler extends AudioEventAdapter {
    private final BlockingQueue<AudioTrack> tracks = new LinkedBlockingQueue<>();
    private final MusicPlayer player;

    public TrackScheduler(MusicPlayer player) {
        this.player = player;
    }

    public BlockingQueue<AudioTrack> getTracks() {
        return tracks;
    }

    public int getTrackSize() {
        return tracks.size();
    }

    public void queue(AudioTrack track) {
        if (!player.getAudioPlayer().startTrack(track, true))
            tracks.offer(track);
    }

    public AudioTrack nextTrack() {
        if (tracks.isEmpty()) {
            if (player.getGuild().getAudioManager().getConnectedChannel() != null) {
                player.getGuild().getAudioManager().closeAudioConnection();
                player.getAudioPlayer().stopTrack();
                Bot.getInstance().setDefaultActivity();
            }
            return null;
        }
        AudioTrack track = tracks.poll();
        player.getAudioPlayer().startTrack(track, false);
        Bot.logger.info("Playing: " + track.getInfo().title + " | " + track.getInfo().uri);
        return track;
    }

    public synchronized void addTrackFirst(AudioTrack track) {
        List<AudioTrack> remainingTracks = new ArrayList<>();
        tracks.drainTo(remainingTracks);
        tracks.offer(track);
        for (AudioTrack tmpTrack : remainingTracks) {
            tracks.offer(tmpTrack);
        }
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        // Player was paused
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        // Player was resumed
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        // A track started playing
        JDAManager.getShardManager().setActivity(Activity.listening(track.getInfo().title));
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            // Start next track
            if (this.player.isLoopMode()) {
                player.playTrack(track.makeClone());
                return;
            }
            if (this.player.isLoopQueueMode()) {
                this.player.playTrack(track.makeClone());
            }
            nextTrack();
        }

        // endReason == FINISHED: A track finished or died by an exception (mayStartNext
        // = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not
        // finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you
        // can put a
        // clone of this back to your queue
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        // An already playing track threw an exception (track end event will still be
        // received separately)
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        // Audio track has been unable to provide us any audio, might want to just start
        // a new track
    }
}

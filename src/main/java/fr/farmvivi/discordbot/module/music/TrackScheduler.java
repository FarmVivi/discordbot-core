package fr.farmvivi.discordbot.module.music;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.jda.JDAManager;
import net.dv8tion.jda.api.entities.Activity;

public class TrackScheduler extends AudioEventAdapter {
    private final BlockingQueue<AudioTrack> tracks = new LinkedBlockingQueue<>();
    private final MusicPlayer player;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> quitTask;

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
        this.queue(track, false);
    }

    public void queue(AudioTrack track, boolean playNow) {
        if (quitTask != null && !quitTask.isDone())
            quitTask.cancel(true);
        if (!player.getAudioPlayer().startTrack(track, true))
            if (playNow)
                addTrackFirst(track);
            else
                tracks.offer(track);
    }

    public AudioTrack nextTrack() {
        return this.nextTrack(true);
    }

    public AudioTrack nextTrack(boolean delete) {
        if (!delete && player.getAudioPlayer().getPlayingTrack() != null) {
            AudioTrack currentTrack = player.getAudioPlayer().getPlayingTrack();
            this.queue(currentTrack.makeClone());
        }
        if (tracks.isEmpty()) {
            player.getAudioPlayer().stopTrack();
            Bot.setDefaultActivity();
            if (player.getGuild().getAudioManager().getConnectedChannel() != null)
                quitTask = scheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (player.getGuild().getAudioManager().getConnectedChannel() != null)
                            player.getGuild().getAudioManager().closeAudioConnection();
                    }
                }, MusicModule.QUIT_TIMEOUT, TimeUnit.SECONDS);
            return null;
        }
        AudioTrack track;
        if (player.isShuffleMode()) {
            List<AudioTrack> remainingTracks = new ArrayList<>();
            tracks.drainTo(remainingTracks);
            Random random = new Random();
            if (player.isLoopQueueMode())
                // Get random track on the 50% first part of the list
                track = remainingTracks.get(random.nextInt(remainingTracks.size() / 2));
            else
                // Get random track on the entire list
                track = remainingTracks.get(random.nextInt(remainingTracks.size()));
            remainingTracks.remove(track);
            if (!remainingTracks.isEmpty())
                for (AudioTrack tmpTrack : remainingTracks)
                    tracks.offer(tmpTrack);
        } else {
            track = tracks.poll();
        }
        player.getAudioPlayer().startTrack(track, false);
        return track;
    }

    public synchronized void addTrackFirst(AudioTrack track) {
        List<AudioTrack> remainingTracks = new ArrayList<>();
        tracks.drainTo(remainingTracks);
        tracks.offer(track);
        for (AudioTrack tmpTrack : remainingTracks)
            tracks.offer(tmpTrack);
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
        if (!this.player.isLoopQueueMode())
            JDAManager.getShardManager().setActivity(Activity.streaming(track.getInfo().title, track.getInfo().uri));
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
                tracks.offer(track.makeClone());
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

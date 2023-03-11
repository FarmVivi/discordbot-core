package fr.farmvivi.discordbot.module.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.jda.JDAManager;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TrackScheduler extends AudioEventAdapter {
    private final LinkedList<AudioTrack> tracks = new LinkedList<>();
    private final MusicPlayer player;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> quitTask;

    public TrackScheduler(MusicPlayer player) {
        this.player = player;
    }

    public void queue(AudioTrack track) {
        this.queue(track, false);
    }

    public void queue(AudioTrack track, boolean playNow) {
        Logger logger = player.getMusicModule().getLogger();
        Guild guild = player.getGuild();

        if (quitTask != null) {
            // Log : [<Guild name> (Guild id)] Cancel quit task
            logger.info(String.format("[%s (%s)] Canceling quit task", guild.getName(), guild.getId()));

            quitTask.cancel(true);
            quitTask = null;
        }

        // Log : [<Guild name> (Guild id)] Track added to queue : <Track name> (Link) (now = <true/false>)
        logger.info(String.format("[%s (%s)] Track added to queue : %s (%s) (first = %s)", guild.getName(), guild.getId(), track.getInfo().title, track.getInfo().uri, playNow));

        if (!player.getAudioPlayer().startTrack(track, true)) {
            if (playNow) {
                tracks.addFirst(track);
            } else {
                tracks.offer(track);
            }

            // Refresh player message
            player.getMusicPlayerMessage().refreshMessage();
        }
    }

    public AudioTrack nextTrack() {
        return this.nextTrack(true);
    }

    public AudioTrack nextTrack(boolean delete) {
        Logger logger = player.getMusicModule().getLogger();
        Guild guild = player.getGuild();

        // Log : [<Guild name> (Guild id)] Next track (delete = <true/false>)
        logger.info(String.format("[%s (%s)] Next track (delete = %s)", guild.getName(), guild.getId(), delete));

        if (!delete && player.getAudioPlayer().getPlayingTrack() != null) {
            AudioTrack currentTrack = player.getAudioPlayer().getPlayingTrack();
            this.queue(currentTrack.makeClone());
        }
        if (tracks.isEmpty()) {
            player.getAudioPlayer().stopTrack();
            Bot.setDefaultActivity();
            if (player.getGuild().getAudioManager().getConnectedChannel() != null) {
                int quitTimeout = MusicModule.QUIT_TIMEOUT;

                // Log : [<Guild name> (Guild id)] No more track in queue, quit in <timeout> seconds
                logger.info(String.format("[%s (%s)] No more track in queue, quit in %s seconds", guild.getName(), guild.getId(), quitTimeout));

                quitTask = scheduler.schedule(() -> {
                    if (player.getGuild().getAudioManager().getConnectedChannel() != null)
                        player.getGuild().getAudioManager().closeAudioConnection();
                }, quitTimeout, TimeUnit.SECONDS);
            }
            return null;
        }
        AudioTrack track;
        if (player.isShuffleMode()) {
            List<AudioTrack> remainingTracks = new ArrayList<>(tracks);
            tracks.clear();
            Random random = new Random();
            if (player.isLoopQueueMode() && remainingTracks.size() > 1)
                // Get random track on the 50% first part of the list
                track = remainingTracks.get(random.nextInt(remainingTracks.size() / 2));
            else
                // Get random track on the entire list
                track = remainingTracks.get(random.nextInt(remainingTracks.size()));
            remainingTracks.remove(track);
            if (!remainingTracks.isEmpty()) {
                tracks.addAll(remainingTracks);
            }
        } else {
            track = tracks.poll();
        }
        player.getAudioPlayer().startTrack(track, false);
        return track;
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        Logger logger = this.player.getMusicModule().getLogger();
        Guild guild = this.player.getGuild();

        // Log : [<Guild name> (Guild id)] Player paused
        logger.info(String.format("[%s (%s)] Player paused", guild.getName(), guild.getId()));

        // Player was paused
        this.player.getMusicPlayerMessage().refreshMessage();
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        Logger logger = this.player.getMusicModule().getLogger();
        Guild guild = this.player.getGuild();

        // Log : [<Guild name> (Guild id)] Player resumed
        logger.info(String.format("[%s (%s)] Player resumed", guild.getName(), guild.getId()));

        // Player was resumed
        this.player.getMusicPlayerMessage().refreshMessage();
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        Logger logger = this.player.getMusicModule().getLogger();
        Guild guild = this.player.getGuild();

        // Log : [<Guild name> (Guild id)] Track started : <Track name> (Link)
        logger.info(String.format("[%s (%s)] Track started : %s (%s)", guild.getName(), guild.getId(), track.getInfo().title, track.getInfo().uri));

        // A track started playing
        this.player.getMusicPlayerMessage().refreshMessage();

        if (!this.player.isLoopQueueMode()) {
            JDAManager.getJDA().getPresence().setActivity(Activity.streaming(track.getInfo().title, track.getInfo().uri));
        } else if (!Bot.isDefaultActivity()) {
            Bot.setDefaultActivity();
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        Logger logger = this.player.getMusicModule().getLogger();
        Guild guild = this.player.getGuild();

        // Log : [<Guild name> (Guild id)] Track ended : <Track name> (Link) (reason = <reason>) (startNext = <true/false>)
        logger.info(String.format("[%s (%s)] Track ended : %s (%s) (reason = %s) (startNext = %s)", guild.getName(), guild.getId(), track.getInfo().title, track.getInfo().uri, endReason.name(), endReason.mayStartNext));

        // End of the queue
        this.player.getMusicPlayerMessage().refreshMessage();

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
        // An already playing track threw an exception (track end event will still be received separately)

        Logger logger = this.player.getMusicModule().getLogger();
        Guild guild = this.player.getGuild();

        // Log : [<Guild name> (Guild id)] Track exception : <Track name> (Link) (error = <error>)
        logger.error(String.format("[%s (%s)] Track exception : %s (%s) (error = %s)", guild.getName(), guild.getId(), track.getInfo().title, track.getInfo().uri, exception.getMessage()));
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        // Audio track has been unable to provide us any audio, might want to just start a new track

        Logger logger = this.player.getMusicModule().getLogger();
        Guild guild = this.player.getGuild();

        // Log : [<Guild name> (Guild id)] Track stuck : <Track name> (Link) (threshold = <threshold>ms)
        logger.error(String.format("[%s (%s)] Track stuck : %s (%s) (threshold = %sms)", guild.getName(), guild.getId(), track.getInfo().title, track.getInfo().uri, thresholdMs));
    }

    public LinkedList<AudioTrack> getTracks() {
        return tracks;
    }
}

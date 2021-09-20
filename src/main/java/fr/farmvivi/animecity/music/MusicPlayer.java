package fr.farmvivi.animecity.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;

public class MusicPlayer {
    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private final Guild guild;

    private boolean loopMode = false;

    public MusicPlayer(AudioPlayer audioPlayer, Guild guild) {
        this.audioPlayer = audioPlayer;
        this.guild = guild;
        trackScheduler = new TrackScheduler(this);
        audioPlayer.addListener(trackScheduler);
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public Guild getGuild() {
        return guild;
    }

    public TrackScheduler getListener() {
        return trackScheduler;
    }

    public AudioPlayerSendHandler getAudioPlayerSendHandler() {
        return new AudioPlayerSendHandler(audioPlayer);
    }

    public synchronized void playTrack(AudioTrack track) {
        trackScheduler.queue(track);
    }

    public synchronized AudioTrack skipTrack() {
        return trackScheduler.nextTrack();
    }

    public void setLoopMode(boolean loopMode) {
        this.loopMode = loopMode;
    }

    public boolean isLoopMode() {
        return loopMode;
    }
}

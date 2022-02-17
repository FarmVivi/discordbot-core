package fr.farmvivi.discordbot.module.music;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;

public class MusicPlayer {
    private final MusicModule musicModule;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private final Guild guild;
    private EqualizerFactory equalizer;

    private boolean loopQueueMode = false;
    private boolean loopMode = false;
    private boolean shuffleMode = false;

    public MusicPlayer(MusicModule musicModule, AudioPlayer audioPlayer, Guild guild) {
        this.musicModule = musicModule;
        this.audioPlayer = audioPlayer;
        this.guild = guild;
        this.equalizer = new EqualizerFactory();
        trackScheduler = new TrackScheduler(this);
        audioPlayer.addListener(trackScheduler);
    }

    public MusicModule getMusicModule() {
        return musicModule;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public Guild getGuild() {
        return guild;
    }

    public EqualizerFactory getEqualizer() {
        return equalizer;
    }

    public TrackScheduler getListener() {
        return trackScheduler;
    }

    public AudioPlayerSendHandler getAudioPlayerSendHandler() {
        return new AudioPlayerSendHandler(audioPlayer);
    }

    public synchronized void playTrack(AudioTrack track) {
        this.playTrack(track, false);
    }

    public synchronized void playTrack(AudioTrack track, boolean playNow) {
        trackScheduler.queue(track, playNow);
    }

    public synchronized AudioTrack skipTrack() {
        return this.skipTrack(true);
    }

    public synchronized AudioTrack skipTrack(boolean delete) {
        return trackScheduler.nextTrack(delete);
    }

    public void resetToDefaultSettings() {
        this.loopQueueMode = false;
        this.loopMode = false;
        this.shuffleMode = false;
        audioPlayer.setVolume(MusicModule.DEFAULT_VOICE_VOLUME);
        audioPlayer.setFilterFactory(null);
        this.equalizer = new EqualizerFactory();
    }

    public void setLoopQueueMode(boolean loopQueueMode) {
        this.loopQueueMode = loopQueueMode;
    }

    public boolean isLoopQueueMode() {
        return loopQueueMode;
    }

    public void setLoopMode(boolean loopMode) {
        this.loopMode = loopMode;
    }

    public boolean isLoopMode() {
        return loopMode;
    }

    public void setShuffleMode(boolean shuffleMode) {
        this.shuffleMode = shuffleMode;
    }

    public boolean isShuffleMode() {
        return shuffleMode;
    }
}

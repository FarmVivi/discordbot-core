package fr.farmvivi.discordbot.module.music;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;

public class MusicPlayer {
    private final MusicModule musicModule;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private final MusicPlayerMessage musicPlayerMessage;
    private final Guild guild;
    private EqualizerFactory equalizer;

    private boolean loopQueueMode = false;
    private boolean loopMode = false;
    private boolean shuffleMode = false;

    public MusicPlayer(MusicModule musicModule, AudioPlayer audioPlayer, Guild guild) {
        this.musicModule = musicModule;
        this.audioPlayer = audioPlayer;
        this.musicPlayerMessage = new MusicPlayerMessage(this);
        this.guild = guild;
        this.equalizer = new EqualizerFactory();
        this.trackScheduler = new TrackScheduler(this);
        audioPlayer.addListener(trackScheduler);
    }

    public MusicModule getMusicModule() {
        return musicModule;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public MusicPlayerMessage getMusicPlayerMessage() {
        return musicPlayerMessage;
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

    public void playTrackNow(AudioTrack track) {
        this.playTrack(track, true);
    }

    public void playTrack(AudioTrack track) {
        this.playTrack(track, false);
    }

    private synchronized void playTrack(AudioTrack track, boolean playNow) {
        trackScheduler.queue(track, playNow);
    }

    public AudioTrack nextTrack() {
        return this.skipTrack(false);
    }

    public AudioTrack skipTrack() {
        return this.skipTrack(true);
    }

    private synchronized AudioTrack skipTrack(boolean delete) {
        return trackScheduler.nextTrack(delete);
    }

    public void resetToDefaultSettings() {
        this.loopQueueMode = false;
        this.loopMode = false;
        this.shuffleMode = false;
        audioPlayer.setVolume(MusicModule.DEFAULT_VOICE_VOLUME);
        audioPlayer.setFilterFactory(null);
        this.equalizer = new EqualizerFactory();
        this.musicPlayerMessage.refreshMessage();
    }

    public boolean isLoopQueueMode() {
        return loopQueueMode;
    }

    public void setLoopQueueMode(boolean loopQueueMode) {
        if (this.loopQueueMode != loopQueueMode) {
            this.musicPlayerMessage.refreshMessage();
            this.loopQueueMode = loopQueueMode;
        }
    }

    public boolean isLoopMode() {
        return loopMode;
    }

    public void setLoopMode(boolean loopMode) {
        if (this.loopMode != loopMode) {
            this.musicPlayerMessage.refreshMessage();
            this.loopMode = loopMode;
        }
    }

    public boolean isShuffleMode() {
        return shuffleMode;
    }

    public void setShuffleMode(boolean shuffleMode) {
        if (this.shuffleMode != shuffleMode) {
            this.musicPlayerMessage.refreshMessage();
            this.shuffleMode = shuffleMode;
        }
    }
}

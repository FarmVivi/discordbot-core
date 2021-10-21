package fr.farmvivi.animecity.music;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.farmvivi.animecity.Bot;
import net.dv8tion.jda.api.entities.Guild;

public class MusicPlayer {
    public static final int DEFAULT_VOICE_VOLUME = 1;
    public static final int DEFAULT_RADIO_VOLUME = 25;

    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private final Guild guild;
    private EqualizerFactory equalizer;

    private boolean loopQueueMode = false;
    private boolean loopMode = false;
    private boolean shuffleMode = false;

    public MusicPlayer(AudioPlayer audioPlayer, Guild guild) {
        this.audioPlayer = audioPlayer;
        this.guild = guild;
        this.equalizer = new EqualizerFactory();
        trackScheduler = new TrackScheduler(this);
        audioPlayer.addListener(trackScheduler);
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
        trackScheduler.queue(track);
    }

    public synchronized AudioTrack skipTrack() {
        return trackScheduler.nextTrack();
    }

    public void resetToDefaultSettings() {
        this.loopQueueMode = false;
        this.loopMode = false;
        this.shuffleMode = false;
        if (Bot.getInstance().getConfiguration().radioMode)
            audioPlayer.setVolume(DEFAULT_RADIO_VOLUME);
        else
            audioPlayer.setVolume(DEFAULT_VOICE_VOLUME);
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

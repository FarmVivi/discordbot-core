package fr.farmvivi.discordbot.module.music;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import java.util.List;

public class MusicPlayer {
    private final MusicModule musicModule;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private final MusicPlayerMessage musicPlayerMessage;
    private final Guild guild;
    private EqualizerFactory equalizer;

    private int lastVolume = 0;
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

    public int getQueueSize() {
        return this.trackScheduler.getTracks().size();
    }

    public List<AudioTrack> getQueue() {
        return this.trackScheduler.getTracks();
    }

    public void clearQueue() {
        this.trackScheduler.getTracks().clear();
        this.musicPlayerMessage.refreshMessage();

        // Log : [<Guild name> (Guild id)] Queue cleared
        this.musicModule.getLogger().info(String.format("[%s (%s)] Queue cleared", this.guild.getName(), this.guild.getId()));
    }

    public int getVolume() {
        return this.audioPlayer.getVolume();
    }

    public void setVolume(int volume) {
        int lastVolume = this.audioPlayer.getVolume();
        if (lastVolume > 0) {
            this.lastVolume = lastVolume;
        }
        this.audioPlayer.setVolume(volume);

        // Log : [<Guild name> (Guild id)] Volume changed to <volume>
        this.musicModule.getLogger().info(String.format("[%s (%s)] Volume changed to %d%%", this.guild.getName(), this.guild.getId(), volume));

        this.musicPlayerMessage.refreshMessage();
    }

    public void mute() {
        this.setVolume(0);
    }

    public void unmute() {
        if (this.lastVolume == 0) {
            this.lastVolume = MusicModule.DEFAULT_VOICE_VOLUME;
        }
        this.setVolume(this.lastVolume);
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

    public void resetToDefaultSettings() {
        this.setLoopQueueMode(false);
        this.setLoopMode(false);
        this.setShuffleMode(false);
        this.setVolume(MusicModule.DEFAULT_VOICE_VOLUME);
        audioPlayer.setFilterFactory(null);
        this.equalizer = new EqualizerFactory();
    }
}

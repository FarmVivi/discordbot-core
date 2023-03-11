package fr.farmvivi.discordbot.module.music;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;

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

        // Log : [<Guild name> (Guild id)] Muted
        this.musicModule.getLogger().info(String.format("[%s (%s)] Muted", this.guild.getName(), this.guild.getId()));
    }

    public void unmute() {
        if (this.lastVolume == 0) {
            this.lastVolume = MusicModule.DEFAULT_VOICE_VOLUME;
        }
        this.setVolume(this.lastVolume);

        // Log : [<Guild name> (Guild id)] Unmuted
        this.musicModule.getLogger().info(String.format("[%s (%s)] Unmuted", this.guild.getName(), this.guild.getId()));
    }

    public boolean isLoopQueueMode() {
        return loopQueueMode;
    }

    public void setLoopQueueMode(boolean loopQueueMode) {
        if (this.loopQueueMode != loopQueueMode) {
            this.loopQueueMode = loopQueueMode;

            if (loopQueueMode) {
                // Log : [<Guild name> (Guild id)] Loop queue mode enabled
                this.musicModule.getLogger().info(String.format("[%s (%s)] Loop queue mode enabled", this.guild.getName(), this.guild.getId()));
            } else {
                // Log : [<Guild name> (Guild id)] Loop queue mode disabled
                this.musicModule.getLogger().info(String.format("[%s (%s)] Loop queue mode disabled", this.guild.getName(), this.guild.getId()));
            }

            this.musicPlayerMessage.refreshMessage();
        }
    }

    public boolean isLoopMode() {
        return loopMode;
    }

    public void setLoopMode(boolean loopMode) {
        if (this.loopMode != loopMode) {
            this.loopMode = loopMode;

            if (loopMode) {
                // Log : [<Guild name> (Guild id)] Loop mode enabled
                this.musicModule.getLogger().info(String.format("[%s (%s)] Loop mode enabled", this.guild.getName(), this.guild.getId()));
            } else {
                // Log : [<Guild name> (Guild id)] Loop mode disabled
                this.musicModule.getLogger().info(String.format("[%s (%s)] Loop mode disabled", this.guild.getName(), this.guild.getId()));
            }

            this.musicPlayerMessage.refreshMessage();
        }
    }

    public boolean isShuffleMode() {
        return shuffleMode;
    }

    public void setShuffleMode(boolean shuffleMode) {
        if (this.shuffleMode != shuffleMode) {
            this.shuffleMode = shuffleMode;

            if (shuffleMode) {
                // Log : [<Guild name> (Guild id)] Shuffle mode enabled
                this.musicModule.getLogger().info(String.format("[%s (%s)] Shuffle mode enabled", this.guild.getName(), this.guild.getId()));
            } else {
                // Log : [<Guild name> (Guild id)] Shuffle mode disabled
                this.musicModule.getLogger().info(String.format("[%s (%s)] Shuffle mode disabled", this.guild.getName(), this.guild.getId()));
            }

            this.musicPlayerMessage.refreshMessage();
        }
    }

    public void resetToDefaultSettings() {
        // Log : [<Guild name> (Guild id)] Resetting to default settings...
        this.musicModule.getLogger().info(String.format("[%s (%s)] Resetting to default settings...", this.guild.getName(), this.guild.getId()));

        this.setLoopQueueMode(false);
        this.setLoopMode(false);
        this.setShuffleMode(false);
        this.setVolume(MusicModule.DEFAULT_VOICE_VOLUME);
        audioPlayer.setFilterFactory(null);
        this.equalizer = new EqualizerFactory();
    }
}

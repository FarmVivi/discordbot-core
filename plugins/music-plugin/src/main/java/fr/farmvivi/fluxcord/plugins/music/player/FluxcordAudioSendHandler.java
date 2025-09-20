package fr.farmvivi.fluxcord.plugins.music.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

/**
 * Audio send handler that integrates LavaPlayer with JDA for Fluxcord.
 * This bridges the audio data from LavaPlayer to Discord's audio system.
 */
public class FluxcordAudioSendHandler implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private final MutableAudioFrame frame;
    private final ByteBuffer buffer;
    
    public FluxcordAudioSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.buffer = ByteBuffer.allocate(1024);
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }
    
    @Override
    public boolean canProvide() {
        // Check if we can provide audio data
        return audioPlayer.provide(frame);
    }
    
    @Override
    public ByteBuffer provide() {
        // Flip buffer to prepare for reading
        buffer.flip();
        return buffer;
    }
    
    @Override
    public boolean isOpus() {
        // We're providing PCM audio, not Opus
        return false;
    }
}
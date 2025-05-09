package fr.farmvivi.discordbot.core.audio;

import fr.farmvivi.discordbot.core.api.audio.AudioFactory;
import fr.farmvivi.discordbot.core.api.audio.MixingAudioSendHandler;
import fr.farmvivi.discordbot.core.api.audio.MultiUserAudioReceiveHandler;
import fr.farmvivi.discordbot.core.audio.handlers.BasicAudioReceiveHandler;
import fr.farmvivi.discordbot.core.audio.handlers.BasicAudioSendHandler;
import fr.farmvivi.discordbot.core.audio.handlers.SimpleMixingAudioSendHandler;
import fr.farmvivi.discordbot.core.audio.handlers.SimpleMultiUserAudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.SpeakingMode;

/**
 * Implementation of the AudioFactory interface.
 * This class creates various audio handlers for use with the audio system.
 */
public class BasicAudioFactory implements AudioFactory {

    @Override
    public AudioSendHandler createAudioSendHandler() {
        return new BasicAudioSendHandler();
    }

    @Override
    public AudioSendHandler createAudioSendHandler(SpeakingMode speakingMode) {
        return new BasicAudioSendHandler(speakingMode);
    }

    @Override
    public AudioReceiveHandler createAudioReceiveHandler(boolean receiveCombined, boolean receiveUser) {
        return new BasicAudioReceiveHandler(receiveCombined, receiveUser);
    }

    @Override
    public MixingAudioSendHandler createMixingAudioSendHandler() {
        return new SimpleMixingAudioSendHandler();
    }

    @Override
    public MixingAudioSendHandler createMixingAudioSendHandler(SpeakingMode speakingMode) {
        return new SimpleMixingAudioSendHandler(speakingMode);
    }

    @Override
    public MultiUserAudioReceiveHandler createMultiUserAudioReceiveHandler(boolean receiveCombined, boolean receiveUser) {
        return new SimpleMultiUserAudioReceiveHandler(receiveCombined, receiveUser);
    }
}
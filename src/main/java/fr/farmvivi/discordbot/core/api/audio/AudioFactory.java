package fr.farmvivi.discordbot.core.api.audio;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.SpeakingMode;

/**
 * Factory for creating audio handlers.
 * This interface provides methods for creating various audio handler implementations.
 */
public interface AudioFactory {
    /**
     * Creates a basic audio send handler.
     * This handler is used to send audio to a voice channel.
     *
     * @return A new audio send handler
     */
    AudioSendHandler createAudioSendHandler();

    /**
     * Creates a basic audio send handler with a specific speaking mode.
     * This handler is used to send audio to a voice channel.
     *
     * @param speakingMode The speaking mode
     * @return A new audio send handler
     */
    AudioSendHandler createAudioSendHandler(SpeakingMode speakingMode);

    /**
     * Creates a basic audio receive handler.
     * This handler is used to receive audio from a voice channel.
     *
     * @param receiveCombined Whether to receive combined audio
     * @param receiveUser     Whether to receive user audio
     * @return A new audio receive handler
     */
    AudioReceiveHandler createAudioReceiveHandler(boolean receiveCombined, boolean receiveUser);

    /**
     * Creates a mixing audio send handler.
     * This handler can mix multiple audio sources together.
     *
     * @return A new mixing audio send handler
     */
    MixingAudioSendHandler createMixingAudioSendHandler();

    /**
     * Creates a mixing audio send handler with a specific speaking mode.
     * This handler can mix multiple audio sources together.
     *
     * @param speakingMode The speaking mode
     * @return A new mixing audio send handler
     */
    MixingAudioSendHandler createMixingAudioSendHandler(SpeakingMode speakingMode);

    /**
     * Creates a multi-user audio receive handler.
     * This handler can process audio from multiple users.
     *
     * @param receiveCombined Whether to receive combined audio
     * @param receiveUser     Whether to receive user audio
     * @return A new multi-user audio receive handler
     */
    MultiUserAudioReceiveHandler createMultiUserAudioReceiveHandler(boolean receiveCombined, boolean receiveUser);
}
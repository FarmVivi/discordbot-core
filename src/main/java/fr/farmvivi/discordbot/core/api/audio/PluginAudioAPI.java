package fr.farmvivi.discordbot.core.api.audio;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * API for plugins to interact with audio functionality.
 * This interface provides a simplified way for plugins to use audio features.
 */
public interface PluginAudioAPI {
    /**
     * Joins a voice channel.
     *
     * @param voiceChannel The voice channel to join
     * @return true if joined successfully, false otherwise
     */
    boolean joinVoiceChannel(VoiceChannel voiceChannel);

    /**
     * Leaves a voice channel in a guild.
     *
     * @param guild The guild
     * @return true if left successfully, false otherwise
     */
    boolean leaveVoiceChannel(Guild guild);

    /**
     * Checks if the bot is connected to a voice channel in a guild.
     *
     * @param guild The guild
     * @return true if connected, false otherwise
     */
    boolean isConnected(Guild guild);

    /**
     * Gets the voice channel the bot is connected to in a guild.
     *
     * @param guild The guild to check
     * @return An Optional containing the voice channel, or empty if not connected
     */
    Optional<VoiceChannel> getConnectedChannel(Guild guild);

    /**
     * Creates a basic audio send handler.
     *
     * @return A new audio send handler
     */
    AudioSendHandler createAudioSendHandler();

    /**
     * Creates a basic audio send handler with a specific speaking mode.
     *
     * @param speakingMode The speaking mode
     * @return A new audio send handler
     */
    AudioSendHandler createAudioSendHandler(SpeakingMode speakingMode);

    /**
     * Creates a basic audio receive handler.
     *
     * @param receiveCombined Whether to receive combined audio
     * @param receiveUser Whether to receive user audio
     * @return A new audio receive handler
     */
    AudioReceiveHandler createAudioReceiveHandler(boolean receiveCombined, boolean receiveUser);

    /**
     * Creates a mixing audio send handler.
     *
     * @return A new mixing audio send handler
     */
    MixingAudioSendHandler createMixingAudioSendHandler();

    /**
     * Creates a mixing audio send handler with a specific speaking mode.
     *
     * @param speakingMode The speaking mode
     * @return A new mixing audio send handler
     */
    MixingAudioSendHandler createMixingAudioSendHandler(SpeakingMode speakingMode);

    /**
     * Creates a multi-user audio receive handler.
     *
     * @param receiveCombined Whether to receive combined audio
     * @param receiveUser Whether to receive user audio
     * @return A new multi-user audio receive handler
     */
    MultiUserAudioReceiveHandler createMultiUserAudioReceiveHandler(boolean receiveCombined, boolean receiveUser);

    /**
     * Registers an audio send handler for a guild.
     *
     * @param guild The guild
     * @param handler The audio send handler
     * @return true if registered successfully, false otherwise
     */
    boolean registerAudioSendHandler(Guild guild, AudioSendHandler handler);

    /**
     * Registers an audio receive handler for a guild.
     *
     * @param guild The guild
     * @param handler The audio receive handler
     * @return true if registered successfully, false otherwise
     */
    boolean registerAudioReceiveHandler(Guild guild, AudioReceiveHandler handler);

    /**
     * Unregisters the audio send handler for a guild.
     *
     * @param guild The guild
     * @return true if unregistered successfully, false otherwise
     */
    boolean unregisterAudioSendHandler(Guild guild);

    /**
     * Unregisters the audio receive handler for a guild.
     *
     * @param guild The guild
     * @return true if unregistered successfully, false otherwise
     */
    boolean unregisterAudioReceiveHandler(Guild guild);

    /**
     * Gets the audio send handler for a guild.
     *
     * @param guild The guild
     * @return An Optional containing the audio send handler, or empty if none is registered
     */
    Optional<AudioSendHandler> getAudioSendHandler(Guild guild);

    /**
     * Gets the audio receive handler for a guild.
     *
     * @param guild The guild
     * @return An Optional containing the audio receive handler, or empty if none is registered
     */
    Optional<AudioReceiveHandler> getAudioReceiveHandler(Guild guild);

    /**
     * Sets up a mixing audio handler for a guild.
     * This is a convenience method for creating and registering a mixing audio handler.
     *
     * @param guild The guild
     * @param speakingMode The speaking mode
     * @return The created mixing audio handler, or empty if the operation failed
     */
    Optional<MixingAudioSendHandler> setupMixingAudio(Guild guild, SpeakingMode speakingMode);

    /**
     * Sets up a multi-user audio receive handler for a guild.
     * This is a convenience method for creating and registering a multi-user audio handler.
     *
     * @param guild The guild
     * @param combinedAudioHandler Handler for combined audio
     * @param userAudioHandlers Handlers for specific users' audio
     * @return The created multi-user audio handler, or empty if the operation failed
     */
    Optional<MultiUserAudioReceiveHandler> setupMultiUserAudio(Guild guild, 
                                                              Consumer<byte[]> combinedAudioHandler,
                                                              Map<String, Consumer<byte[]>> userAudioHandlers);

    /**
     * Gets the users currently speaking in a guild's voice channel.
     *
     * @param guild The guild
     * @return A set of speaking users, or an empty set if not connected or no users are speaking
     */
    Set<User> getSpeakingUsers(Guild guild);

    /**
     * Queues audio data to be sent to a guild's voice channel.
     * This is a convenience method that creates or uses an existing send handler.
     *
     * @param guild The guild
     * @param data The audio data
     * @return true if the data was queued successfully, false otherwise
     */
    boolean queueAudio(Guild guild, byte[] data);

    /**
     * Queues audio data to a specific source in a mixing audio handler.
     * This is a convenience method that creates or uses an existing mixing audio handler.
     *
     * @param guild The guild
     * @param sourceId The source identifier
     * @param data The audio data
     * @return true if the data was queued successfully, false otherwise
     */
    boolean queueAudioToSource(Guild guild, String sourceId, byte[] data);

    /**
     * Queues audio data to a specific source in a mixing audio handler.
     * This is a convenience method that creates or uses an existing mixing audio handler.
     *
     * @param guild The guild
     * @param sourceId The source identifier
     * @param buffer The audio data
     * @return true if the data was queued successfully, false otherwise
     */
    boolean queueAudioToSource(Guild guild, String sourceId, ByteBuffer buffer);
}
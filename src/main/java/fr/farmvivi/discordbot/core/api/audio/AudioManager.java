package fr.farmvivi.discordbot.core.api.audio;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.io.Closeable;
import java.util.Optional;

/**
 * Manages audio functionality for voice channels.
 * This interface provides methods for joining/leaving voice channels and
 * registering audio handlers for sending and receiving audio.
 */
public interface AudioManager extends Closeable {
    /**
     * Joins a voice channel.
     *
     * @param voiceChannel The voice channel to join
     * @return true if the operation was successful, false otherwise
     */
    boolean joinVoiceChannel(VoiceChannel voiceChannel);

    /**
     * Leaves the voice channel in a guild.
     *
     * @param guild The guild containing the voice channel
     * @return true if the operation was successful, false otherwise
     */
    boolean leaveVoiceChannel(Guild guild);

    /**
     * Checks if the bot is connected to a voice channel in a guild.
     *
     * @param guild The guild to check
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
     * Registers an audio send handler for a guild.
     * This handler is used to send audio to a voice channel.
     *
     * @param guild   The guild
     * @param handler The audio send handler
     * @return true if the operation was successful, false otherwise
     */
    boolean registerAudioSendHandler(Guild guild, AudioSendHandler handler);

    /**
     * Registers an audio receive handler for a guild.
     * This handler is used to receive audio from a voice channel.
     *
     * @param guild   The guild
     * @param handler The audio receive handler
     * @return true if the operation was successful, false otherwise
     */
    boolean registerAudioReceiveHandler(Guild guild, AudioReceiveHandler handler);

    /**
     * Unregisters the audio send handler for a guild.
     *
     * @param guild The guild
     * @return true if the operation was successful, false otherwise
     */
    boolean unregisterAudioSendHandler(Guild guild);

    /**
     * Unregisters the audio receive handler for a guild.
     *
     * @param guild The guild
     * @return true if the operation was successful, false otherwise
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
}
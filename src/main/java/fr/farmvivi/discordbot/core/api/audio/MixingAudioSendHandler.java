package fr.farmvivi.discordbot.core.api.audio;

import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;
import java.util.Set;

/**
 * An audio send handler that can mix multiple audio sources.
 * This handler is useful for playing multiple audio streams simultaneously.
 */
public interface MixingAudioSendHandler extends AudioSendHandler {
    /**
     * Adds an audio source.
     *
     * @param sourceId A unique identifier for the source
     * @return true if the source was added, false if it already exists
     */
    boolean addAudioSource(String sourceId);

    /**
     * Removes an audio source.
     *
     * @param sourceId The source identifier
     * @return true if the source was removed, false if it doesn't exist
     */
    boolean removeAudioSource(String sourceId);

    /**
     * Gets all registered audio source IDs.
     *
     * @return A set of source IDs
     */
    Set<String> getAudioSources();

    /**
     * Queues audio data for a specific source.
     *
     * @param sourceId The source identifier
     * @param data     The audio data to queue
     * @return true if the data was queued, false if the source doesn't exist
     */
    boolean queueAudio(String sourceId, byte[] data);

    /**
     * Queues audio data for a specific source.
     *
     * @param sourceId The source identifier
     * @param buffer   The audio data to queue
     * @return true if the data was queued, false if the source doesn't exist
     */
    boolean queueAudio(String sourceId, ByteBuffer buffer);

    /**
     * Clears the audio queue for a specific source.
     *
     * @param sourceId The source identifier
     * @return true if the queue was cleared, false if the source doesn't exist
     */
    boolean clearSourceQueue(String sourceId);

    /**
     * Clears all audio queues.
     */
    void clearAllQueues();

    /**
     * Checks if a source has audio data queued.
     *
     * @param sourceId The source identifier
     * @return true if the source has data, false otherwise or if the source doesn't exist
     */
    boolean hasQueuedAudio(String sourceId);

    /**
     * Gets the number of queued audio packets for a source.
     *
     * @param sourceId The source identifier
     * @return The number of queued packets, or 0 if the source doesn't exist
     */
    int getQueueSize(String sourceId);
}
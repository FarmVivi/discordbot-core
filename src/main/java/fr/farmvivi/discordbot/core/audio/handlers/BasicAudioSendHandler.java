package fr.farmvivi.discordbot.core.audio.handlers;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.SpeakingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Basic implementation of AudioSendHandler.
 * This handler is used to send audio to a voice channel.
 */
public class BasicAudioSendHandler implements AudioSendHandler {
    private static final Logger logger = LoggerFactory.getLogger(BasicAudioSendHandler.class);
    private static final int BUFFER_CAPACITY = 400; // Capacity limiter to prevent memory issues

    protected final Queue<ByteBuffer> queue;
    protected final ReentrantLock queueLock;
    protected final SpeakingMode speakingMode;

    /**
     * Creates a new BasicAudioSendHandler with default speaking mode (VOICE).
     */
    public BasicAudioSendHandler() {
        this(SpeakingMode.VOICE);
    }

    /**
     * Creates a new BasicAudioSendHandler with a specific speaking mode.
     *
     * @param speakingMode The speaking mode
     */
    public BasicAudioSendHandler(SpeakingMode speakingMode) {
        this.queue = new ArrayDeque<>();
        this.queueLock = new ReentrantLock();
        this.speakingMode = speakingMode;
    }

    @Override
    public boolean canProvide() {
        return !queue.isEmpty();
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        ByteBuffer buffer = null;
        try {
            queueLock.lock();
            buffer = queue.poll();
        } finally {
            queueLock.unlock();
        }

        if (buffer != null) {
            buffer.flip();
        }

        return buffer;
    }

    @Override
    public boolean isOpus() {
        return false; // Using PCM audio, not Opus-encoded
    }

    /**
     * Queues audio data to be sent.
     *
     * @param data The audio data to queue
     */
    public void queueAudio(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }

        try {
            queueLock.lock();

            // Check if we're at capacity to prevent memory issues
            if (queue.size() >= BUFFER_CAPACITY) {
                logger.warn("Audio queue is at capacity ({}), dropping oldest packet", BUFFER_CAPACITY);
                queue.poll(); // Remove oldest packet
            }

            queue.add(ByteBuffer.wrap(data));
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * Queues audio data to be sent.
     *
     * @param buffer The audio data to queue
     */
    public void queueAudio(ByteBuffer buffer) {
        if (buffer == null || !buffer.hasRemaining()) {
            return;
        }

        try {
            queueLock.lock();

            // Check if we're at capacity to prevent memory issues
            if (queue.size() >= BUFFER_CAPACITY) {
                logger.warn("Audio queue is at capacity ({}), dropping oldest packet", BUFFER_CAPACITY);
                queue.poll(); // Remove oldest packet
            }

            queue.add(buffer);
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * Clears the audio queue.
     */
    public void clearQueue() {
        try {
            queueLock.lock();
            queue.clear();
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * Gets the number of queued audio packets.
     *
     * @return The number of queued packets
     */
    public int getQueueSize() {
        try {
            queueLock.lock();
            return queue.size();
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * Checks if the queue has audio data.
     *
     * @return true if the queue has data, false otherwise
     */
    public boolean hasQueuedAudio() {
        try {
            queueLock.lock();
            return !queue.isEmpty();
        } finally {
            queueLock.unlock();
        }
    }
}
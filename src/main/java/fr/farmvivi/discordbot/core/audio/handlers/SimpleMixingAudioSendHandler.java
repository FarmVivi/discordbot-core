package fr.farmvivi.discordbot.core.audio.handlers;

import fr.farmvivi.discordbot.core.api.audio.MixingAudioSendHandler;
import net.dv8tion.jda.api.audio.SpeakingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of MixingAudioSendHandler.
 * This handler can mix multiple audio sources together.
 */
public class SimpleMixingAudioSendHandler implements MixingAudioSendHandler {
    private static final Logger logger = LoggerFactory.getLogger(SimpleMixingAudioSendHandler.class);
    private static final int BUFFER_CAPACITY = 200; // Capacity limiter per source

    protected final Map<String, Queue<ByteBuffer>> sourceQueues;
    protected final Map<String, ReentrantLock> sourceLocks;
    protected final SpeakingMode speakingMode;

    /**
     * Creates a new SimpleMixingAudioSendHandler with default speaking mode (VOICE).
     */
    public SimpleMixingAudioSendHandler() {
        this(SpeakingMode.VOICE);
    }

    /**
     * Creates a new SimpleMixingAudioSendHandler with a specific speaking mode.
     *
     * @param speakingMode The speaking mode
     */
    public SimpleMixingAudioSendHandler(SpeakingMode speakingMode) {
        this.sourceQueues = new ConcurrentHashMap<>();
        this.sourceLocks = new ConcurrentHashMap<>();
        this.speakingMode = speakingMode;
    }

    @Override
    public boolean canProvide() {
        // Can provide if any source has audio queued
        return sourceQueues.values().stream().anyMatch(q -> !q.isEmpty());
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        // No sources with audio
        if (!canProvide()) {
            return null;
        }

        // Get audio from all sources with data
        List<ByteBuffer> buffers = new ArrayList<>();

        for (Map.Entry<String, Queue<ByteBuffer>> entry : sourceQueues.entrySet()) {
            String sourceId = entry.getKey();
            Queue<ByteBuffer> queue = entry.getValue();
            ReentrantLock lock = sourceLocks.get(sourceId);

            try {
                lock.lock();
                if (!queue.isEmpty()) {
                    ByteBuffer buffer = queue.poll();
                    if (buffer != null) {
                        buffer.flip();
                        buffers.add(buffer);
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        // No data available
        if (buffers.isEmpty()) {
            return null;
        }

        // If only one source had data, return it directly
        if (buffers.size() == 1) {
            return buffers.get(0);
        }

        // Mix audio from all sources
        return mixAudio(buffers);
    }

    /**
     * Mixes audio from multiple sources.
     *
     * @param buffers The audio buffers to mix
     * @return The mixed audio
     */
    private ByteBuffer mixAudio(List<ByteBuffer> buffers) {
        // Determine the size of the output buffer (should be 20ms of audio, typically 3840 bytes for stereo 48kHz 16-bit PCM)
        int minSize = Integer.MAX_VALUE;
        for (ByteBuffer buffer : buffers) {
            minSize = Math.min(minSize, buffer.remaining());
        }

        // Create output buffer
        ByteBuffer mixed = ByteBuffer.allocate(minSize);

        // Mix audio samples
        short[] sampleMix = new short[minSize / 2]; // 16-bit samples (2 bytes per sample)

        for (ByteBuffer buffer : buffers) {
            for (int i = 0; i < minSize / 2; i++) {
                // Read 16-bit sample
                short sample = buffer.getShort();

                // Add to mix, avoiding overflow
                int mixedSample = sampleMix[i] + sample;
                if (mixedSample > Short.MAX_VALUE) {
                    mixedSample = Short.MAX_VALUE;
                } else if (mixedSample < Short.MIN_VALUE) {
                    mixedSample = Short.MIN_VALUE;
                }
                sampleMix[i] = (short) mixedSample;
            }
            buffer.rewind(); // Rewind for next use
        }

        // Write mixed samples to output buffer
        for (short sample : sampleMix) {
            mixed.putShort(sample);
        }

        mixed.flip();
        return mixed;
    }

    @Override
    public boolean isOpus() {
        return false; // Using PCM audio, not Opus-encoded
    }

    @Override
    public boolean addAudioSource(String sourceId) {
        if (sourceQueues.containsKey(sourceId)) {
            return false; // Source already exists
        }

        sourceQueues.put(sourceId, new ArrayDeque<>());
        sourceLocks.put(sourceId, new ReentrantLock());
        logger.debug("Added audio source: {}", sourceId);
        return true;
    }

    @Override
    public boolean removeAudioSource(String sourceId) {
        if (!sourceQueues.containsKey(sourceId)) {
            return false; // Source doesn't exist
        }

        ReentrantLock lock = sourceLocks.get(sourceId);
        try {
            lock.lock();
            sourceQueues.remove(sourceId);
            sourceLocks.remove(sourceId);
            logger.debug("Removed audio source: {}", sourceId);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<String> getAudioSources() {
        return new HashSet<>(sourceQueues.keySet());
    }

    @Override
    public boolean queueAudio(String sourceId, byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }

        return queueAudio(sourceId, ByteBuffer.wrap(data));
    }

    @Override
    public boolean queueAudio(String sourceId, ByteBuffer buffer) {
        if (buffer == null || !buffer.hasRemaining()) {
            return false;
        }

        Queue<ByteBuffer> queue = sourceQueues.get(sourceId);
        ReentrantLock lock = sourceLocks.get(sourceId);

        if (queue == null || lock == null) {
            // Add source if it doesn't exist
            if (!addAudioSource(sourceId)) {
                return false;
            }
            queue = sourceQueues.get(sourceId);
            lock = sourceLocks.get(sourceId);
        }

        try {
            lock.lock();

            // Check if we're at capacity to prevent memory issues
            if (queue.size() >= BUFFER_CAPACITY) {
                logger.warn("Audio queue for source {} is at capacity ({}), dropping oldest packet", sourceId, BUFFER_CAPACITY);
                queue.poll(); // Remove oldest packet
            }

            queue.add(buffer);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean clearSourceQueue(String sourceId) {
        Queue<ByteBuffer> queue = sourceQueues.get(sourceId);
        ReentrantLock lock = sourceLocks.get(sourceId);

        if (queue == null || lock == null) {
            return false; // Source doesn't exist
        }

        try {
            lock.lock();
            queue.clear();
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clearAllQueues() {
        for (String sourceId : getAudioSources()) {
            clearSourceQueue(sourceId);
        }
    }

    @Override
    public boolean hasQueuedAudio(String sourceId) {
        Queue<ByteBuffer> queue = sourceQueues.get(sourceId);
        ReentrantLock lock = sourceLocks.get(sourceId);

        if (queue == null || lock == null) {
            return false; // Source doesn't exist
        }

        try {
            lock.lock();
            return !queue.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getQueueSize(String sourceId) {
        Queue<ByteBuffer> queue = sourceQueues.get(sourceId);
        ReentrantLock lock = sourceLocks.get(sourceId);

        if (queue == null || lock == null) {
            return 0; // Source doesn't exist
        }

        try {
            lock.lock();
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
}
package fr.farmvivi.discordbot.core.audio;

import fr.farmvivi.discordbot.core.api.audio.*;
import fr.farmvivi.discordbot.core.audio.handlers.BasicAudioSendHandler;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Adapter for plugins to interact with audio functionality.
 * This class provides a simplified interface for plugins to use audio features.
 */
public class PluginAudioAdapter implements PluginAudioAPI {
    private static final Logger logger = LoggerFactory.getLogger(PluginAudioAdapter.class);

    private final AudioManager audioManager;
    private final AudioFactory audioFactory;
    private final String pluginId; // Keeping plugin ID for logging purposes

    /**
     * Creates a new PluginAudioAdapter.
     *
     * @param audioManager The audio manager
     * @param audioFactory The audio factory
     * @param pluginId     The plugin ID
     */
    public PluginAudioAdapter(AudioManager audioManager, AudioFactory audioFactory, String pluginId) {
        this.audioManager = audioManager;
        this.audioFactory = audioFactory;
        this.pluginId = pluginId;
    }

    @Override
    public boolean joinVoiceChannel(VoiceChannel voiceChannel) {
        return audioManager.joinVoiceChannel(voiceChannel);
    }

    @Override
    public boolean leaveVoiceChannel(Guild guild) {
        return audioManager.leaveVoiceChannel(guild);
    }

    @Override
    public boolean isConnected(Guild guild) {
        return audioManager.isConnected(guild);
    }

    @Override
    public Optional<VoiceChannel> getConnectedChannel(Guild guild) {
        return audioManager.getConnectedChannel(guild);
    }

    @Override
    public AudioSendHandler createAudioSendHandler() {
        return audioFactory.createAudioSendHandler();
    }

    @Override
    public AudioSendHandler createAudioSendHandler(SpeakingMode speakingMode) {
        return audioFactory.createAudioSendHandler(speakingMode);
    }

    @Override
    public AudioReceiveHandler createAudioReceiveHandler(boolean receiveCombined, boolean receiveUser) {
        return audioFactory.createAudioReceiveHandler(receiveCombined, receiveUser);
    }

    @Override
    public MixingAudioSendHandler createMixingAudioSendHandler() {
        return audioFactory.createMixingAudioSendHandler();
    }

    @Override
    public MixingAudioSendHandler createMixingAudioSendHandler(SpeakingMode speakingMode) {
        return audioFactory.createMixingAudioSendHandler(speakingMode);
    }

    @Override
    public MultiUserAudioReceiveHandler createMultiUserAudioReceiveHandler(boolean receiveCombined, boolean receiveUser) {
        return audioFactory.createMultiUserAudioReceiveHandler(receiveCombined, receiveUser);
    }

    @Override
    public boolean registerAudioSendHandler(Guild guild, AudioSendHandler handler) {
        return audioManager.registerAudioSendHandler(guild, handler);
    }

    @Override
    public boolean registerAudioReceiveHandler(Guild guild, AudioReceiveHandler handler) {
        return audioManager.registerAudioReceiveHandler(guild, handler);
    }

    @Override
    public boolean unregisterAudioSendHandler(Guild guild) {
        return audioManager.unregisterAudioSendHandler(guild);
    }

    @Override
    public boolean unregisterAudioReceiveHandler(Guild guild) {
        return audioManager.unregisterAudioReceiveHandler(guild);
    }

    @Override
    public Optional<AudioSendHandler> getAudioSendHandler(Guild guild) {
        return audioManager.getAudioSendHandler(guild);
    }

    @Override
    public Optional<AudioReceiveHandler> getAudioReceiveHandler(Guild guild) {
        return audioManager.getAudioReceiveHandler(guild);
    }

    @Override
    public Optional<MixingAudioSendHandler> setupMixingAudio(Guild guild, SpeakingMode speakingMode) {
        // Create and register a mixing audio handler
        MixingAudioSendHandler handler = createMixingAudioSendHandler(speakingMode);

        if (registerAudioSendHandler(guild, handler)) {
            return Optional.of(handler);
        }

        return Optional.empty();
    }

    @Override
    public Optional<MultiUserAudioReceiveHandler> setupMultiUserAudio(Guild guild,
                                                                      Consumer<byte[]> combinedAudioHandler,
                                                                      Map<String, Consumer<byte[]>> userAudioHandlers) {
        // Create a multi-user audio handler
        boolean receiveCombined = combinedAudioHandler != null;
        boolean receiveUser = userAudioHandlers != null && !userAudioHandlers.isEmpty();

        MultiUserAudioReceiveHandler handler = createMultiUserAudioReceiveHandler(receiveCombined, receiveUser);

        // Configure the handler
        if (receiveCombined) {
            handler.setCombinedAudioHandler(combinedAudioHandler);
        }

        if (receiveUser) {
            userAudioHandlers.forEach(handler::addUserAudioHandler);
        }

        // Register the handler
        if (registerAudioReceiveHandler(guild, handler)) {
            return Optional.of(handler);
        }

        return Optional.empty();
    }

    @Override
    public Set<User> getSpeakingUsers(Guild guild) {
        Optional<AudioReceiveHandler> handlerOpt = getAudioReceiveHandler(guild);

        if (handlerOpt.isPresent() && handlerOpt.get() instanceof MultiUserAudioReceiveHandler multiHandler) {
            return multiHandler.getSpeakingUsers();
        }

        return Collections.emptySet();
    }

    @Override
    public boolean queueAudio(Guild guild, byte[] data) {
        Optional<AudioSendHandler> handlerOpt = getAudioSendHandler(guild);

        // If no handler exists or it's not our type, create a basic one
        if (handlerOpt.isEmpty() || !(handlerOpt.get() instanceof BasicAudioSendHandler)) {
            BasicAudioSendHandler handler = (BasicAudioSendHandler) createAudioSendHandler();
            if (!registerAudioSendHandler(guild, handler)) {
                return false;
            }

            handler.queueAudio(data);
            return true;
        } else {
            // Use existing handler
            ((BasicAudioSendHandler) handlerOpt.get()).queueAudio(data);
            return true;
        }
    }

    @Override
    public boolean queueAudioToSource(Guild guild, String sourceId, byte[] data) {
        Optional<AudioSendHandler> handlerOpt = getAudioSendHandler(guild);

        // If no handler exists or it's not a mixing handler, create one
        if (handlerOpt.isEmpty() || !(handlerOpt.get() instanceof MixingAudioSendHandler)) {
            MixingAudioSendHandler handler = createMixingAudioSendHandler();
            if (!registerAudioSendHandler(guild, handler)) {
                return false;
            }

            handler.addAudioSource(sourceId);
            return handler.queueAudio(sourceId, data);
        } else {
            // Use existing handler
            MixingAudioSendHandler mixingHandler = (MixingAudioSendHandler) handlerOpt.get();

            // Add source if it doesn't exist
            if (!mixingHandler.getAudioSources().contains(sourceId)) {
                mixingHandler.addAudioSource(sourceId);
            }

            return mixingHandler.queueAudio(sourceId, data);
        }
    }

    @Override
    public boolean queueAudioToSource(Guild guild, String sourceId, ByteBuffer buffer) {
        Optional<AudioSendHandler> handlerOpt = getAudioSendHandler(guild);

        // If no handler exists or it's not a mixing handler, create one
        if (handlerOpt.isEmpty() || !(handlerOpt.get() instanceof MixingAudioSendHandler)) {
            MixingAudioSendHandler handler = createMixingAudioSendHandler();
            if (!registerAudioSendHandler(guild, handler)) {
                return false;
            }

            handler.addAudioSource(sourceId);
            return handler.queueAudio(sourceId, buffer);
        } else {
            // Use existing handler
            MixingAudioSendHandler mixingHandler = (MixingAudioSendHandler) handlerOpt.get();

            // Add source if it doesn't exist
            if (!mixingHandler.getAudioSources().contains(sourceId)) {
                mixingHandler.addAudioSource(sourceId);
            }

            return mixingHandler.queueAudio(sourceId, buffer);
        }
    }
}
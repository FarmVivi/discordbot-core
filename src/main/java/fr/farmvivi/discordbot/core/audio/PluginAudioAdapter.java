package fr.farmvivi.discordbot.core.audio;

import fr.farmvivi.discordbot.core.api.audio.*;
import fr.farmvivi.discordbot.core.api.permissions.Permission;
import fr.farmvivi.discordbot.core.api.permissions.PermissionManager;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Consumer;

/**
 * Adapter for plugins to interact with audio functionality.
 * This class provides a simplified interface for plugins to use audio features.
 */
public class PluginAudioAdapter implements PluginAudioAPI {
    private static final Logger logger = LoggerFactory.getLogger(PluginAudioAdapter.class);

    private final AudioManager audioManager;
    private final AudioFactory audioFactory;
    private final PermissionManager permissionManager;
    private final String pluginId;
    private final Permission joinPermission;
    private final Permission leavePermission;
    private final Permission sendPermission;
    private final Permission receivePermission;

    /**
     * Creates a new PluginAudioAdapter.
     *
     * @param audioManager The audio manager
     * @param audioFactory The audio factory
     * @param permissionManager The permission manager
     * @param pluginId The plugin ID
     */
    public PluginAudioAdapter(AudioManager audioManager, AudioFactory audioFactory, 
                              PermissionManager permissionManager, String pluginId) {
        this.audioManager = audioManager;
        this.audioFactory = audioFactory;
        this.permissionManager = permissionManager;
        this.pluginId = pluginId;
        
        // Create permissions
        this.joinPermission = permissionManager.registerPermission(
                pluginId + ".audio.join", 
                "Allows joining voice channels",
                permissionManager.getDefaultAllow()
        );
        
        this.leavePermission = permissionManager.registerPermission(
                pluginId + ".audio.leave", 
                "Allows leaving voice channels",
                permissionManager.getDefaultAllow()
        );
        
        this.sendPermission = permissionManager.registerPermission(
                pluginId + ".audio.send", 
                "Allows sending audio to voice channels",
                permissionManager.getDefaultAllow()
        );
        
        this.receivePermission = permissionManager.registerPermission(
                pluginId + ".audio.receive", 
                "Allows receiving audio from voice channels",
                permissionManager.getDefaultAllow()
        );
    }

    @Override
    public boolean joinVoiceChannel(VoiceChannel voiceChannel) {
        if (!permissionManager.hasPermission(pluginId, joinPermission)) {
            logger.warn("Plugin {} does not have permission to join voice channels", pluginId);
            return false;
        }
        
        return audioManager.joinVoiceChannel(voiceChannel);
    }

    @Override
    public boolean leaveVoiceChannel(Guild guild) {
        if (!permissionManager.hasPermission(pluginId, leavePermission)) {
            logger.warn("Plugin {} does not have permission to leave voice channels", pluginId);
            return false;
        }
        
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
        if (!permissionManager.hasPermission(pluginId, sendPermission)) {
            logger.warn("Plugin {} does not have permission to send audio", pluginId);
            return false;
        }
        
        return audioManager.registerAudioSendHandler(guild, handler);
    }

    @Override
    public boolean registerAudioReceiveHandler(Guild guild, AudioReceiveHandler handler) {
        if (!permissionManager.hasPermission(pluginId, receivePermission)) {
            logger.warn("Plugin {} does not have permission to receive audio", pluginId);
            return false;
        }
        
        return audioManager.registerAudioReceiveHandler(guild, handler);
    }

    @Override
    public boolean unregisterAudioSendHandler(Guild guild) {
        if (!permissionManager.hasPermission(pluginId, sendPermission)) {
            logger.warn("Plugin {} does not have permission to send audio", pluginId);
            return false;
        }
        
        return audioManager.unregisterAudioSendHandler(guild);
    }

    @Override
    public boolean unregisterAudioReceiveHandler(Guild guild) {
        if (!permissionManager.hasPermission(pluginId, receivePermission)) {
            logger.warn("Plugin {} does not have permission to receive audio", pluginId);
            return false;
        }
        
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
        if (!permissionManager.hasPermission(pluginId, sendPermission)) {
            logger.warn("Plugin {} does not have permission to send audio", pluginId);
            return Optional.empty();
        }
        
        // Create and register a mixing audio handler
        MixingAudioSendHandler handler = createMixingAudioSendHandler(speakingMode);
        
        if (registerAudioSendHandler(guild, handler)) {
            return Optional.of(handler);
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<MultiUserAudioReceiveHandler> setupMultiUserAudio(Guild guild, Consumer<byte[]> combinedAudioHandler, 
                                                                     Map<String, Consumer<byte[]>> userAudioHandlers) {
        if (!permissionManager.hasPermission(pluginId, receivePermission)) {
            logger.warn("Plugin {} does not have permission to receive audio", pluginId);
            return Optional.empty();
        }
        
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
        if (!permissionManager.hasPermission(pluginId, sendPermission)) {
            logger.warn("Plugin {} does not have permission to send audio", pluginId);
            return false;
        }
        
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
        if (!permissionManager.hasPermission(pluginId, sendPermission)) {
            logger.warn("Plugin {} does not have permission to send audio", pluginId);
            return false;
        }
        
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
        if (!permissionManager.hasPermission(pluginId, sendPermission)) {
            logger.warn("Plugin {} does not have permission to send audio", pluginId);
            return false;
        }
        
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
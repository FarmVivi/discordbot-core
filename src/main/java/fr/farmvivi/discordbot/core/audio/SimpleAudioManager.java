package fr.farmvivi.discordbot.core.audio;

import fr.farmvivi.discordbot.core.api.audio.AudioManager;
import fr.farmvivi.discordbot.core.api.audio.events.*;
import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the AudioManager interface.
 * This class manages audio connections and handlers for voice channels.
 */
public class SimpleAudioManager implements fr.farmvivi.discordbot.core.api.audio.AudioManager {
    private static final Logger logger = LoggerFactory.getLogger(SimpleAudioManager.class);

    private final DiscordAPI discordAPI;
    private final EventManager eventManager;
    private final Map<String, AudioSendHandler> sendHandlers = new HashMap<>();
    private final Map<String, AudioReceiveHandler> receiveHandlers = new HashMap<>();

    /**
     * Creates a new SimpleAudioManager.
     *
     * @param discordAPI The Discord API
     * @param eventManager The event manager
     */
    public SimpleAudioManager(DiscordAPI discordAPI, EventManager eventManager) {
        this.discordAPI = discordAPI;
        this.eventManager = eventManager;
    }

    @Override
    public boolean joinVoiceChannel(VoiceChannel voiceChannel) {
        Guild guild = voiceChannel.getGuild();
        
        // Fire join event
        VoiceChannelJoinEvent joinEvent = new VoiceChannelJoinEvent(guild, voiceChannel);
        eventManager.callEvent(joinEvent);
        
        if (joinEvent.isCancelled()) {
            logger.debug("Voice channel join cancelled by event handler for channel {} in guild {}", 
                    voiceChannel.getName(), guild.getName());
            return false;
        }
        
        try {
            if (!discordAPI.isConnected()) {
                logger.warn("Cannot join voice channel: not connected to Discord");
                return false;
            }
            
            AudioManager audioManager = guild.getAudioManager();
            
            // If already connected to this channel, just return true
            if (audioManager.isConnected() && audioManager.getConnectedChannel() != null && 
                    audioManager.getConnectedChannel().getIdLong() == voiceChannel.getIdLong()) {
                return true;
            }

            // Disconnect from current channel if connected
            if (audioManager.isConnected()) {
                leaveVoiceChannel(guild);
            }
            
            // Connect to the new channel
            audioManager.openAudioConnection(voiceChannel);
            
            // Fire joined event
            VoiceChannelJoinedEvent joinedEvent = new VoiceChannelJoinedEvent(guild, voiceChannel);
            eventManager.callEvent(joinedEvent);
            
            logger.info("Joined voice channel {} in guild {}", voiceChannel.getName(), guild.getName());
            return true;
        } catch (Exception e) {
            logger.error("Failed to join voice channel {} in guild {}", 
                    voiceChannel.getName(), guild.getName(), e);
            return false;
        }
    }

    @Override
    public boolean leaveVoiceChannel(Guild guild) {
        AudioManager audioManager = guild.getAudioManager();
        
        // If not connected, nothing to do
        if (!audioManager.isConnected()) {
            return true;
        }
        
        VoiceChannel voiceChannel = (VoiceChannel) audioManager.getConnectedChannel();
        if (voiceChannel == null) {
            audioManager.closeAudioConnection();
            return true;
        }
        
        // Fire leave event
        VoiceChannelLeaveEvent leaveEvent = new VoiceChannelLeaveEvent(guild, voiceChannel);
        eventManager.callEvent(leaveEvent);
        
        if (leaveEvent.isCancelled()) {
            logger.debug("Voice channel leave cancelled by event handler for channel {} in guild {}", 
                    voiceChannel.getName(), guild.getName());
            return false;
        }
        
        try {
            // Unregister handlers
            audioManager.setSendingHandler(null);
            audioManager.setReceivingHandler(null);
            
            // Remove from maps
            sendHandlers.remove(guild.getId());
            receiveHandlers.remove(guild.getId());
            
            // Disconnect
            audioManager.closeAudioConnection();
            
            // Fire left event
            VoiceChannelLeftEvent leftEvent = new VoiceChannelLeftEvent(guild, voiceChannel);
            eventManager.callEvent(leftEvent);
            
            logger.info("Left voice channel {} in guild {}", voiceChannel.getName(), guild.getName());
            return true;
        } catch (Exception e) {
            logger.error("Failed to leave voice channel {} in guild {}", 
                    voiceChannel.getName(), guild.getName(), e);
            return false;
        }
    }

    @Override
    public boolean isConnected(Guild guild) {
        if (!discordAPI.isConnected()) {
            return false;
        }
        
        AudioManager audioManager = guild.getAudioManager();
        return audioManager.isConnected();
    }

    @Override
    public Optional<VoiceChannel> getConnectedChannel(Guild guild) {
        if (!discordAPI.isConnected() || !isConnected(guild)) {
            return Optional.empty();
        }
        
        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.getConnectedChannel() instanceof VoiceChannel voiceChannel) {
            return Optional.of(voiceChannel);
        }
        
        return Optional.empty();
    }

    @Override
    public boolean registerAudioSendHandler(Guild guild, AudioSendHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Audio send handler cannot be null");
        }
        
        // Fire register event
        AudioSendHandlerRegisterEvent registerEvent = new AudioSendHandlerRegisterEvent(guild, handler);
        eventManager.callEvent(registerEvent);
        
        if (registerEvent.isCancelled()) {
            logger.debug("Audio send handler registration cancelled by event handler in guild {}", 
                    guild.getName());
            return false;
        }
        
        try {
            if (!discordAPI.isConnected()) {
                logger.warn("Cannot register audio send handler: not connected to Discord");
                return false;
            }
            
            AudioManager audioManager = guild.getAudioManager();
            audioManager.setSendingHandler(handler);
            sendHandlers.put(guild.getId(), handler);
            
            logger.debug("Registered audio send handler for guild {}", guild.getName());
            return true;
        } catch (Exception e) {
            logger.error("Failed to register audio send handler for guild {}", guild.getName(), e);
            return false;
        }
    }

    @Override
    public boolean registerAudioReceiveHandler(Guild guild, AudioReceiveHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Audio receive handler cannot be null");
        }
        
        // Fire register event
        AudioReceiveHandlerRegisterEvent registerEvent = new AudioReceiveHandlerRegisterEvent(guild, handler);
        eventManager.callEvent(registerEvent);
        
        if (registerEvent.isCancelled()) {
            logger.debug("Audio receive handler registration cancelled by event handler in guild {}", 
                    guild.getName());
            return false;
        }
        
        try {
            if (!discordAPI.isConnected()) {
                logger.warn("Cannot register audio receive handler: not connected to Discord");
                return false;
            }
            
            AudioManager audioManager = guild.getAudioManager();
            audioManager.setReceivingHandler(handler);
            receiveHandlers.put(guild.getId(), handler);
            
            logger.debug("Registered audio receive handler for guild {}", guild.getName());
            return true;
        } catch (Exception e) {
            logger.error("Failed to register audio receive handler for guild {}", guild.getName(), e);
            return false;
        }
    }

    @Override
    public boolean unregisterAudioSendHandler(Guild guild) {
        AudioSendHandler currentHandler = sendHandlers.get(guild.getId());
        if (currentHandler == null) {
            // No handler to unregister
            return true;
        }
        
        // Fire unregister event
        AudioSendHandlerUnregisterEvent unregisterEvent = new AudioSendHandlerUnregisterEvent(guild, currentHandler);
        eventManager.callEvent(unregisterEvent);
        
        if (unregisterEvent.isCancelled()) {
            logger.debug("Audio send handler unregistration cancelled by event handler in guild {}", 
                    guild.getName());
            return false;
        }
        
        try {
            if (!discordAPI.isConnected()) {
                // Just remove from our map if not connected
                sendHandlers.remove(guild.getId());
                return true;
            }
            
            AudioManager audioManager = guild.getAudioManager();
            audioManager.setSendingHandler(null);
            sendHandlers.remove(guild.getId());
            
            logger.debug("Unregistered audio send handler for guild {}", guild.getName());
            return true;
        } catch (Exception e) {
            logger.error("Failed to unregister audio send handler for guild {}", guild.getName(), e);
            return false;
        }
    }

    @Override
    public boolean unregisterAudioReceiveHandler(Guild guild) {
        AudioReceiveHandler currentHandler = receiveHandlers.get(guild.getId());
        if (currentHandler == null) {
            // No handler to unregister
            return true;
        }
        
        // Fire unregister event
        AudioReceiveHandlerUnregisterEvent unregisterEvent = new AudioReceiveHandlerUnregisterEvent(guild, currentHandler);
        eventManager.callEvent(unregisterEvent);
        
        if (unregisterEvent.isCancelled()) {
            logger.debug("Audio receive handler unregistration cancelled by event handler in guild {}", 
                    guild.getName());
            return false;
        }
        
        try {
            if (!discordAPI.isConnected()) {
                // Just remove from our map if not connected
                receiveHandlers.remove(guild.getId());
                return true;
            }
            
            AudioManager audioManager = guild.getAudioManager();
            audioManager.setReceivingHandler(null);
            receiveHandlers.remove(guild.getId());
            
            logger.debug("Unregistered audio receive handler for guild {}", guild.getName());
            return true;
        } catch (Exception e) {
            logger.error("Failed to unregister audio receive handler for guild {}", guild.getName(), e);
            return false;
        }
    }

    @Override
    public Optional<AudioSendHandler> getAudioSendHandler(Guild guild) {
        return Optional.ofNullable(sendHandlers.get(guild.getId()));
    }

    @Override
    public Optional<AudioReceiveHandler> getAudioReceiveHandler(Guild guild) {
        return Optional.ofNullable(receiveHandlers.get(guild.getId()));
    }

    @Override
    public void close() throws IOException {
        // Leave all voice channels
        if (discordAPI.isConnected()) {
            discordAPI.getJDA().getGuilds().forEach(this::leaveVoiceChannel);
        }
        
        // Clear all handlers
        sendHandlers.clear();
        receiveHandlers.clear();
        
        logger.info("Audio manager closed");
    }
}
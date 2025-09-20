package fr.farmvivi.fluxcord.plugins.music;

import fr.farmvivi.fluxcord.plugins.music.service.AudioPlayerService;
import fr.farmvivi.fluxcord.plugins.music.service.VoiceChannelService;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main manager class that coordinates all music-related services.
 * Acts as a facade for the various music services.
 */
public class MusicManager {
    private static final Logger logger = LoggerFactory.getLogger(MusicManager.class);

    private final MusicPlugin plugin;
    private final AudioPlayerService audioPlayerService;
    private final VoiceChannelService voiceChannelService;

    public MusicManager(MusicPlugin plugin) {
        this.plugin = plugin;
        
        logger.info("Initializing music services...");
        
        // Initialize services
        this.audioPlayerService = new AudioPlayerService(plugin);
        this.voiceChannelService = new VoiceChannelService(plugin, audioPlayerService);
        
        logger.info("Music services initialized successfully");
    }

    public void handleVoiceUpdate(GuildVoiceUpdateEvent event) {
        voiceChannelService.handleVoiceUpdate(event);
    }

    public void shutdown() {
        logger.info("Shutting down music manager...");
        
        if (voiceChannelService != null) {
            voiceChannelService.shutdown();
        }
        
        if (audioPlayerService != null) {
            audioPlayerService.shutdown();
        }
        
        logger.info("Music manager shutdown complete");
    }
    
    // Getters for services
    public AudioPlayerService getAudioPlayerService() {
        return audioPlayerService;
    }
    
    public VoiceChannelService getVoiceChannelService() {
        return voiceChannelService;
    }
}
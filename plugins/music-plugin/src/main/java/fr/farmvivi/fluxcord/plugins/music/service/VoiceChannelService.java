package fr.farmvivi.fluxcord.plugins.music.service;

import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.model.GuildMusicPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing voice channel connections and auto-leave functionality.
 */
public class VoiceChannelService {
    private static final Logger logger = LoggerFactory.getLogger(VoiceChannelService.class);

    private final MusicPlugin plugin;
    private final AudioPlayerService audioPlayerService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> autoLeaveTasks = new ConcurrentHashMap<>();
    
    public VoiceChannelService(MusicPlugin plugin, AudioPlayerService audioPlayerService) {
        this.plugin = plugin;
        this.audioPlayerService = audioPlayerService;
    }
    
    /**
     * Connects the bot to a voice channel and sets up audio sending.
     */
    public boolean connectToChannel(VoiceChannel channel) {
        try {
            Guild guild = channel.getGuild();
            AudioManager audioManager = guild.getAudioManager();
            
            // Check if already connected to the same channel
            if (audioManager.isConnected() && audioManager.getConnectedChannel() != null) {
                if (audioManager.getConnectedChannel().getIdLong() == channel.getIdLong()) {
                    return true; // Already connected to this channel
                }
            }
            
            // Get or create the music player for this guild
            GuildMusicPlayer musicPlayer = audioPlayerService.getGuildPlayer(guild);
            
            // Connect to the voice channel
            audioManager.openAudioConnection(channel);
            audioManager.setSendingHandler(new AudioPlayerSendHandler(musicPlayer.getAudioPlayer()));
            
            // Cancel any pending auto-leave task
            cancelAutoLeave(guild);
            
            logger.info("Connected to voice channel: {} in guild: {}", channel.getName(), guild.getName());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to connect to voice channel: {} in guild: {}", 
                channel.getName(), channel.getGuild().getName(), e);
            return false;
        }
    }
    
    /**
     * Disconnects the bot from voice channel and cleans up.
     */
    public void disconnectFromChannel(Guild guild) {
        try {
            AudioManager audioManager = guild.getAudioManager();
            
            if (audioManager.isConnected()) {
                audioManager.closeAudioConnection();
                logger.info("Disconnected from voice channel in guild: {}", guild.getName());
            }
            
            // Cancel auto-leave task
            cancelAutoLeave(guild);
            
            // Stop and cleanup the music player
            audioPlayerService.removeGuildPlayer(guild);
            
        } catch (Exception e) {
            logger.error("Error disconnecting from voice channel in guild: {}", guild.getName(), e);
        }
    }
    
    /**
     * Handles voice state updates for auto-leave functionality.
     */
    public void handleVoiceUpdate(GuildVoiceUpdateEvent event) {
        Guild guild = event.getGuild();
        Member botMember = guild.getSelfMember();
        
        // Check if bot is in a voice channel
        VoiceChannel botChannel = (VoiceChannel) botMember.getVoiceState().getChannel();
        if (botChannel == null) {
            return; // Bot is not in any voice channel
        }
        
        // Check if auto-leave is enabled
        if (!plugin.getConfiguration().getBoolean("voice.auto_leave", true)) {
            return;
        }
        
        // Count non-bot members in the voice channel
        long humanMembers = botChannel.getMembers().stream()
            .filter(member -> !member.getUser().isBot())
            .count();
        
        if (humanMembers == 0) {
            // Schedule auto-leave if no humans are left
            scheduleAutoLeave(guild);
        } else {
            // Cancel auto-leave if humans joined back
            cancelAutoLeave(guild);
        }
    }
    
    /**
     * Schedules an auto-leave task for the specified guild.
     */
    private void scheduleAutoLeave(Guild guild) {
        // Cancel any existing auto-leave task
        cancelAutoLeave(guild);
        
        int timeoutSeconds = plugin.getConfiguration().getInt("voice.auto_leave_timeout", 300);
        
        ScheduledFuture<?> task = scheduler.schedule(() -> {
            try {
                // Double-check that the channel is still empty
                Member botMember = guild.getSelfMember();
                VoiceChannel botChannel = (VoiceChannel) botMember.getVoiceState().getChannel();
                
                if (botChannel != null) {
                    long humanMembers = botChannel.getMembers().stream()
                        .filter(member -> !member.getUser().isBot())
                        .count();
                    
                    if (humanMembers == 0) {
                        logger.info("Auto-leaving voice channel in guild: {} (timeout: {}s)", 
                            guild.getName(), timeoutSeconds);
                        disconnectFromChannel(guild);
                    }
                }
            } catch (Exception e) {
                logger.error("Error during auto-leave in guild: {}", guild.getName(), e);
            } finally {
                autoLeaveTasks.remove(guild.getIdLong());
            }
        }, timeoutSeconds, TimeUnit.SECONDS);
        
        autoLeaveTasks.put(guild.getIdLong(), task);
        logger.debug("Scheduled auto-leave for guild: {} in {}s", guild.getName(), timeoutSeconds);
    }
    
    /**
     * Cancels the auto-leave task for the specified guild.
     */
    private void cancelAutoLeave(Guild guild) {
        ScheduledFuture<?> task = autoLeaveTasks.remove(guild.getIdLong());
        if (task != null && !task.isDone()) {
            task.cancel(false);
            logger.debug("Cancelled auto-leave for guild: {}", guild.getName());
        }
    }
    
    /**
     * Checks if the bot can connect to the specified voice channel.
     */
    public boolean canConnectToChannel(VoiceChannel channel) {
        return channel.getGuild().getSelfMember().hasPermission(channel, 
            net.dv8tion.jda.api.Permission.VOICE_CONNECT, 
            net.dv8tion.jda.api.Permission.VOICE_SPEAK);
    }
    
    /**
     * Shutdown the service and cleanup resources.
     */
    public void shutdown() {
        logger.info("Shutting down voice channel service...");
        
        // Cancel all auto-leave tasks
        autoLeaveTasks.values().forEach(task -> task.cancel(false));
        autoLeaveTasks.clear();
        
        // Shutdown scheduler
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("Voice channel service shutdown complete");
    }
}
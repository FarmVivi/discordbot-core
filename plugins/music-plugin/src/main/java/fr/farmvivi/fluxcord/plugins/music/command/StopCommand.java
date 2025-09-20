package fr.farmvivi.fluxcord.plugins.music.command;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.model.GuildMusicPlayer;
import fr.farmvivi.fluxcord.plugins.music.service.AudioPlayerService;
import fr.farmvivi.fluxcord.plugins.music.service.VoiceChannelService;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Command to stop music playback and clear the queue.
 */
public class StopCommand {
    
    private final MusicPlugin plugin;
    private final AudioPlayerService audioPlayerService;
    private final VoiceChannelService voiceChannelService;
    
    public StopCommand(MusicPlugin plugin, AudioPlayerService audioPlayerService, VoiceChannelService voiceChannelService) {
        this.plugin = plugin;
        this.audioPlayerService = audioPlayerService;
        this.voiceChannelService = voiceChannelService;
    }
    
    public CommandResult execute(CommandContext context) {
        // Check permission
        if (!plugin.getPluginPermissionManager().hasPermission(
                context.getUser().getId(), "musicplugin.admin")) {
            context.reply("❌ You don't have permission to use this command!");
            return CommandResult.error("No permission");
        }
        
        // Validate that we're in a guild
        if (context.getGuild().isEmpty()) {
            context.reply("❌ This command can only be used in a server!");
            return CommandResult.error("Not in guild");
        }
        
        Guild guild = context.getGuild().get();
        GuildMusicPlayer musicPlayer = audioPlayerService.getGuildPlayer(guild);
        
        if (musicPlayer.getCurrentTrack() == null && musicPlayer.getQueueSize() == 0) {
            context.reply("❌ No music is currently playing and queue is empty!");
            return CommandResult.error("Nothing to stop");
        }
        
        // Stop playback and clear queue
        musicPlayer.stop();
        
        // Check if we should disconnect from voice channel
        boolean autoLeave = plugin.getConfiguration().getBoolean("voice.auto_leave", true);
        if (autoLeave) {
            voiceChannelService.disconnectFromChannel(guild);
            context.reply("⏹️ Music stopped, queue cleared, and left voice channel!");
        } else {
            context.reply("⏹️ Music stopped and queue cleared!");
        }
        
        return CommandResult.success();
    }
}
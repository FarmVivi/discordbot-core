package fr.farmvivi.fluxcord.plugins.music.command;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.model.GuildMusicPlayer;
import fr.farmvivi.fluxcord.plugins.music.service.AudioPlayerService;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Command to clear the music queue.
 */
public class ClearCommand {
    
    private final MusicPlugin plugin;
    private final AudioPlayerService audioPlayerService;
    
    public ClearCommand(MusicPlugin plugin, AudioPlayerService audioPlayerService) {
        this.plugin = plugin;
        this.audioPlayerService = audioPlayerService;
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
        
        int queueSize = musicPlayer.getQueueSize();
        
        if (queueSize == 0) {
            context.reply("❌ The queue is already empty!");
            return CommandResult.error("Queue already empty");
        }
        
        // Clear the queue
        musicPlayer.clearQueue();
        
        context.reply("🗑️ Cleared " + queueSize + " songs from the queue!");
        return CommandResult.success();
    }
}
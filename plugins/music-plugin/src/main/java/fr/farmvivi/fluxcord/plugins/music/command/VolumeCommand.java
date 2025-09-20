package fr.farmvivi.fluxcord.plugins.music.command;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.model.GuildMusicPlayer;
import fr.farmvivi.fluxcord.plugins.music.service.AudioPlayerService;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Command to control playback volume.
 */
public class VolumeCommand {
    
    private final MusicPlugin plugin;
    private final AudioPlayerService audioPlayerService;
    
    public VolumeCommand(MusicPlugin plugin, AudioPlayerService audioPlayerService) {
        this.plugin = plugin;
        this.audioPlayerService = audioPlayerService;
    }
    
    public CommandResult execute(CommandContext context) {
        // Check permission
        if (!plugin.getPluginPermissionManager().hasPermission(
                context.getUser().getId(), "musicplugin.volume")) {
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
        
        // Check if volume parameter is provided
        if (context.hasOption("volume")) {
            // Set volume
            int volume = context.getOption("volume", 50);
            
            if (volume < 0 || volume > 100) {
                context.reply("❌ Volume must be between 0 and 100!");
                return CommandResult.error("Invalid volume");
            }
            
            try {
                musicPlayer.setVolume(volume);
                context.reply("🔊 Volume set to **" + volume + "%**");
                return CommandResult.success();
            } catch (Exception e) {
                context.reply("❌ Failed to set volume: " + e.getMessage());
                return CommandResult.error("Failed to set volume");
            }
        } else {
            // Display current volume
            int currentVolume = musicPlayer.getVolume();
            String volumeIcon = getVolumeIcon(currentVolume);
            context.reply(volumeIcon + " Current volume: **" + currentVolume + "%**");
            return CommandResult.success();
        }
    }
    
    private String getVolumeIcon(int volume) {
        if (volume == 0) return "🔇";
        if (volume <= 30) return "🔉";
        if (volume <= 70) return "🔊";
        return "📢";
    }
}
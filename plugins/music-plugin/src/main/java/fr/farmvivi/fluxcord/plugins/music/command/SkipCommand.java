package fr.farmvivi.fluxcord.plugins.music.command;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.model.GuildMusicPlayer;
import fr.farmvivi.fluxcord.plugins.music.service.AudioPlayerService;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Command to skip the current track.
 */
public class SkipCommand {
    
    private final MusicPlugin plugin;
    private final AudioPlayerService audioPlayerService;
    
    public SkipCommand(MusicPlugin plugin, AudioPlayerService audioPlayerService) {
        this.plugin = plugin;
        this.audioPlayerService = audioPlayerService;
    }
    
    public CommandResult execute(CommandContext context) {
        // Check permission
        if (!plugin.getPluginPermissionManager().hasPermission(
                context.getUser().getId(), "musicplugin.skip")) {
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
        
        if (musicPlayer.getCurrentTrack() == null) {
            context.reply("❌ No music is currently playing!");
            return CommandResult.error("No music playing");
        }
        
        String currentTitle = musicPlayer.getCurrentTrack().getInfo().title;
        
        if (musicPlayer.skipTrack()) {
            context.reply("⏭️ Skipped: **" + currentTitle + "**");
            return CommandResult.success();
        } else {
            context.reply("❌ Failed to skip the track!");
            return CommandResult.error("Skip failed");
        }
    }
}
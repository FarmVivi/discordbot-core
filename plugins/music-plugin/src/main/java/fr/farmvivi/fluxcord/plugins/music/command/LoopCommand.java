package fr.farmvivi.fluxcord.plugins.music.command;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.model.GuildMusicPlayer;
import fr.farmvivi.fluxcord.plugins.music.service.AudioPlayerService;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Command to control loop modes (track, queue, off).
 */
public class LoopCommand {
    
    private final MusicPlugin plugin;
    private final AudioPlayerService audioPlayerService;
    
    public LoopCommand(MusicPlugin plugin, AudioPlayerService audioPlayerService) {
        this.plugin = plugin;
        this.audioPlayerService = audioPlayerService;
    }
    
    public CommandResult execute(CommandContext context) {
        // Check permission
        if (!plugin.getPluginPermissionManager().hasPermission(
                context.getUser().getId(), "musicplugin.queue")) {
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
        
        // Get loop mode parameter or toggle current mode
        String mode = context.getOption("mode", "");
        
        if (mode.isEmpty()) {
            // Toggle through modes: off -> track -> queue -> off
            if (!musicPlayer.isLoopTrack() && !musicPlayer.isLoopQueue()) {
                // Currently off, enable track loop
                musicPlayer.setLoopTrack(true);
                musicPlayer.setLoopQueue(false);
                context.reply("🔂 Loop mode: **Track** (current song will repeat)");
            } else if (musicPlayer.isLoopTrack() && !musicPlayer.isLoopQueue()) {
                // Currently track loop, enable queue loop
                musicPlayer.setLoopTrack(false);
                musicPlayer.setLoopQueue(true);
                context.reply("🔁 Loop mode: **Queue** (entire queue will repeat)");
            } else {
                // Currently queue loop, disable all looping
                musicPlayer.setLoopTrack(false);
                musicPlayer.setLoopQueue(false);
                context.reply("▶️ Loop mode: **Off** (normal playback)");
            }
        } else {
            // Set specific mode
            switch (mode.toLowerCase()) {
                case "off", "none", "disable" -> {
                    musicPlayer.setLoopTrack(false);
                    musicPlayer.setLoopQueue(false);
                    context.reply("▶️ Loop mode: **Off** (normal playback)");
                }
                case "track", "song", "current" -> {
                    musicPlayer.setLoopTrack(true);
                    musicPlayer.setLoopQueue(false);
                    context.reply("🔂 Loop mode: **Track** (current song will repeat)");
                }
                case "queue", "all", "playlist" -> {
                    musicPlayer.setLoopTrack(false);
                    musicPlayer.setLoopQueue(true);
                    context.reply("🔁 Loop mode: **Queue** (entire queue will repeat)");
                }
                default -> {
                    context.reply("❌ Invalid loop mode! Use: `off`, `track`, or `queue`");
                    return CommandResult.error("Invalid mode");
                }
            }
        }
        
        return CommandResult.success();
    }
    
    public CommandResult executeToggle(CommandContext context) {
        // For compatibility with legacy toggle behavior
        return execute(context);
    }
}
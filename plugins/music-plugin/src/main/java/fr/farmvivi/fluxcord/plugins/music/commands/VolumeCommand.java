package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.MusicPlayer;

/**
 * Command to adjust playback volume.
 */
public class VolumeCommand {
    private final MusicPlugin plugin;
    
    public VolumeCommand(MusicPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void execute(CommandContext ctx, Integer level) {
        MusicPlayer player = plugin.getMusicManager().getPlayer(ctx.getGuild());
        
        // Check permission
        if (!plugin.getPluginPermissionManager().hasPermission(ctx.getUser(), "music.volume")) {
            ctx.replyError(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.error.no_permission"
            ));
            return;
        }
        
        if (level == null) {
            // Show current volume
            ctx.replyInfo(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.volume.current", player.getVolume()
            ));
        } else {
            // Set new volume
            player.setVolume(level);
            ctx.replySuccess(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.volume.set", level
            ));
        }
    }
}
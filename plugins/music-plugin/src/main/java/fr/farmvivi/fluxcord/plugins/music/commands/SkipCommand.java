package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.MusicPlayer;

/**
 * Command to skip the current track.
 */
public class SkipCommand {
    private final MusicPlugin plugin;
    
    public SkipCommand(MusicPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void execute(CommandContext ctx) {
        MusicPlayer player = plugin.getMusicManager().getPlayer(ctx.getGuild());
        
        if (player.getPlayingTrack() == null) {
            ctx.replyError(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.error.nothing_playing"
            ));
            return;
        }
        
        // Check permission
        if (!plugin.getPluginPermissionManager().hasPermission(ctx.getUser(), "music.skip")) {
            ctx.replyError(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.error.no_permission"
            ));
            return;
        }
        
        String skippedTitle = player.getPlayingTrack().getInfo().title;
        player.skipTrack();
        
        ctx.replySuccess(plugin.getPluginLanguageAdapter().getString(
            ctx.getGuild(), "music.skipped", skippedTitle
        ));
    }
}
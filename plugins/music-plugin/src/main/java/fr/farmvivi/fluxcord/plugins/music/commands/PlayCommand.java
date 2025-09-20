package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;

/**
 * Command to play music from a URL or search query.
 */
public class PlayCommand {
    private final MusicPlugin plugin;
    
    public PlayCommand(MusicPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void execute(CommandContext ctx, String query, boolean playNow) {
        // Check if user is in a voice channel
        if (ctx.getMember().getVoiceState().getChannel() == null) {
            ctx.replyError(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.error.not_in_voice"
            ));
            return;
        }
        
        // Check permission
        if (!plugin.getPluginPermissionManager().hasPermission(ctx.getUser(), "music.play")) {
            ctx.replyError(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.error.no_permission"
            ));
            return;
        }
        
        // Load and play the track
        plugin.getMusicManager().loadTrack(ctx, query, playNow);
    }
}
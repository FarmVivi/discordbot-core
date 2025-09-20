package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.MusicPlayer;

/**
 * Command to clear the music queue.
 */
public class ClearCommand {
    private final MusicPlugin plugin;
    
    public ClearCommand(MusicPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void execute(CommandContext ctx) {
        MusicPlayer player = plugin.getMusicManager().getPlayer(ctx.getGuild());
        
        if (player.getTrackScheduler().getQueueSize() == 0) {
            ctx.replyError(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.error.queue_empty"
            ));
            return;
        }
        
        int cleared = player.getTrackScheduler().getQueueSize();
        player.getTrackScheduler().clear();
        
        ctx.replySuccess(plugin.getPluginLanguageAdapter().getString(
            ctx.getGuild(), "music.queue.cleared", cleared
        ));
        
        player.getPlayerMessage().refresh();
    }
}
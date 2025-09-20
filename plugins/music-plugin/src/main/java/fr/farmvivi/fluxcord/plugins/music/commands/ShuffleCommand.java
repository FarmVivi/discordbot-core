package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.MusicPlayer;

/**
 * Command to toggle shuffle mode.
 */
public class ShuffleCommand {
    private final MusicPlugin plugin;
    
    public ShuffleCommand(MusicPlugin plugin) {
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
        
        boolean shuffled = !player.getTrackScheduler().isShuffleMode();
        player.getTrackScheduler().setShuffleMode(shuffled);
        
        if (shuffled) {
            ctx.replySuccess(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.shuffle.enabled"
            ));
        } else {
            ctx.replySuccess(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.shuffle.disabled"
            ));
        }
        
        player.getPlayerMessage().refresh();
    }
}
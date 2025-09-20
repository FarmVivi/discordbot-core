package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.MusicPlayer;

/**
 * Command to pause or resume playback.
 */
public class PauseCommand {
    private final MusicPlugin plugin;
    
    public PauseCommand(MusicPlugin plugin) {
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
        
        boolean paused = !player.isPaused();
        player.setPaused(paused);
        
        if (paused) {
            ctx.replySuccess(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.paused"
            ));
        } else {
            ctx.replySuccess(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.resumed"
            ));
        }
    }
}
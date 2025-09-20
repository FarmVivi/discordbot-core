package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.MusicPlayer;

/**
 * Command to stop playback and clear the queue.
 */
public class StopCommand {
    private final MusicPlugin plugin;
    
    public StopCommand(MusicPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void execute(CommandContext ctx) {
        MusicPlayer player = plugin.getMusicManager().getPlayer(ctx.getGuild());
        
        if (player.getPlayingTrack() == null && player.getTrackScheduler().getQueueSize() == 0) {
            ctx.replyError(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.error.nothing_playing"
            ));
            return;
        }
        
        player.stop();
        ctx.getGuild().getAudioManager().closeAudioConnection();
        
        ctx.replySuccess(plugin.getPluginLanguageAdapter().getString(
            ctx.getGuild(), "music.stopped"
        ));
    }
}
package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.MusicPlayer;
import fr.farmvivi.fluxcord.plugins.music.player.TrackScheduler;

/**
 * Command to control loop modes.
 */
public class LoopCommand {
    private final MusicPlugin plugin;
    
    public LoopCommand(MusicPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void execute(CommandContext ctx, String mode) {
        MusicPlayer player = plugin.getMusicManager().getPlayer(ctx.getGuild());
        TrackScheduler scheduler = player.getTrackScheduler();
        
        switch (mode.toLowerCase()) {
            case "off":
                scheduler.setLoopMode(false);
                scheduler.setLoopQueueMode(false);
                ctx.replySuccess(plugin.getPluginLanguageAdapter().getString(
                    ctx.getGuild(), "music.loop.disabled"
                ));
                break;
                
            case "track":
                scheduler.setLoopMode(true);
                scheduler.setLoopQueueMode(false);
                ctx.replySuccess(plugin.getPluginLanguageAdapter().getString(
                    ctx.getGuild(), "music.loop.track"
                ));
                break;
                
            case "queue":
                scheduler.setLoopMode(false);
                scheduler.setLoopQueueMode(true);
                ctx.replySuccess(plugin.getPluginLanguageAdapter().getString(
                    ctx.getGuild(), "music.loop.queue"
                ));
                break;
                
            case "toggle":
            default:
                if (scheduler.isLoopMode()) {
                    scheduler.setLoopMode(false);
                    scheduler.setLoopQueueMode(true);
                    ctx.replySuccess(plugin.getPluginLanguageAdapter().getString(
                        ctx.getGuild(), "music.loop.queue"
                    ));
                } else if (scheduler.isLoopQueueMode()) {
                    scheduler.setLoopMode(false);
                    scheduler.setLoopQueueMode(false);
                    ctx.replySuccess(plugin.getPluginLanguageAdapter().getString(
                        ctx.getGuild(), "music.loop.disabled"
                    ));
                } else {
                    scheduler.setLoopMode(true);
                    scheduler.setLoopQueueMode(false);
                    ctx.replySuccess(plugin.getPluginLanguageAdapter().getString(
                        ctx.getGuild(), "music.loop.track"
                    ));
                }
                break;
        }
        
        player.getPlayerMessage().refresh();
    }
}
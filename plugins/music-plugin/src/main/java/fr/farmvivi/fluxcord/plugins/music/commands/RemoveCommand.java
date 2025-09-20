package fr.farmvivi.fluxcord.plugins.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.MusicPlayer;

import java.util.List;

/**
 * Command to remove a track from the queue.
 */
public class RemoveCommand {
    private final MusicPlugin plugin;
    
    public RemoveCommand(MusicPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void execute(CommandContext ctx, int position) {
        MusicPlayer player = plugin.getMusicManager().getPlayer(ctx.getGuild());
        List<AudioTrack> queue = player.getTrackScheduler().getQueue();
        
        if (queue.isEmpty()) {
            ctx.replyError(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.error.queue_empty"
            ));
            return;
        }
        
        if (position < 1 || position > queue.size()) {
            ctx.replyError(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.error.invalid_position", 1, queue.size()
            ));
            return;
        }
        
        AudioTrack removed = queue.get(position - 1);
        if (player.getTrackScheduler().removeTrack(position - 1)) {
            ctx.replySuccess(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.queue.removed", removed.getInfo().title
            ));
            player.getPlayerMessage().refresh();
        } else {
            ctx.replyError(plugin.getPluginLanguageAdapter().getString(
                ctx.getGuild(), "music.error.remove_failed"
            ));
        }
    }
}
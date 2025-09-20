package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;

import java.util.function.BiFunction;
import fr.farmvivi.fluxcord.api.command.Command;

/**
 * Clear command to clear the music queue.
 */
public class ClearCommand implements BiFunction<CommandContext, Command, CommandResult> {
    private final MusicPlugin plugin;
    
    public ClearCommand(MusicPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public CommandResult apply(CommandContext context, Command command) {
        // Check if user is in a guild
        if (!context.isFromGuild()) {
            context.replyError("This command can only be used in a server.");
            return CommandResult.error("Not in guild");
        }
        
        Guild guild = context.getGuild().get();
        GuildMusicManager guildManager = plugin.getGuildMusicManager(guild);
        
        // Check if queue is empty
        if (guildManager.getQueue().isEmpty()) {
            context.replyError("The queue is already empty.");
            return CommandResult.error("Queue empty");
        }
        
        int queueSize = guildManager.getQueue().size();
        guildManager.clearQueue();
        
        context.replySuccess("🗑️ Cleared **" + queueSize + "** track(s) from the queue.");
        return CommandResult.success();
    }
}
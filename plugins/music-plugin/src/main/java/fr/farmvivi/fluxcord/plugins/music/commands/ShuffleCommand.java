package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;

import java.util.function.BiFunction;
import fr.farmvivi.fluxcord.api.command.Command;

/**
 * Shuffle command to toggle shuffle mode.
 */
public class ShuffleCommand implements BiFunction<CommandContext, Command, CommandResult> {
    private final MusicPlugin plugin;
    
    public ShuffleCommand(MusicPlugin plugin) {
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
        
        // Check if queue has tracks to shuffle
        if (guildManager.getQueue().isEmpty()) {
            context.replyError("The queue is empty. Add some tracks before shuffling.");
            return CommandResult.error("Queue empty");
        }
        
        boolean shuffleEnabled = guildManager.toggleShuffle();
        
        if (shuffleEnabled) {
            context.replySuccess("🔀 Shuffle mode **enabled**. Queue has been shuffled.");
        } else {
            context.replySuccess("🔀 Shuffle mode **disabled**.");
        }
        
        return CommandResult.success();
    }
}
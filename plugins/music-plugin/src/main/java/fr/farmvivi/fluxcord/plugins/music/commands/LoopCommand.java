package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;

import java.util.function.BiFunction;
import fr.farmvivi.fluxcord.api.command.Command;

/**
 * Loop command to toggle loop mode.
 */
public class LoopCommand implements BiFunction<CommandContext, Command, CommandResult> {
    private final MusicPlugin plugin;
    
    public LoopCommand(MusicPlugin plugin) {
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
        
        String modeOption = context.getOption("mode", "");
        GuildMusicManager.LoopMode newMode;
        
        if (modeOption.isEmpty()) {
            // Cycle through modes if no specific mode provided
            newMode = guildManager.setLoopMode(null);
        } else {
            // Set specific mode
            try {
                newMode = GuildMusicManager.LoopMode.valueOf(modeOption.toUpperCase());
                guildManager.setLoopMode(newMode);
            } catch (IllegalArgumentException e) {
                context.replyError("Invalid loop mode. Use: `off`, `track`, or `queue`");
                return CommandResult.error("Invalid mode");
            }
        }
        
        String emoji;
        String description;
        
        switch (newMode) {
            case OFF:
                emoji = "➡️";
                description = "**disabled**";
                break;
            case TRACK:
                emoji = "🔂";
                description = "**track** - Current track will repeat";
                break;
            case QUEUE:
                emoji = "🔁";
                description = "**queue** - Entire queue will repeat";
                break;
            default:
                emoji = "❓";
                description = "unknown";
        }
        
        context.replySuccess(emoji + " Loop mode set to " + description + ".");
        return CommandResult.success();
    }
}
package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;

import java.util.function.Function;

/**
 * Skip command to skip the current track.
 */
public class SkipCommand implements Function<CommandContext, CommandResult> {
    private final MusicPlugin plugin;
    
    public SkipCommand(MusicPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public CommandResult apply(CommandContext context) {
        // TODO: Implement skip functionality
        context.reply("⏭️ Skip command (Implementation in progress)");
        return CommandResult.success();
    }
}
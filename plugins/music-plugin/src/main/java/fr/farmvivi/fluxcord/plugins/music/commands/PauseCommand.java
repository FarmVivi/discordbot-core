package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;

import java.util.function.BiFunction;
import fr.farmvivi.fluxcord.api.command.Command;

/**
 * Pause/resume command for music playback.
 */
public class PauseCommand implements BiFunction<CommandContext, Command, CommandResult> {
    private final MusicPlugin plugin;
    
    public PauseCommand(MusicPlugin plugin) {
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
        
        // Check if anything is playing
        if (guildManager.getAudioPlayer().getPlayingTrack() == null) {
            context.replyError("Nothing is currently playing.");
            return CommandResult.error("Nothing playing");
        }
        
        boolean isPaused = guildManager.getAudioPlayer().isPaused();
        guildManager.setPaused(!isPaused);
        
        if (isPaused) {
            context.replySuccess("▶️ Resumed playback.");
        } else {
            context.replySuccess("⏸️ Paused playback.");
        }
        
        return CommandResult.success();
    }
}
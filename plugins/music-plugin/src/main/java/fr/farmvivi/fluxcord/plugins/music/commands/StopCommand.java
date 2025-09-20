package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;

import java.util.function.BiFunction;
import fr.farmvivi.fluxcord.api.command.Command;

/**
 * Stop command to stop music and clear queue.
 */
public class StopCommand implements BiFunction<CommandContext, Command, CommandResult> {
    private final MusicPlugin plugin;
    
    public StopCommand(MusicPlugin plugin) {
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
        if (guildManager.getAudioPlayer().getPlayingTrack() == null && guildManager.getQueue().isEmpty()) {
            context.replyError("Nothing is currently playing or queued.");
            return CommandResult.error("Nothing playing");
        }
        
        // Stop playback and clear queue
        guildManager.stop();
        
        // Disconnect from voice channel
        guild.getAudioManager().closeAudioConnection();
        
        context.replySuccess("⏹️ Stopped playback and cleared the queue.");
        return CommandResult.success();
    }
}
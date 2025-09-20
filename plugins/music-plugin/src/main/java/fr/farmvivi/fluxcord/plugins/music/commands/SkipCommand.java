package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.function.BiFunction;
import fr.farmvivi.fluxcord.api.command.Command;

/**
 * Skip command to skip the current track.
 */
public class SkipCommand implements BiFunction<CommandContext, Command, CommandResult> {
    private final MusicPlugin plugin;
    
    public SkipCommand(MusicPlugin plugin) {
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
        
        AudioTrack currentTrack = guildManager.getAudioPlayer().getPlayingTrack();
        
        // Check if anything is playing
        if (currentTrack == null) {
            context.replyError("Nothing is currently playing.");
            return CommandResult.error("Nothing playing");
        }
        
        String trackTitle = currentTrack.getInfo().title;
        boolean skipped = guildManager.skipTrack();
        
        if (skipped) {
            context.replySuccess("⏭️ Skipped: **" + trackTitle + "**");
        } else {
            context.replyInfo("⏭️ Skipped: **" + trackTitle + "** (Queue is empty)");
        }
        
        return CommandResult.success();
    }
}
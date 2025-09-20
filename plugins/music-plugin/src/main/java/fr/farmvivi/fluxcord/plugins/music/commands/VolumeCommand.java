package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;

import java.util.function.BiFunction;
import fr.farmvivi.fluxcord.api.command.Command;

/**
 * Volume command to set or view volume.
 */
public class VolumeCommand implements BiFunction<CommandContext, Command, CommandResult> {
    private final MusicPlugin plugin;
    
    public VolumeCommand(MusicPlugin plugin) {
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
        
        // Get current volume
        int currentVolume = guildManager.getAudioPlayer().getVolume();
        
        // Check if user provided a volume level
        if (!context.hasOption("level")) {
            context.replyInfo("🔊 Current volume: **" + currentVolume + "%**");
            return CommandResult.success();
        }
        
        int newVolume = context.getOption("level", currentVolume);
        
        // Validate volume range
        if (newVolume < 0 || newVolume > 100) {
            context.replyError("Volume must be between 0 and 100.");
            return CommandResult.error("Invalid volume");
        }
        
        // Set new volume
        guildManager.setVolume(newVolume);
        
        String volumeIcon;
        if (newVolume == 0) {
            volumeIcon = "🔇";
        } else if (newVolume <= 33) {
            volumeIcon = "🔈";
        } else if (newVolume <= 66) {
            volumeIcon = "🔉";
        } else {
            volumeIcon = "🔊";
        }
        
        context.replySuccess(volumeIcon + " Volume set to **" + newVolume + "%**");
        return CommandResult.success();
    }
}
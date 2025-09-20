package fr.farmvivi.fluxcord.plugins.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.model.GuildMusicPlayer;
import fr.farmvivi.fluxcord.plugins.music.service.AudioPlayerService;
import fr.farmvivi.fluxcord.plugins.music.util.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.awt.Color;

/**
 * Command to display information about the currently playing track.
 */
public class NowPlayingCommand {
    
    private final MusicPlugin plugin;
    private final AudioPlayerService audioPlayerService;
    
    public NowPlayingCommand(MusicPlugin plugin, AudioPlayerService audioPlayerService) {
        this.plugin = plugin;
        this.audioPlayerService = audioPlayerService;
    }
    
    public CommandResult execute(CommandContext context) {
        // Check permission
        if (!plugin.getPluginPermissionManager().hasPermission(
                context.getUser().getId(), "musicplugin.queue")) {
            context.reply("❌ You don't have permission to use this command!");
            return CommandResult.error("No permission");
        }
        
        // Validate that we're in a guild
        if (context.getGuild().isEmpty()) {
            context.reply("❌ This command can only be used in a server!");
            return CommandResult.error("Not in guild");
        }
        
        Guild guild = context.getGuild().get();
        GuildMusicPlayer musicPlayer = audioPlayerService.getGuildPlayer(guild);
        
        AudioTrack currentTrack = musicPlayer.getCurrentTrack();
        
        if (currentTrack == null) {
            context.reply("❌ No music is currently playing!");
            return CommandResult.error("No music playing");
        }
        
        // Create rich embed for now playing
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("🎵 Now Playing")
            .setColor(musicPlayer.isPaused() ? Color.ORANGE : Color.GREEN);
        
        // Track information
        embed.addField("Title", "**" + FormatUtil.escapeMarkdown(currentTrack.getInfo().title) + "**", false);
        embed.addField("Artist", FormatUtil.escapeMarkdown(currentTrack.getInfo().author), true);
        embed.addField("Duration", FormatUtil.formatDuration(currentTrack.getDuration()), true);
        
        // Playback status
        String status = musicPlayer.isPaused() ? "⏸️ Paused" : "▶️ Playing";
        embed.addField("Status", status, true);
        
        // Progress bar and time
        String progressBar = FormatUtil.createProgressBar(
            currentTrack.getPosition(), 
            currentTrack.getDuration(), 
            20
        );
        
        String timeInfo = String.format("%s / %s", 
            FormatUtil.formatDuration(currentTrack.getPosition()),
            FormatUtil.formatDuration(currentTrack.getDuration())
        );
        
        embed.addField("Progress", progressBar + "\n" + timeInfo, false);
        
        // Loop status
        String loopStatus = "Off";
        if (musicPlayer.isLoopTrack()) {
            loopStatus = "🔂 Track";
        } else if (musicPlayer.isLoopQueue()) {
            loopStatus = "🔁 Queue";
        }
        embed.addField("Loop", loopStatus, true);
        
        // Volume and queue info
        embed.addField("Volume", musicPlayer.getVolume() + "%", true);
        embed.addField("Queue", musicPlayer.getQueueSize() + " songs", true);
        
        // Add thumbnail if available (note: artworkUrl may not be available in all LavaPlayer versions)
        // This is commented out for compatibility
        /*
        String artworkUrl = currentTrack.getInfo().artworkUrl;
        if (artworkUrl != null && !artworkUrl.isEmpty()) {
            embed.setThumbnail(artworkUrl);
        }
        */
        
        // Add track URL if available
        String trackUrl = currentTrack.getInfo().uri;
        if (trackUrl != null && !trackUrl.isEmpty() && trackUrl.startsWith("http")) {
            embed.addField("Link", "[Open in browser](" + trackUrl + ")", false);
        }
        
        // Footer with additional info
        embed.setFooter("Requested by " + context.getUser().getName(), 
                       context.getUser().getAvatarUrl());
        
        context.replyEmbed(embed);
        return CommandResult.success();
    }
}
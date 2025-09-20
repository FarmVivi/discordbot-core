package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.GuildMusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.awt.Color;
import java.util.function.BiFunction;
import java.util.concurrent.TimeUnit;
import fr.farmvivi.fluxcord.api.command.Command;

/**
 * Now playing command to show current track.
 */
public class NowPlayingCommand implements BiFunction<CommandContext, Command, CommandResult> {
    private final MusicPlugin plugin;
    
    public NowPlayingCommand(MusicPlugin plugin) {
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
        
        AudioTrackInfo info = currentTrack.getInfo();
        boolean isPaused = guildManager.getAudioPlayer().isPaused();
        
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(isPaused ? Color.ORANGE : Color.GREEN);
        embed.setTitle(isPaused ? "⏸️ Currently Paused" : "🎵 Now Playing");
        
        // Track information
        embed.addField("Title", String.format("[%s](%s)", info.title, info.uri), false);
        if (info.author != null && !info.author.isEmpty()) {
            embed.addField("Artist", info.author, true);
        }
        
        // Duration and progress
        if (info.length != Long.MAX_VALUE) {
            long position = currentTrack.getPosition();
            String progress = formatTime(position) + " / " + formatTime(info.length);
            embed.addField("Duration", progress, true);
            
            // Progress bar
            int progressPercent = (int) ((position * 100) / info.length);
            String progressBar = createProgressBar(progressPercent);
            embed.addField("Progress", progressBar, false);
        } else {
            embed.addField("Duration", "Live Stream", true);
        }
        
        // Queue information
        int queueSize = guildManager.getQueue().size();
        if (queueSize > 0) {
            embed.addField("Queue", queueSize + " track(s) remaining", true);
        }
        
        // Playback settings
        StringBuilder settings = new StringBuilder();
        if (guildManager.getLoopMode() != GuildMusicManager.LoopMode.OFF) {
            settings.append("🔂 Loop: ").append(guildManager.getLoopMode().name().toLowerCase()).append(" ");
        }
        if (guildManager.isShuffleMode()) {
            settings.append("🔀 Shuffle ");
        }
        if (settings.length() > 0) {
            embed.addField("Settings", settings.toString().trim(), false);
        }
        
        // Add thumbnail if available (note: artworkUrl may not be available in all LavaPlayer versions)
        // embed.setThumbnail(info.artworkUrl);
        
        context.replyEmbed(embed);
        return CommandResult.success();
    }
    
    private String formatTime(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    private String createProgressBar(int percent) {
        int filled = percent / 10;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                bar.append("█");
            } else if (i == filled) {
                bar.append("▓");
            } else {
                bar.append("░");
            }
        }
        bar.append(" ").append(percent).append("%");
        return bar.toString();
    }
}
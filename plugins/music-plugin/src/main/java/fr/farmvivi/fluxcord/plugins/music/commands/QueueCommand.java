package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.player.GuildMusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.awt.Color;
import java.util.List;
import java.util.function.BiFunction;
import java.util.concurrent.TimeUnit;
import fr.farmvivi.fluxcord.api.command.Command;

/**
 * Queue command to show the music queue.
 */
public class QueueCommand implements BiFunction<CommandContext, Command, CommandResult> {
    private final MusicPlugin plugin;
    
    public QueueCommand(MusicPlugin plugin) {
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
        List<AudioTrack> queue = guildManager.getQueueCopy();
        
        // Check if anything is playing or queued
        if (currentTrack == null && queue.isEmpty()) {
            context.replyError("The queue is empty.");
            return CommandResult.error("Queue empty");
        }
        
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.BLUE);
        embed.setTitle("📜 Music Queue");
        
        // Current track
        if (currentTrack != null) {
            String status = guildManager.getAudioPlayer().isPaused() ? "[PAUSED]" : "[PLAYING]";
            String duration = formatTime(currentTrack.getInfo().length);
            embed.addField("Currently Playing", 
                String.format("%s **%s** by %s (%s)", status, currentTrack.getInfo().title, 
                currentTrack.getInfo().author, duration), false);
        }
        
        // Queue
        if (!queue.isEmpty()) {
            StringBuilder queueList = new StringBuilder();
            int page = context.getOption("page", 1);
            int itemsPerPage = 10;
            int startIndex = (page - 1) * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, queue.size());
            
            if (startIndex >= queue.size()) {
                context.replyError("Page " + page + " does not exist. Queue has " + 
                    ((queue.size() - 1) / itemsPerPage + 1) + " pages.");
                return CommandResult.error("Invalid page");
            }
            
            for (int i = startIndex; i < endIndex; i++) {
                AudioTrack track = queue.get(i);
                String duration = formatTime(track.getInfo().length);
                queueList.append(String.format("`%d.` **%s** by %s (%s)\n", 
                    i + 1, track.getInfo().title, track.getInfo().author, duration));
            }
            
            embed.addField("Queue (" + queue.size() + " tracks)", queueList.toString(), false);
            
            // Add pagination info if needed
            int totalPages = (queue.size() - 1) / itemsPerPage + 1;
            if (totalPages > 1) {
                embed.setFooter("Page " + page + " of " + totalPages + " | Use /queue page:<number> to view other pages");
            }
        }
        
        // Queue stats
        if (!queue.isEmpty()) {
            long totalDuration = queue.stream()
                .mapToLong(track -> track.getInfo().length)
                .sum();
            embed.addField("Total Duration", formatTime(totalDuration), true);
        }
        
        // Loop and shuffle status
        StringBuilder settings = new StringBuilder();
        if (guildManager.getLoopMode() != GuildMusicManager.LoopMode.OFF) {
            settings.append("🔂 Loop: ").append(guildManager.getLoopMode().name().toLowerCase()).append(" ");
        }
        if (guildManager.isShuffleMode()) {
            settings.append("🔀 Shuffle ");
        }
        if (settings.length() > 0) {
            embed.addField("Settings", settings.toString().trim(), true);
        }
        
        context.replyEmbed(embed);
        return CommandResult.success();
    }
    
    private String formatTime(long millis) {
        if (millis == Long.MAX_VALUE) {
            return "Live";
        }
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
}
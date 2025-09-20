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
import java.util.List;

/**
 * Command to display the current music queue.
 */
public class QueueCommand {
    
    private final MusicPlugin plugin;
    private final AudioPlayerService audioPlayerService;
    
    public QueueCommand(MusicPlugin plugin, AudioPlayerService audioPlayerService) {
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
        List<AudioTrack> queue = musicPlayer.getQueue();
        
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("🎵 Music Queue for " + guild.getName())
            .setColor(Color.BLUE);
        
        // Current track
        if (currentTrack != null) {
            String status = musicPlayer.isPaused() ? "⏸️ Paused" : "▶️ Playing";
            String loopStatus = "";
            if (musicPlayer.isLoopTrack()) {
                loopStatus = " 🔂";
            } else if (musicPlayer.isLoopQueue()) {
                loopStatus = " 🔁";
            }
            
            embed.addField("Now Playing" + loopStatus, 
                String.format("%s **%s** by %s\n%s - `%s / %s`", 
                    status,
                    currentTrack.getInfo().title,
                    currentTrack.getInfo().author,
                    createProgressBar(currentTrack),
                    FormatUtil.formatDuration(currentTrack.getPosition()),
                    FormatUtil.formatDuration(currentTrack.getDuration())
                ), false);
        } else {
            embed.addField("Now Playing", "Nothing is currently playing", false);
        }
        
        // Queue
        if (queue.isEmpty()) {
            embed.addField("Up Next", "Queue is empty", false);
        } else {
            StringBuilder queueText = new StringBuilder();
            int maxDisplay = Math.min(queue.size(), 10);
            
            for (int i = 0; i < maxDisplay; i++) {
                AudioTrack track = queue.get(i);
                queueText.append(String.format("`%d.` **%s** by %s `[%s]`\n",
                    i + 1,
                    track.getInfo().title,
                    track.getInfo().author,
                    FormatUtil.formatDuration(track.getDuration())
                ));
            }
            
            if (queue.size() > 10) {
                queueText.append(String.format("\n... and %d more tracks", queue.size() - 10));
            }
            
            embed.addField("Up Next (" + queue.size() + " tracks)", queueText.toString(), false);
        }
        
        // Footer with additional info
        embed.setFooter(String.format("Volume: %d%% | Queue Duration: %s", 
            musicPlayer.getVolume(), 
            FormatUtil.formatDuration(calculateQueueDuration(queue))));
        
        context.replyEmbed(embed);
        return CommandResult.success();
    }
    
    private String createProgressBar(AudioTrack track) {
        int progressBarLength = 20;
        long position = track.getPosition();
        long duration = track.getDuration();
        
        if (duration <= 0) return "[▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬]";
        
        int progressChars = (int) ((double) position / duration * progressBarLength);
        
        StringBuilder progress = new StringBuilder("[");
        for (int i = 0; i < progressBarLength; i++) {
            if (i == progressChars) {
                progress.append("🔘");
            } else if (i < progressChars) {
                progress.append("▬");
            } else {
                progress.append("▬");
            }
        }
        progress.append("]");
        
        return progress.toString();
    }
    
    private long calculateQueueDuration(List<AudioTrack> queue) {
        return queue.stream()
            .mapToLong(track -> track.getDuration())
            .sum();
    }
}
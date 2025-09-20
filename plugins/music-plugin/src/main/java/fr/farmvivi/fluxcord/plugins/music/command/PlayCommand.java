package fr.farmvivi.fluxcord.plugins.music.command;

import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.service.AudioPlayerService;
import fr.farmvivi.fluxcord.plugins.music.service.VoiceChannelService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

/**
 * Command to play music from URL or search query.
 */
public class PlayCommand {
    
    private final MusicPlugin plugin;
    private final AudioPlayerService audioPlayerService;
    private final VoiceChannelService voiceChannelService;
    
    public PlayCommand(MusicPlugin plugin, AudioPlayerService audioPlayerService, VoiceChannelService voiceChannelService) {
        this.plugin = plugin;
        this.audioPlayerService = audioPlayerService;
        this.voiceChannelService = voiceChannelService;
    }
    
    public CommandResult execute(CommandContext context) {
        // Check permission
        if (!plugin.getPluginPermissionManager().hasPermission(
                context.getUser().getId(), "musicplugin.play")) {
            context.reply("❌ You don't have permission to use this command!");
            return CommandResult.error("No permission");
        }
        
        // Validate that we're in a guild
        if (context.getGuild().isEmpty()) {
            context.reply("❌ This command can only be used in a server!");
            return CommandResult.error("Not in guild");
        }
        
        Guild guild = context.getGuild().get();
        Member member = guild.getMember(context.getUser());
        
        if (member == null) {
            context.reply("❌ Could not find your member information!");
            return CommandResult.error("Member not found");
        }
        
        // Check if user is in a voice channel
        VoiceChannel userVoiceChannel = (VoiceChannel) member.getVoiceState().getChannel();
        if (userVoiceChannel == null) {
            context.reply("❌ You need to be in a voice channel to play music!");
            return CommandResult.error("User not in voice channel");
        }
        
        // Check if bot can connect to the voice channel
        if (!voiceChannelService.canConnectToChannel(userVoiceChannel)) {
            context.reply("❌ I don't have permission to join that voice channel!");
            return CommandResult.error("Cannot connect to voice channel");
        }
        
        // Get the search query or URL
        String query = context.getOption("query", "");
        if (query.trim().isEmpty()) {
            context.reply("❌ Please provide a search query or URL!");
            return CommandResult.error("No query provided");
        }
        
        // Connect to voice channel if not already connected
        if (!voiceChannelService.connectToChannel(userVoiceChannel)) {
            context.reply("❌ Failed to join the voice channel!");
            return CommandResult.error("Failed to connect");
        }
        
        // Set text channel for now playing messages
        if (context.getChannel() instanceof TextChannel textChannel) {
            audioPlayerService.getGuildPlayer(guild).setTextChannel(textChannel);
        }
        
        // Load and play the track
        context.reply("🔍 Searching for: `" + query + "`...");
        
        boolean insertNext = context.getOption("next", false);
        audioPlayerService.loadAndPlay(guild, query, (TextChannel) context.getChannel(), insertNext);
        
        return CommandResult.success();
    }
}
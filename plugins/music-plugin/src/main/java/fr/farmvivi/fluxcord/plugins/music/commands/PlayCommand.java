package fr.farmvivi.fluxcord.plugins.music.commands;

import fr.farmvivi.fluxcord.api.command.Command;
import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import java.util.function.BiFunction;

/**
 * Play command to start music playback from various sources.
 */
public class PlayCommand implements BiFunction<CommandContext, Command, CommandResult> {
    private final MusicPlugin plugin;
    
    public PlayCommand(MusicPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public CommandResult apply(CommandContext context, Command command) {
        String query = context.getOption("query", "");
        
        if (query.trim().isEmpty()) {
            context.replyError("Please provide a song name, URL, or search term.");
            return CommandResult.error("No query provided");
        }
        
        // Check if user is in a guild
        if (!context.isFromGuild()) {
            context.replyError("This command can only be used in a server.");
            return CommandResult.error("Not in guild");
        }
        
        Guild guild = context.getGuild().get();
        Member member = guild.getMember(context.getUser());
        
        if (member == null) {
            context.replyError("Could not find you in this server.");
            return CommandResult.error("Member not found");
        }
        
        // Check if user is in a voice channel
        AudioChannelUnion voiceChannel = member.getVoiceState().getChannel();
        if (voiceChannel == null) {
            context.replyError("You need to be in a voice channel to play music.");
            return CommandResult.error("Not in voice channel");
        }
        
        // Check if bot can connect to the voice channel
        if (!guild.getAudioManager().isConnected()) {
            // Connect to the voice channel
            guild.getAudioManager().openAudioConnection(voiceChannel);
        }
        
        // Defer reply for potentially long loading times
        context.deferReply();
        
        // Load and play the track
        plugin.getMusicManager().loadTrack(guild, query, null, false)
            .thenAccept(result -> {
                // Edit the deferred reply with the result
                context.reply(result);
            })
            .exceptionally(throwable -> {
                plugin.getLogger().error("Error loading track", throwable);
                context.replyError("An error occurred while loading the track.");
                return null;
            });
        
        return CommandResult.success();
    }
}
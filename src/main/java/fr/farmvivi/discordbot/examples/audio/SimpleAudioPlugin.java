package fr.farmvivi.discordbot.examples.audio;

import fr.farmvivi.discordbot.core.api.audio.MixingAudioSendHandler;
import fr.farmvivi.discordbot.core.api.audio.MultiUserAudioReceiveHandler;
import fr.farmvivi.discordbot.core.api.audio.PluginAudioAPI;
import fr.farmvivi.discordbot.core.api.audio.events.VoiceChannelJoinedEvent;
import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.api.event.EventHandler;
import fr.farmvivi.discordbot.core.api.event.EventRegistry;
import fr.farmvivi.discordbot.core.api.plugin.AbstractPlugin;
import fr.farmvivi.discordbot.core.api.plugin.PluginContext;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Simple example plugin to demonstrate audio functionality.
 */
public class SimpleAudioPlugin extends AbstractPlugin {
    private static final String VERSION = "1.0.0";
    
    private PluginContext context;
    private Logger logger;
    private DiscordAPI discordAPI;
    private PluginAudioAPI audioAPI;
    private EventRegistry eventRegistry;
    
    // Map to keep track of recording sessions (guildId -> filename)
    private final Map<String, File> recordingSessions = new ConcurrentHashMap<>();
    // Map to store audio sources (guildId -> sourceIds)
    private final Map<String, Set<String>> audioSources = new ConcurrentHashMap<>();
    
    private CommandHandler commandHandler;

    @Override
    public String getName() {
        return "SimpleAudioPlugin";
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void onLoad(PluginContext context) {
        this.context = context;
        this.logger = context.getLogger();
        this.discordAPI = context.getDiscordAPI();
        this.audioAPI = context.getAudioAPI();
        this.eventRegistry = context.getEventRegistry();
        
        logger.info("SimpleAudioPlugin loaded");
    }

    @Override
    public void onEnable() {
        // Register our event listener for voice channel join
        eventRegistry.registerListener(this);
        
        // Create command handler
        commandHandler = new CommandHandler();
        
        // Register our JDA event listener for slash commands
        discordAPI.getJDA().addEventListener(commandHandler);
        
        // Register slash commands
        discordAPI.getJDA().updateCommands().addCommands(
                Commands.slash("join", "Join a voice channel")
                        .addOption(OptionType.CHANNEL, "channel", "The voice channel to join", true),
                Commands.slash("leave", "Leave the current voice channel"),
                Commands.slash("play", "Play an audio source")
                        .addOption(OptionType.STRING, "name", "Name of the audio source", true),
                Commands.slash("stop", "Stop an audio source")
                        .addOption(OptionType.STRING, "name", "Name of the audio source", true),
                Commands.slash("list", "List all audio sources"),
                Commands.slash("record", "Start recording audio")
                        .addOption(OptionType.STRING, "filename", "Name of the file to save the recording to", true),
                Commands.slash("stoprecord", "Stop recording audio")
        ).queue();
        
        logger.info("SimpleAudioPlugin enabled");
    }

    @Override
    public void onDisable() {
        // Unregister our event listeners
        eventRegistry.unregisterListener(this);
        
        // Unregister JDA event listener
        if (commandHandler != null) {
            discordAPI.getJDA().removeEventListener(commandHandler);
        }
        
        // Leave all voice channels
        discordAPI.getJDA().getGuilds().forEach(guild -> {
            if (audioAPI.isConnected(guild)) {
                audioAPI.leaveVoiceChannel(guild);
            }
        });
        
        // Clear maps
        recordingSessions.clear();
        audioSources.clear();
        
        logger.info("SimpleAudioPlugin disabled");
    }
    
    /**
     * Event handler for when the bot joins a voice channel.
     */
    @EventHandler
    public void onVoiceChannelJoined(VoiceChannelJoinedEvent event) {
        Guild guild = event.getGuild();
        VoiceChannel channel = event.getVoiceChannel();
        
        logger.info("Joined voice channel {} in guild {}", channel.getName(), guild.getName());
        
        // Initialize audio source set for this guild
        audioSources.putIfAbsent(guild.getId(), ConcurrentHashMap.newKeySet());
    }
    
    /**
     * Plays a test tone to a guild's voice channel.
     * 
     * @param guild The guild
     * @param sourceName The name of the audio source
     * @return true if the operation was successful, false otherwise
     */
    private boolean playTestTone(Guild guild, String sourceName) {
        if (!audioAPI.isConnected(guild)) {
            return false;
        }
        
        try {
            // Create a test tone (440 Hz sine wave, 3 seconds)
            byte[] audioData = generateSineWave(440, 3.0f, 48000);
            
            // Get or create a mixing audio handler
            MixingAudioSendHandler handler = audioAPI.setupMixingAudio(guild, SpeakingMode.VOICE)
                    .orElseThrow(() -> new IllegalStateException("Failed to create mixing audio handler"));
            
            // Add source if needed
            if (!handler.getAudioSources().contains(sourceName)) {
                handler.addAudioSource(sourceName);
                // Add to our sources map
                audioSources.get(guild.getId()).add(sourceName);
            }
            
            // Queue the audio data
            handler.queueAudio(sourceName, audioData);
            
            return true;
        } catch (Exception e) {
            logger.error("Error playing test tone", e);
            return false;
        }
    }
    
    /**
     * Stops playing an audio source.
     * 
     * @param guild The guild
     * @param sourceName The name of the audio source
     * @return true if the operation was successful, false otherwise
     */
    private boolean stopSource(Guild guild, String sourceName) {
        if (!audioAPI.isConnected(guild)) {
            return false;
        }
        
        Optional<MixingAudioSendHandler> handlerOpt = audioAPI.getAudioSendHandler(guild)
                .filter(handler -> handler instanceof MixingAudioSendHandler)
                .map(handler -> (MixingAudioSendHandler) handler);
        
        if (handlerOpt.isEmpty()) {
            return false;
        }
        
        MixingAudioSendHandler handler = handlerOpt.get();
        
        // Check if the source exists
        if (!handler.getAudioSources().contains(sourceName)) {
            return false;
        }
        
        // Clear the source queue
        handler.clearSourceQueue(sourceName);
        return true;
    }
    
    /**
     * Starts recording audio from a guild's voice channel.
     * 
     * @param guild The guild
     * @param filename The name of the file to save the recording to
     * @return true if the operation was successful, false otherwise
     */
    private boolean startRecording(Guild guild, String filename) {
        if (!audioAPI.isConnected(guild)) {
            return false;
        }
        
        // Check if already recording
        if (recordingSessions.containsKey(guild.getId())) {
            return false;
        }
        
        try {
            // Create recording file
            File recordingFile = new File(context.getDataFolder(), filename);
            if (!recordingFile.getParentFile().exists()) {
                recordingFile.getParentFile().mkdirs();
            }
            
            // Create audio recording handler
            Consumer<byte[]> audioHandler = data -> {
                try {
                    // In a real implementation, you would want to append this to the file
                    // For simplicity, we'll just log that we received data
                    logger.debug("Received {} bytes of audio data", data.length);
                    
                    // In a real implementation:
                    // Files.write(recordingFile.toPath(), data, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (Exception e) {
                    logger.error("Error writing audio data", e);
                }
            };
            
            // Create a map for individual user audio handlers (empty for now, we'll just use combined audio)
            Map<String, Consumer<byte[]>> userHandlers = new HashMap<>();
            
            // Setup multi-user audio handler
            MultiUserAudioReceiveHandler handler = audioAPI.setupMultiUserAudio(guild, audioHandler, userHandlers)
                    .orElseThrow(() -> new IllegalStateException("Failed to create multi-user audio handler"));
            
            // Store recording session
            recordingSessions.put(guild.getId(), recordingFile);
            
            return true;
        } catch (Exception e) {
            logger.error("Error starting recording", e);
            return false;
        }
    }
    
    /**
     * Stops recording audio from a guild's voice channel.
     * 
     * @param guild The guild
     * @return true if the operation was successful, false otherwise
     */
    private boolean stopRecording(Guild guild) {
        if (!recordingSessions.containsKey(guild.getId())) {
            return false;
        }
        
        try {
            // Get the recording file
            File recordingFile = recordingSessions.remove(guild.getId());
            
            // Unregister audio receive handler
            audioAPI.unregisterAudioReceiveHandler(guild);
            
            logger.info("Stopped recording to file {}", recordingFile.getName());
            return true;
        } catch (Exception e) {
            logger.error("Error stopping recording", e);
            return false;
        }
    }
    
    /**
     * Generates a sine wave as PCM audio.
     * 
     * @param frequency The frequency of the sine wave in Hz
     * @param duration The duration of the sine wave in seconds
     * @param sampleRate The sample rate in Hz
     * @return The PCM audio data
     */
    private byte[] generateSineWave(double frequency, float duration, int sampleRate) {
        int numSamples = (int) (duration * sampleRate);
        byte[] buffer = new byte[numSamples * 2]; // 16-bit samples, 2 bytes per sample
        
        double amplitude = 32760; // Maximum amplitude for 16-bit audio
        double period = (2.0 * Math.PI * frequency) / sampleRate;
        
        for (int i = 0; i < numSamples; i++) {
            double angle = period * i;
            short sample = (short) (amplitude * Math.sin(angle));
            
            // Write sample to buffer (little-endian: low byte first, then high byte)
            buffer[i * 2] = (byte) (sample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        
        return buffer;
    }

    /**
     * Slash command handler for the plugin.
     */
    private class CommandHandler extends ListenerAdapter {
        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            if (!event.isFromGuild()) {
                event.reply("This command can only be used in a guild.").setEphemeral(true).queue();
                return;
            }
            
            Guild guild = event.getGuild();
            String command = event.getName();
            
            switch (command) {
                case "join":
                    handleJoinCommand(event);
                    break;
                case "leave":
                    handleLeaveCommand(event);
                    break;
                case "play":
                    handlePlayCommand(event);
                    break;
                case "stop":
                    handleStopCommand(event);
                    break;
                case "list":
                    handleListCommand(event);
                    break;
                case "record":
                    handleRecordCommand(event);
                    break;
                case "stoprecord":
                    handleStopRecordCommand(event);
                    break;
                default:
                    event.reply("Unknown command: " + command).setEphemeral(true).queue();
                    break;
            }
        }
        
        private void handleJoinCommand(SlashCommandInteractionEvent event) {
            net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel channel = event.getOption("channel").getAsChannel().asVoiceChannel();
            
            if (audioAPI.isConnected(event.getGuild()) && 
                    audioAPI.getConnectedChannel(event.getGuild()).isPresent() &&
                    audioAPI.getConnectedChannel(event.getGuild()).get().getIdLong() == channel.getIdLong()) {
                event.reply("Already connected to " + channel.getAsMention()).setEphemeral(true).queue();
                return;
            }
            
            boolean success = audioAPI.joinVoiceChannel(channel);
            
            if (success) {
                event.reply("Joined " + channel.getAsMention()).queue();
            } else {
                event.reply("Failed to join " + channel.getAsMention()).setEphemeral(true).queue();
            }
        }
        
        private void handleLeaveCommand(SlashCommandInteractionEvent event) {
            if (!audioAPI.isConnected(event.getGuild())) {
                event.reply("Not connected to any voice channel").setEphemeral(true).queue();
                return;
            }
            
            VoiceChannel channel = audioAPI.getConnectedChannel(event.getGuild()).get();
            boolean success = audioAPI.leaveVoiceChannel(event.getGuild());
            
            if (success) {
                event.reply("Left " + channel.getAsMention()).queue();
            } else {
                event.reply("Failed to leave " + channel.getAsMention()).setEphemeral(true).queue();
            }
        }
        
        private void handlePlayCommand(SlashCommandInteractionEvent event) {
            if (!audioAPI.isConnected(event.getGuild())) {
                event.reply("Not connected to any voice channel").setEphemeral(true).queue();
                return;
            }
            
            String sourceName = event.getOption("name").getAsString();
            boolean success = playTestTone(event.getGuild(), sourceName);
            
            if (success) {
                event.reply("Playing test tone with source name: " + sourceName).queue();
            } else {
                event.reply("Failed to play test tone").setEphemeral(true).queue();
            }
        }
        
        private void handleStopCommand(SlashCommandInteractionEvent event) {
            if (!audioAPI.isConnected(event.getGuild())) {
                event.reply("Not connected to any voice channel").setEphemeral(true).queue();
                return;
            }
            
            String sourceName = event.getOption("name").getAsString();
            boolean success = stopSource(event.getGuild(), sourceName);
            
            if (success) {
                event.reply("Stopped source: " + sourceName).queue();
            } else {
                event.reply("Failed to stop source: " + sourceName).setEphemeral(true).queue();
            }
        }
        
        private void handleListCommand(SlashCommandInteractionEvent event) {
            if (!audioAPI.isConnected(event.getGuild())) {
                event.reply("Not connected to any voice channel").setEphemeral(true).queue();
                return;
            }
            
            Optional<MixingAudioSendHandler> handlerOpt = audioAPI.getAudioSendHandler(event.getGuild())
                    .filter(handler -> handler instanceof MixingAudioSendHandler)
                    .map(handler -> (MixingAudioSendHandler) handler);
            
            if (handlerOpt.isEmpty()) {
                event.reply("No audio sources found").setEphemeral(true).queue();
                return;
            }
            
            MixingAudioSendHandler handler = handlerOpt.get();
            Set<String> sources = handler.getAudioSources();
            
            if (sources.isEmpty()) {
                event.reply("No audio sources found").setEphemeral(true).queue();
                return;
            }
            
            StringBuilder sb = new StringBuilder("**Audio sources:**\n");
            
            for (String source : sources) {
                sb.append("- ").append(source).append(": ")
                  .append(handler.getQueueSize(source)).append(" packets queued\n");
            }
            
            // Also list speaking users
            Set<User> speakingUsers = audioAPI.getSpeakingUsers(event.getGuild());
            
            sb.append("\n**Speaking users:**\n");
            
            if (speakingUsers.isEmpty()) {
                sb.append("No users are speaking");
            } else {
                for (User user : speakingUsers) {
                    sb.append("- ").append(user.getName()).append("\n");
                }
            }
            
            event.reply(sb.toString()).queue();
        }
        
        private void handleRecordCommand(SlashCommandInteractionEvent event) {
            if (!audioAPI.isConnected(event.getGuild())) {
                event.reply("Not connected to any voice channel").setEphemeral(true).queue();
                return;
            }
            
            String filename = event.getOption("filename").getAsString();
            
            if (!filename.endsWith(".pcm") && !filename.endsWith(".wav")) {
                filename += ".pcm"; // Default to PCM format
            }
            
            boolean success = startRecording(event.getGuild(), filename);
            
            if (success) {
                event.reply("Started recording to file: " + filename).queue();
            } else {
                event.reply("Failed to start recording").setEphemeral(true).queue();
            }
        }
        
        private void handleStopRecordCommand(SlashCommandInteractionEvent event) {
            if (!recordingSessions.containsKey(event.getGuild().getId())) {
                event.reply("Not currently recording").setEphemeral(true).queue();
                return;
            }
            
            File recordingFile = recordingSessions.get(event.getGuild().getId());
            boolean success = stopRecording(event.getGuild());
            
            if (success) {
                event.reply("Stopped recording to file: " + recordingFile.getName()).queue();
            } else {
                event.reply("Failed to stop recording").setEphemeral(true).queue();
            }
        }
    }
}
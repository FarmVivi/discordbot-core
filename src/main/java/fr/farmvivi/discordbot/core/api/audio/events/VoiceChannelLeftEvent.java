package fr.farmvivi.discordbot.core.api.audio.events;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

/**
 * Event fired after the bot has left a voice channel.
 */
public class VoiceChannelLeftEvent extends AudioEvent {
    private final VoiceChannel voiceChannel;

    /**
     * Creates a new VoiceChannelLeftEvent.
     *
     * @param guild The guild where the event occurred
     * @param voiceChannel The voice channel that was left
     */
    public VoiceChannelLeftEvent(Guild guild, VoiceChannel voiceChannel) {
        super(guild);
        this.voiceChannel = voiceChannel;
    }

    /**
     * Gets the voice channel that was left.
     *
     * @return The voice channel
     */
    public VoiceChannel getVoiceChannel() {
        return voiceChannel;
    }
}
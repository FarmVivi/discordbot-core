package fr.farmvivi.discordbot.core.api.audio.events;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

/**
 * Event fired after the bot has joined a voice channel.
 */
public class VoiceChannelJoinedEvent extends AudioEvent {
    private final VoiceChannel voiceChannel;

    /**
     * Creates a new VoiceChannelJoinedEvent.
     *
     * @param guild The guild where the event occurred
     * @param voiceChannel The voice channel that was joined
     */
    public VoiceChannelJoinedEvent(Guild guild, VoiceChannel voiceChannel) {
        super(guild);
        this.voiceChannel = voiceChannel;
    }

    /**
     * Gets the voice channel that was joined.
     *
     * @return The voice channel
     */
    public VoiceChannel getVoiceChannel() {
        return voiceChannel;
    }
}
package fr.farmvivi.discordbot.core.api.audio.events;

import fr.farmvivi.discordbot.core.api.event.Cancellable;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

/**
 * Event fired when the bot is about to join a voice channel.
 * This event is cancellable.
 */
public class VoiceChannelJoinEvent extends AudioEvent implements Cancellable {
    private final VoiceChannel voiceChannel;
    private boolean cancelled = false;

    /**
     * Creates a new VoiceChannelJoinEvent.
     *
     * @param guild The guild where the event occurred
     * @param voiceChannel The voice channel being joined
     */
    public VoiceChannelJoinEvent(Guild guild, VoiceChannel voiceChannel) {
        super(guild);
        this.voiceChannel = voiceChannel;
    }

    /**
     * Gets the voice channel being joined.
     *
     * @return The voice channel
     */
    public VoiceChannel getVoiceChannel() {
        return voiceChannel;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
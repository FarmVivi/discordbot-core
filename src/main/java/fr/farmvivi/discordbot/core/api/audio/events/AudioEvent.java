package fr.farmvivi.discordbot.core.api.audio.events;

import fr.farmvivi.discordbot.core.api.event.Event;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Base class for all audio-related events.
 */
public abstract class AudioEvent extends Event {
    private final Guild guild;

    /**
     * Creates a new AudioEvent.
     *
     * @param guild The guild where the event occurred
     */
    public AudioEvent(Guild guild) {
        this.guild = guild;
    }

    /**
     * Gets the guild where the event occurred.
     *
     * @return The guild
     */
    public Guild getGuild() {
        return guild;
    }
}
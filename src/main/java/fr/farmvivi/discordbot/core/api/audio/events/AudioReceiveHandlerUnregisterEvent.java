package fr.farmvivi.discordbot.core.api.audio.events;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Event fired when an audio receive handler is about to be unregistered.
 */
public class AudioReceiveHandlerUnregisterEvent extends AudioHandlerEvent {
    private final AudioReceiveHandler handler;

    /**
     * Creates a new AudioReceiveHandlerUnregisterEvent.
     *
     * @param guild The guild where the event occurred
     * @param handler The audio receive handler being unregistered
     */
    public AudioReceiveHandlerUnregisterEvent(Guild guild, AudioReceiveHandler handler) {
        super(guild);
        this.handler = handler;
    }

    /**
     * Gets the audio receive handler being unregistered.
     *
     * @return The audio receive handler
     */
    public AudioReceiveHandler getHandler() {
        return handler;
    }
}
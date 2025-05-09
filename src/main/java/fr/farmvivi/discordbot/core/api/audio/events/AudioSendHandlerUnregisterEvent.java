package fr.farmvivi.discordbot.core.api.audio.events;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Event fired when an audio send handler is about to be unregistered.
 */
public class AudioSendHandlerUnregisterEvent extends AudioHandlerEvent {
    private final AudioSendHandler handler;

    /**
     * Creates a new AudioSendHandlerUnregisterEvent.
     *
     * @param guild The guild where the event occurred
     * @param handler The audio send handler being unregistered
     */
    public AudioSendHandlerUnregisterEvent(Guild guild, AudioSendHandler handler) {
        super(guild);
        this.handler = handler;
    }

    /**
     * Gets the audio send handler being unregistered.
     *
     * @return The audio send handler
     */
    public AudioSendHandler getHandler() {
        return handler;
    }
}
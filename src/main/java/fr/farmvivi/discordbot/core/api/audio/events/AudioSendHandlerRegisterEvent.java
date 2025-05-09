package fr.farmvivi.discordbot.core.api.audio.events;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Event fired when an audio send handler is about to be registered.
 */
public class AudioSendHandlerRegisterEvent extends AudioHandlerEvent {
    private final AudioSendHandler handler;

    /**
     * Creates a new AudioSendHandlerRegisterEvent.
     *
     * @param guild The guild where the event occurred
     * @param handler The audio send handler being registered
     */
    public AudioSendHandlerRegisterEvent(Guild guild, AudioSendHandler handler) {
        super(guild);
        this.handler = handler;
    }

    /**
     * Gets the audio send handler being registered.
     *
     * @return The audio send handler
     */
    public AudioSendHandler getHandler() {
        return handler;
    }
}
package fr.farmvivi.discordbot.core.api.audio.events;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Event fired when an audio receive handler is about to be registered.
 */
public class AudioReceiveHandlerRegisterEvent extends AudioHandlerEvent {
    private final AudioReceiveHandler handler;

    /**
     * Creates a new AudioReceiveHandlerRegisterEvent.
     *
     * @param guild The guild where the event occurred
     * @param handler The audio receive handler being registered
     */
    public AudioReceiveHandlerRegisterEvent(Guild guild, AudioReceiveHandler handler) {
        super(guild);
        this.handler = handler;
    }

    /**
     * Gets the audio receive handler being registered.
     *
     * @return The audio receive handler
     */
    public AudioReceiveHandler getHandler() {
        return handler;
    }
}
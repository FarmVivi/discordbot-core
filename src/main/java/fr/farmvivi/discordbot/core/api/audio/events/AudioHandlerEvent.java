package fr.farmvivi.discordbot.core.api.audio.events;

import fr.farmvivi.discordbot.core.api.event.Cancellable;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Base class for audio handler registration/unregistration events.
 */
public abstract class AudioHandlerEvent extends AudioEvent implements Cancellable {
    private boolean cancelled = false;

    /**
     * Creates a new AudioHandlerEvent.
     *
     * @param guild The guild where the event occurred
     */
    protected AudioHandlerEvent(Guild guild) {
        super(guild);
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
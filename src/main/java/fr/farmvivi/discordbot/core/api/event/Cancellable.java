package fr.farmvivi.discordbot.core.api.event;

/**
 * Interface for cancellable events.
 */
public interface Cancellable {
    /**
     * Checks if the event is cancelled.
     *
     * @return true if the event is cancelled
     */
    boolean isCancelled();

    /**
     * Sets the cancelled state of the event.
     *
     * @param cancelled true to cancel the event
     */
    void setCancelled(boolean cancelled);
}
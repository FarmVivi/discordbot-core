package fr.farmvivi.discordbot.core.api.event;

/**
 * Annotation to mark methods as event handlers.
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
public @interface EventHandler {
    /**
     * The priority of the event handler.
     * Higher priority event handlers are called first.
     *
     * @return the priority
     */
    EventPriority priority() default EventPriority.NORMAL;

    /**
     * Whether this event handler should be called even if the event is cancelled.
     *
     * @return true if the handler should be called even if the event is cancelled
     */
    boolean ignoreCancelled() default false;
}
package fr.farmvivi.fluxcord.api.language.events;

import fr.farmvivi.fluxcord.api.event.Event;

/**
 * Base class for language-related events.
 */
public abstract class LanguageEvent implements Event {
    private final String namespace;

    /**
     * Creates a new language event.
     *
     * @param namespace the language namespace
     */
    protected LanguageEvent(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Gets the language namespace.
     *
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }
}
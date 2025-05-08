package fr.farmvivi.discordbot.core.language.events;

/**
 * Event fired when a language namespace is registered.
 */
public class NamespaceRegisteredEvent extends LanguageEvent {
    /**
     * Creates a new namespace registered event.
     *
     * @param namespace the namespace that was registered
     */
    public NamespaceRegisteredEvent(String namespace) {
        super(namespace);
    }
}

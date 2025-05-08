package fr.farmvivi.discordbot.core.api.language.events;

import java.util.Locale;
import java.util.Map;

/**
 * Event fired when language strings are loaded for a namespace.
 */
public class LanguageLoadedEvent extends LanguageEvent {
    private final Locale locale;
    private final int translationCount;
    private final Map<String, String> translations;

    /**
     * Creates a new language loaded event.
     *
     * @param namespace        the namespace
     * @param locale           the locale
     * @param translationCount the number of translations loaded
     * @param translations     the translations that were loaded
     */
    public LanguageLoadedEvent(String namespace, Locale locale, int translationCount,
                               Map<String, String> translations) {
        super(namespace);
        this.locale = locale;
        this.translationCount = translationCount;
        this.translations = translations;
    }

    /**
     * Gets the locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Gets the number of translations loaded.
     *
     * @return the translation count
     */
    public int getTranslationCount() {
        return translationCount;
    }

    /**
     * Gets the translations that were loaded.
     *
     * @return the translations
     */
    public Map<String, String> getTranslations() {
        return translations;
    }
}

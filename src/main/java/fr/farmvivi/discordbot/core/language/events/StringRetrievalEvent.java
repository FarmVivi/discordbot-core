package fr.farmvivi.discordbot.core.language.events;

import java.util.Locale;

/**
 * Event fired when a string is retrieved from the language manager.
 * This can be used to track missing translations or override translations.
 */
public class StringRetrievalEvent extends LanguageEvent {
    private final Locale locale;
    private final String key;
    private final Object[] args;
    private String value;
    private boolean overridden = false;

    /**
     * Creates a new string retrieval event.
     *
     * @param namespace the namespace
     * @param locale    the locale
     * @param key       the key
     * @param args      the arguments for placeholder replacement
     * @param value     the value that was retrieved
     */
    public StringRetrievalEvent(String namespace, Locale locale, String key, Object[] args, String value) {
        super(namespace);
        this.locale = locale;
        this.key = key;
        this.args = args;
        this.value = value;
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
     * Gets the key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the arguments for placeholder replacement.
     *
     * @return the arguments
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * Gets the value that was retrieved.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value, overriding the original value.
     *
     * @param value the new value
     */
    public void setValue(String value) {
        this.value = value;
        this.overridden = true;
    }

    /**
     * Checks if the value has been overridden.
     *
     * @return true if the value has been overridden, false otherwise
     */
    public boolean isOverridden() {
        return overridden;
    }
}

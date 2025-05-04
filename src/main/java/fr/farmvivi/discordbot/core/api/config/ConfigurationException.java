package fr.farmvivi.discordbot.core.api.config;

/**
 * Exception thrown when there's an error with configuration.
 */
public class ConfigurationException extends Exception {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
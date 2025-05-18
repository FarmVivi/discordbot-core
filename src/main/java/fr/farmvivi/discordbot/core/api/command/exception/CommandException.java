package fr.farmvivi.discordbot.core.api.command.exception;

/**
 * Base exception for all command-related errors.
 * This exception is the parent class for more specific command exceptions.
 */
public class CommandException extends Exception {
    
    /**
     * Creates a new command exception with a message.
     *
     * @param message the error message
     */
    public CommandException(String message) {
        super(message);
    }
    
    /**
     * Creates a new command exception with a message and a cause.
     *
     * @param message the error message
     * @param cause the cause of the exception
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates a new command exception with a cause.
     *
     * @param cause the cause of the exception
     */
    public CommandException(Throwable cause) {
        super(cause);
    }
}

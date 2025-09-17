package fr.farmvivi.discordbot.core.api.command.exception;

/**
 * Exception thrown when executing a command fails.
 * This includes errors during command business logic execution.
 */
public class CommandExecutionException extends CommandException {
    
    /**
     * Creates a new command execution exception with a message.
     *
     * @param message the error message
     */
    public CommandExecutionException(String message) {
        super(message);
    }
    
    /**
     * Creates a new command execution exception with a message and a cause.
     *
     * @param message the error message
     * @param cause the cause of the exception
     */
    public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates a new command execution exception with a cause.
     *
     * @param cause the cause of the exception
     */
    public CommandExecutionException(Throwable cause) {
        super(cause);
    }
}

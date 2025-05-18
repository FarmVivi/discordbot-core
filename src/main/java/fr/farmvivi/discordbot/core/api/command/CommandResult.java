package fr.farmvivi.discordbot.core.api.command;

/**
 * Represents the result of a command execution.
 * Contains information about success/failure and possible error messages.
 */
public class CommandResult {
    
    private final boolean success;
    private final String errorMessage;
    
    private CommandResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Checks if the command execution was successful.
     *
     * @return true if the command execution was successful
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Gets the error message if the command execution failed.
     *
     * @return the error message, or null if the command execution was successful
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Creates a successful result without any specific message.
     *
     * @return a successful result
     */
    public static CommandResult success() {
        return new CommandResult(true, null);
    }
    
    /**
     * Creates a successful result with a specific success message.
     *
     * @param message the success message
     * @return a successful result
     */
    public static CommandResult success(String message) {
        return new CommandResult(true, message);
    }
    
    /**
     * Creates a failed result with an error message.
     *
     * @param errorMessage the error message
     * @return a failed result
     */
    public static CommandResult error(String errorMessage) {
        return new CommandResult(false, errorMessage);
    }
}

package fr.farmvivi.discordbot.core.api.command.exception;

/**
 * Exception thrown when parsing a command fails.
 * This includes syntax errors, invalid argument types, missing arguments, etc.
 */
public class CommandParseException extends CommandException {

    private final String parameter;

    /**
     * Creates a new command parse exception.
     *
     * @param message the error message
     */
    public CommandParseException(String message) {
        this(message, null);
    }

    /**
     * Creates a new command parse exception with a specific parameter.
     *
     * @param message the error message
     * @param parameter the parameter that caused the error, or null if not applicable
     */
    public CommandParseException(String message, String parameter) {
        super(message);
        this.parameter = parameter;
    }

    /**
     * Creates a new command parse exception with a cause.
     *
     * @param message the error message
     * @param cause the cause of the exception
     */
    public CommandParseException(String message, Throwable cause) {
        this(message, null, cause);
    }

    /**
     * Creates a new command parse exception with a specific parameter and cause.
     *
     * @param message the error message
     * @param parameter the parameter that caused the error, or null if not applicable
     * @param cause the cause of the exception
     */
    public CommandParseException(String message, String parameter, Throwable cause) {
        super(message, cause);
        this.parameter = parameter;
    }

    /**
     * Gets the parameter that caused the error.
     *
     * @return the parameter name, or null if not applicable
     */
    public String getParameter() {
        return parameter;
    }
}

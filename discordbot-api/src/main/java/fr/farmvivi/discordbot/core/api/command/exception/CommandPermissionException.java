package fr.farmvivi.discordbot.core.api.command.exception;

/**
 * Exception thrown when permission is denied for a command.
 */
public class CommandPermissionException extends CommandException {
    
    private final String permission;
    
    /**
     * Creates a new command permission exception.
     *
     * @param message the error message
     * @param permission the permission that was denied, or null if not applicable
     */
    public CommandPermissionException(String message, String permission) {
        super(message);
        this.permission = permission;
    }
    
    /**
     * Gets the permission that was denied.
     *
     * @return the permission, or null if not applicable
     */
    public String getPermission() {
        return permission;
    }
}

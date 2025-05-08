package fr.farmvivi.discordbot.core.permissions;

/**
 * Exception thrown when a user does not have a required permission.
 */
public class PermissionDeniedException extends Exception {
    private final String permission;
    private final String userId;
    private final String guildId;

    /**
     * Creates a new permission denied exception.
     *
     * @param message    the error message
     * @param permission the permission that was denied
     * @param userId     the user ID
     */
    public PermissionDeniedException(String message, String permission, String userId) {
        this(message, permission, userId, null);
    }

    /**
     * Creates a new permission denied exception.
     *
     * @param message    the error message
     * @param permission the permission that was denied
     * @param userId     the user ID
     * @param guildId    the guild ID (may be null)
     */
    public PermissionDeniedException(String message, String permission, String userId, String guildId) {
        super(message);
        this.permission = permission;
        this.userId = userId;
        this.guildId = guildId;
    }

    /**
     * Creates a new permission denied exception with a cause.
     *
     * @param message    the error message
     * @param cause      the cause
     * @param permission the permission that was denied
     * @param userId     the user ID
     */
    public PermissionDeniedException(String message, Throwable cause, String permission, String userId) {
        this(message, cause, permission, userId, null);
    }

    /**
     * Creates a new permission denied exception with a cause.
     *
     * @param message    the error message
     * @param cause      the cause
     * @param permission the permission that was denied
     * @param userId     the user ID
     * @param guildId    the guild ID (may be null)
     */
    public PermissionDeniedException(String message, Throwable cause, String permission, String userId, String guildId) {
        super(message, cause);
        this.permission = permission;
        this.userId = userId;
        this.guildId = guildId;
    }

    /**
     * Gets the permission that was denied.
     *
     * @return the permission
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the guild ID, or null if this was a global check.
     *
     * @return the guild ID
     */
    public String getGuildId() {
        return guildId;
    }
}
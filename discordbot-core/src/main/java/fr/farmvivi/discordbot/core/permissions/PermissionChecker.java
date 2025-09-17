package fr.farmvivi.discordbot.core.permissions;

import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.api.permissions.PermissionDeniedException;
import fr.farmvivi.discordbot.core.api.permissions.PermissionManager;

/**
 * Utility class for checking permissions.
 * This provides convenient methods for verifying permissions with proper error handling.
 */
public class PermissionChecker {
    private final PermissionManager permissionManager;
    private final LanguageManager languageManager;

    /**
     * Creates a new permission checker.
     *
     * @param permissionManager the permission manager
     * @param languageManager   the language manager
     */
    public PermissionChecker(PermissionManager permissionManager, LanguageManager languageManager) {
        this.permissionManager = permissionManager;
        this.languageManager = languageManager;
    }

    /**
     * Checks if a user has a permission, throwing an exception if not.
     *
     * @param userId     the user ID
     * @param permission the permission to check
     * @throws PermissionDeniedException if the user does not have the permission
     */
    public void checkPermission(String userId, String permission) throws PermissionDeniedException {
        if (!permissionManager.hasPermission(userId, permission)) {
            String message = languageManager.getString("permissions.error.denied", permission);
            throw new PermissionDeniedException(message, permission, userId);
        }
    }

    /**
     * Checks if a user has a permission in a guild, throwing an exception if not.
     *
     * @param userId     the user ID
     * @param guildId    the guild ID
     * @param permission the permission to check
     * @throws PermissionDeniedException if the user does not have the permission
     */
    public void checkPermission(String userId, String guildId, String permission)
            throws PermissionDeniedException {
        if (!permissionManager.hasPermission(userId, guildId, permission)) {
            String message = languageManager.getString("permissions.error.denied", permission);
            throw new PermissionDeniedException(message, permission, userId, guildId);
        }
    }

    /**
     * Checks if a user has a permission, returning false if not.
     *
     * @param userId     the user ID
     * @param permission the permission to check
     * @return true if the user has the permission, false otherwise
     */
    public boolean hasPermission(String userId, String permission) {
        return permissionManager.hasPermission(userId, permission);
    }

    /**
     * Checks if a user has a permission in a guild, returning false if not.
     *
     * @param userId     the user ID
     * @param guildId    the guild ID
     * @param permission the permission to check
     * @return true if the user has the permission, false otherwise
     */
    public boolean hasPermission(String userId, String guildId, String permission) {
        return permissionManager.hasPermission(userId, guildId, permission);
    }
}
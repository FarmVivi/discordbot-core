package fr.farmvivi.discordbot.core.api.permissions;

/**
 * Represents the default value for a permission.
 * This determines whether users have a permission by default.
 */
public enum PermissionDefault {
    /**
     * The permission is granted by default.
     */
    TRUE,

    /**
     * The permission is denied by default.
     */
    FALSE,

    /**
     * The permission is granted by default only to operators.
     */
    OP,

    /**
     * The permission is denied by default only to operators.
     */
    NOT_OP
}
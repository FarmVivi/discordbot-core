package fr.farmvivi.discordbot.core.api.permissions;

/**
 * Interface representing a permission that can be granted to users.
 * Permissions can be checked globally or in the context of a specific guild.
 */
public interface Permission {
    /**
     * Gets the name of the permission.
     * Permission names should be in a dot-notation format, typically:
     * plugin.category.action (e.g., "moderation.ban.user").
     *
     * @return the permission name
     */
    String getName();

    /**
     * Gets the description of the permission.
     * This should be a human-readable description of what the permission allows.
     *
     * @return the permission description
     */
    String getDescription();

    /**
     * Gets the default value of this permission.
     * This determines whether users have this permission by default.
     *
     * @return the default value
     */
    PermissionDefault getDefault();
}
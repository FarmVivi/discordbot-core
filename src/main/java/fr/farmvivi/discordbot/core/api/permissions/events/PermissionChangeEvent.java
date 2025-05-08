package fr.farmvivi.discordbot.core.api.permissions.events;

import fr.farmvivi.discordbot.core.api.event.Event;

/**
 * Event fired when a permission is changed for a user.
 * This can be used to track permission changes or perform actions when permissions change.
 */
public class PermissionChangeEvent implements Event {
    private final String userId;
    private final String guildId; // May be null for global changes
    private final String permission;
    private final boolean oldValue;
    private final boolean newValue;

    /**
     * Creates a new permission change event.
     *
     * @param userId     the user ID
     * @param guildId    the guild ID (may be null)
     * @param permission the permission being changed
     * @param oldValue   the old value
     * @param newValue   the new value
     */
    public PermissionChangeEvent(String userId, String guildId, String permission,
                                 boolean oldValue, boolean newValue) {
        this.userId = userId;
        this.guildId = guildId;
        this.permission = permission;
        this.oldValue = oldValue;
        this.newValue = newValue;
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
     * Gets the guild ID, or null if this is a global change.
     *
     * @return the guild ID
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Gets the permission being changed.
     *
     * @return the permission name
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Gets the old value of the permission.
     *
     * @return the old value
     */
    public boolean getOldValue() {
        return oldValue;
    }

    /**
     * Gets the new value of the permission.
     *
     * @return the new value
     */
    public boolean getNewValue() {
        return newValue;
    }
}
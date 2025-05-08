package fr.farmvivi.discordbot.core.api.permissions.events;

import fr.farmvivi.discordbot.core.api.event.Cancellable;
import fr.farmvivi.discordbot.core.api.event.Event;

/**
 * Event fired when a permission check is performed.
 * This can be used to override permission checks or audit permission usage.
 */
public class PermissionCheckEvent implements Event, Cancellable {
    private final String userId;
    private final String guildId; // May be null for global checks
    private final String permission;
    private boolean result;
    private boolean cancelled = false;

    /**
     * Creates a new permission check event.
     *
     * @param userId     the user ID
     * @param guildId    the guild ID (may be null)
     * @param permission the permission being checked
     * @param result     the initial result of the check
     */
    public PermissionCheckEvent(String userId, String guildId, String permission, boolean result) {
        this.userId = userId;
        this.guildId = guildId;
        this.permission = permission;
        this.result = result;
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
     * Gets the guild ID, or null if this is a global check.
     *
     * @return the guild ID
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Gets the permission being checked.
     *
     * @return the permission name
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Gets the current result of the check.
     *
     * @return true if the user has the permission, false otherwise
     */
    public boolean getResult() {
        return result;
    }

    /**
     * Sets the result of the check.
     * This can be used to override the permission check.
     *
     * @param result the new result
     */
    public void setResult(boolean result) {
        this.result = result;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
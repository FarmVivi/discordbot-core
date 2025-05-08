package fr.farmvivi.discordbot.core.permissions;

import fr.farmvivi.discordbot.core.api.permissions.Permission;
import fr.farmvivi.discordbot.core.api.permissions.PermissionDefault;

/**
 * Builder for creating Permission objects.
 * This provides a fluent API for creating permissions.
 */
public class PermissionBuilder {
    private String name;
    private String description = "";
    private PermissionDefault defaultValue = PermissionDefault.FALSE;

    private PermissionBuilder(String name) {
        this.name = name;
    }

    /**
     * Creates a new permission builder.
     *
     * @param name the permission name
     * @return a new permission builder
     */
    public static PermissionBuilder permission(String name) {
        return new PermissionBuilder(name);
    }

    /**
     * Sets the description of the permission.
     *
     * @param description the description
     * @return this builder
     */
    public PermissionBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the default value of the permission.
     *
     * @param defaultValue the default value
     * @return this builder
     */
    public PermissionBuilder defaultValue(PermissionDefault defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Builds the permission.
     *
     * @return the built permission
     */
    public Permission build() {
        return new PermissionImpl(name, description, defaultValue);
    }
}
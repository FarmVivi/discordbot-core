package fr.farmvivi.discordbot.core.permissions;

import fr.farmvivi.discordbot.core.api.permissions.Permission;
import fr.farmvivi.discordbot.core.api.permissions.PermissionDefault;

import java.util.Objects;

/**
 * Implementation of the Permission interface.
 */
public class PermissionImpl implements Permission {
    private final String name;
    private final String description;
    private final PermissionDefault defaultValue;

    /**
     * Creates a new permission.
     *
     * @param name         the permission name
     * @param description  the permission description
     * @param defaultValue the default value
     */
    public PermissionImpl(String name, String description, PermissionDefault defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue != null ? defaultValue : PermissionDefault.FALSE;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public PermissionDefault getDefault() {
        return defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionImpl that = (PermissionImpl) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Permission{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", default=" + defaultValue +
                '}';
    }
}
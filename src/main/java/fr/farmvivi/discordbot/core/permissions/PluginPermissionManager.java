package fr.farmvivi.discordbot.core.permissions;

import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.api.permissions.Permission;
import fr.farmvivi.discordbot.core.api.permissions.PermissionDefault;
import fr.farmvivi.discordbot.core.api.permissions.PermissionManager;
import fr.farmvivi.discordbot.core.api.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

/**
 * Manager for plugin-specific permissions.
 * This provides a convenient way for plugins to register and check their own permissions.
 */
public class PluginPermissionManager {
    private final Plugin plugin;
    private final PermissionManager permissionManager;
    private final LanguageManager languageManager;
    private final Set<String> registeredPermissions = new HashSet<>();
    private final PermissionChecker checker;

    /**
     * Creates a new plugin permission manager.
     *
     * @param plugin            the plugin
     * @param permissionManager the permission manager
     * @param languageManager   the language manager
     */
    public PluginPermissionManager(Plugin plugin, PermissionManager permissionManager, LanguageManager languageManager) {
        this.plugin = plugin;
        this.permissionManager = permissionManager;
        this.languageManager = languageManager;
        this.checker = new PermissionChecker(permissionManager, languageManager);
    }

    /**
     * Registers a permission for this plugin.
     *
     * @param permission the permission to register
     */
    public void registerPermission(Permission permission) {
        permissionManager.registerPermission(permission, plugin);
        registeredPermissions.add(permission.getName());
    }

    /**
     * Creates and registers a permission for this plugin.
     *
     * @param name         the permission name
     * @param description  the permission description
     * @param defaultValue the default value
     * @return the created permission
     */
    public Permission registerPermission(String name, String description, PermissionDefault defaultValue) {
        // Prefix permission name with plugin name for convention
        String prefixedName = plugin.getName().toLowerCase() + "." + name;

        Permission permission = PermissionBuilder.permission(prefixedName)
                .description(description)
                .defaultValue(defaultValue)
                .build();

        registerPermission(permission);
        return permission;
    }

    /**
     * Gets all permissions registered by this plugin.
     *
     * @return the set of permission names
     */
    public Set<String> getRegisteredPermissions() {
        return new HashSet<>(registeredPermissions);
    }

    /**
     * Gets the permission checker for this plugin.
     *
     * @return the permission checker
     */
    public PermissionChecker getChecker() {
        return checker;
    }

    /**
     * Checks if a user has a permission, throwing an exception if not.
     *
     * @param userId     the user ID
     * @param permission the permission to check
     * @throws PermissionDeniedException if the user does not have the permission
     */
    public void checkPermission(String userId, String permission) throws PermissionDeniedException {
        checker.checkPermission(userId, permission);
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
        checker.checkPermission(userId, guildId, permission);
    }

    /**
     * Checks if a user has a permission, returning false if not.
     *
     * @param userId     the user ID
     * @param permission the permission to check
     * @return true if the user has the permission, false otherwise
     */
    public boolean hasPermission(String userId, String permission) {
        return checker.hasPermission(userId, permission);
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
        return checker.hasPermission(userId, guildId, permission);
    }
}
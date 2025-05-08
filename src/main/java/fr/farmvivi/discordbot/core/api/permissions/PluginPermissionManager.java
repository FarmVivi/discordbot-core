package fr.farmvivi.discordbot.core.api.permissions;

import fr.farmvivi.discordbot.core.api.language.LanguageManager;
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
     * Gets all permissions registered by this plugin.
     *
     * @return the set of permission names
     */
    public Set<String> getRegisteredPermissions() {
        return new HashSet<>(registeredPermissions);
    }
}
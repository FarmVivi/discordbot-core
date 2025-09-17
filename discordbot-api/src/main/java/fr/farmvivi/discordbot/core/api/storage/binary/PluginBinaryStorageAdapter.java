package fr.farmvivi.discordbot.core.api.storage.binary;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;

/**
 * Adapter for plugin-specific binary storage operations.
 * Automatically handles namespacing and scoping for the plugin.
 */
public class PluginBinaryStorageAdapter {
    private final Plugin plugin;
    private final BinaryStorageManager storageManager;
    private final String namespace;

    /**
     * Creates a new plugin binary storage adapter.
     *
     * @param plugin         the plugin
     * @param storageManager the binary storage manager
     */
    public PluginBinaryStorageAdapter(Plugin plugin, BinaryStorageManager storageManager) {
        this.plugin = plugin;
        this.storageManager = storageManager;
        this.namespace = plugin.getName().toLowerCase();
    }

    /**
     * Gets the global binary storage context for this plugin.
     * Paths are automatically namespaced with the plugin name.
     *
     * @return the plugin's global binary storage
     */
    public PluginGlobalBinaryStorage getGlobalStorage() {
        return new PluginGlobalBinaryStorage(storageManager.getGlobalStorage(), namespace);
    }

    /**
     * Gets the user binary storage context for this plugin.
     * Paths are automatically namespaced with the plugin name.
     *
     * @param userId the user ID
     * @return the plugin's user binary storage
     */
    public PluginUserBinaryStorage getUserStorage(String userId) {
        return new PluginUserBinaryStorage(storageManager.getUserStorage(userId), namespace);
    }

    /**
     * Gets the guild binary storage context for this plugin.
     * Paths are automatically namespaced with the plugin name.
     *
     * @param guildId the guild ID
     * @return the plugin's guild binary storage
     */
    public PluginGuildBinaryStorage getGuildStorage(String guildId) {
        return new PluginGuildBinaryStorage(storageManager.getGuildStorage(guildId), namespace);
    }

    /**
     * Gets the user-guild binary storage context for this plugin.
     * Paths are automatically namespaced with the plugin name.
     *
     * @param userId  the user ID
     * @param guildId the guild ID
     * @return the plugin's user-guild binary storage
     */
    public PluginUserGuildBinaryStorage getUserGuildStorage(String userId, String guildId) {
        return new PluginUserGuildBinaryStorage(storageManager.getUserGuildStorage(userId, guildId), namespace);
    }
}
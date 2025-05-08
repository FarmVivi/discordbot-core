package fr.farmvivi.discordbot.core.api.storage;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;

/**
 * Adapter for plugin-specific data storage operations.
 * Automatically handles namespacing and scoping for the plugin.
 */
public class PluginDataStorageAdapter {
    private final Plugin plugin;
    private final DataStorageManager storageManager;
    private final String namespace;

    /**
     * Creates a new plugin data storage adapter.
     *
     * @param plugin         the plugin
     * @param storageManager the data storage manager
     */
    public PluginDataStorageAdapter(Plugin plugin, DataStorageManager storageManager) {
        this.plugin = plugin;
        this.storageManager = storageManager;
        this.namespace = plugin.getName().toLowerCase();
    }

    /**
     * Gets the global storage context for this plugin.
     * Keys are automatically namespaced with the plugin name.
     *
     * @return the plugin's global storage
     */
    public PluginGlobalStorage getGlobalStorage() {
        return new PluginGlobalStorage(storageManager.getGlobalStorage(), namespace);
    }

    /**
     * Gets the user storage context for this plugin.
     * Keys are automatically namespaced with the plugin name.
     *
     * @param userId the user ID
     * @return the plugin's user storage
     */
    public PluginUserStorage getUserStorage(String userId) {
        return new PluginUserStorage(storageManager.getUserStorage(userId), namespace);
    }

    /**
     * Gets the guild storage context for this plugin.
     * Keys are automatically namespaced with the plugin name.
     *
     * @param guildId the guild ID
     * @return the plugin's guild storage
     */
    public PluginGuildStorage getGuildStorage(String guildId) {
        return new PluginGuildStorage(storageManager.getGuildStorage(guildId), namespace);
    }

    /**
     * Gets the user-guild storage context for this plugin.
     * Keys are automatically namespaced with the plugin name.
     *
     * @param userId  the user ID
     * @param guildId the guild ID
     * @return the plugin's user-guild storage
     */
    public PluginUserGuildStorage getUserGuildStorage(String userId, String guildId) {
        return new PluginUserGuildStorage(storageManager.getUserGuildStorage(userId, guildId), namespace);
    }

    /**
     * Saves all pending changes.
     *
     * @return true if the operation was successful
     */
    public boolean saveAll() {
        return storageManager.saveAll();
    }
}
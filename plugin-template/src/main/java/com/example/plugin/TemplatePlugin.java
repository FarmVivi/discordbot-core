package com.example.plugin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.entities.emoji.Emoji;

/**
 * Template plugin demonstrating the structure and features available to DiscordBot Core plugins.
 * 
 * <p>This plugin serves as a starting point for plugin development and showcases:
 * <ul>
 *   <li>Plugin lifecycle management (onEnable/onDisable)</li>
 *   <li>Configuration system usage</li>
 *   <li>Event handling with Discord events</li>
 *   <li>Internationalization support</li>
 *   <li>Data storage and persistence</li>
 *   <li>Permission management</li>
 * </ul>
 * 
 * <p>To create your own plugin:
 * <ol>
 *   <li>Copy this template directory</li>
 *   <li>Rename the package and class</li>
 *   <li>Update the plugin.yml with your plugin details</li>
 *   <li>Implement your plugin functionality</li>
 *   <li>Build with: mvn clean package</li>
 *   <li>Copy the JAR to the plugins/ directory</li>
 * </ol>
 * 
 * <p><strong>Note:</strong> Plugin metadata (name, version, etc.) is automatically loaded from 
 * the plugin.yml file, so you don't need to implement getName() and getVersion() methods.
 * 
 * @author YourName
 * @version 1.0.0
 * @since 1.0.0
 * @see fr.farmvivi.discordbot.core.api.plugin.AbstractPlugin
 */
public class TemplatePlugin /* extends AbstractPlugin */ {

    /**
     * Gets the name of the plugin.
     * 
     * <p><strong>Note:</strong> In future versions of DiscordBot Core, this method
     * will not be required as the plugin name will be automatically loaded from
     * the plugin.yml file.
     * 
     * @return the plugin's name
     */
    public String getName() {
        return "TemplatePlugin";
    }

    /**
     * Gets the version of the plugin.
     * 
     * <p><strong>Note:</strong> In future versions of DiscordBot Core, this method
     * will not be required as the plugin version will be automatically loaded from
     * the plugin.yml file.
     * 
     * @return the plugin's version string
     */
    public String getVersion() {
        return "1.0.0";
    }

    /**
     * Called when the plugin is enabled.
     * This is where you should initialize your plugin, load configuration,
     * register permissions, and set up event handlers.
     * 
     * <p>When using the full DiscordBot Core API, add the @Override annotation
     * and extend AbstractPlugin to access:
     * <ul>
     *   <li>Configuration API: getConfiguration()</li>
     *   <li>Storage API: getPluginDataStorage()</li>
     *   <li>Language API: getPluginLanguageAdapter()</li>
     *   <li>Permission API: getPluginPermissionManager()</li>
     *   <li>Logger: logger field</li>
     * </ul>
     */
    public void onEnable() {
        // Load configuration settings
        loadConfiguration();
        
        // Log startup message (replace with logger.info when extending AbstractPlugin)
        System.out.println("Template Plugin enabled!");
        
        // TODO: When extending AbstractPlugin, uncomment these:
        // getPluginLanguageAdapter().registerNamespace("template");
        // registerPermissions();
        // initializeServices();
    }

    /**
     * Called when the plugin is disabled.
     * This is where you should save data, clean up resources, and perform shutdown tasks.
     * 
     * <p>Add @Override annotation when extending AbstractPlugin.
     */
    public void onDisable() {
        // Save any pending data
        savePluginData();
        
        // Log shutdown message (replace with logger.info when extending AbstractPlugin)
        System.out.println("Template Plugin disabled!");
    }

    /**
     * Example event handler that demonstrates how to handle Discord events.
     * This method will be called whenever a message is received in any channel
     * the bot has access to.
     * 
     * <p>Add @EventHandler annotation when extending AbstractPlugin.
     * 
     * @param event the message received event
     */
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignore bot messages to prevent infinite loops
        if (event.getAuthor().isBot()) {
            return;
        }
        
        // Example: React to messages containing "template"
        String content = event.getMessage().getContentRaw().toLowerCase();
        if (content.contains("template")) {
            event.getMessage().addReaction(Emoji.fromUnicode("🤖")).queue();
        }
        
        // Example: Log messages (replace with logger.debug when extending AbstractPlugin)
        System.out.println("Message received: " + event.getMessage().getContentDisplay() + 
                          " from " + event.getAuthor().getEffectiveName());
    }

    /**
     * Loads configuration values with defaults.
     * This demonstrates how to access plugin configuration.
     * 
     * <p>When extending AbstractPlugin, replace with actual configuration API calls.
     */
    private void loadConfiguration() {
        // TODO: When extending AbstractPlugin, replace with:
        // boolean enabled = getConfiguration().getBoolean("example.enabled", true);
        // String setting = getConfiguration().getString("example.setting", "default_value");
        // int number = getConfiguration().getInt("example.number", 42);
        
        System.out.println("Configuration loaded - implement with proper API when extending AbstractPlugin");
    }

    /**
     * Initializes plugin services and components.
     * Add your plugin-specific initialization logic here.
     */
    private void initializeServices() {
        // TODO: Initialize your plugin services
        // Example: database connections, schedulers, caches, etc.
        
        System.out.println("Plugin services initialized");
    }

    /**
     * Registers plugin-specific permissions.
     * This demonstrates how to register permissions that can be used
     * to control access to plugin features.
     */
    private void registerPermissions() {
        // TODO: When extending AbstractPlugin, replace with:
        // getPluginPermissionManager().registerPermission("template.use", PermissionDefault.TRUE);
        // getPluginPermissionManager().registerPermission("template.admin", PermissionDefault.OPERATOR);
        
        System.out.println("Plugin permissions registered");
    }

    /**
     * Saves plugin data to persistent storage.
     * This demonstrates how to use the storage API for data persistence.
     */
    private void savePluginData() {
        // TODO: When extending AbstractPlugin, replace with:
        // getPluginDataStorage().set("plugin.lastShutdown", System.currentTimeMillis());
        
        System.out.println("Plugin data saved");
    }

    /**
     * Example of using the storage API to save player data.
     * 
     * @param playerId the player's unique identifier
     * @param data the data to save
     */
    private void savePlayerData(String playerId, String data) {
        // TODO: When extending AbstractPlugin, replace with:
        // getPluginDataStorage().set("players." + playerId + ".data", data);
        
        System.out.println("Saved data for player: " + playerId);
    }

    /**
     * Example of using the storage API to retrieve player data.
     * 
     * @param playerId the player's unique identifier
     * @return the player's data, or null if not found
     */
    private String getPlayerData(String playerId) {
        // TODO: When extending AbstractPlugin, replace with:
        // return getPluginDataStorage().getString("players." + playerId + ".data", null);
        
        System.out.println("Retrieved data for player: " + playerId);
        return null;
    }

    /**
     * Example of checking user permissions.
     * 
     * @param userId the user's Discord ID
     * @param permission the permission to check
     * @return true if the user has the permission
     */
    private boolean hasPermission(String userId, String permission) {
        // TODO: When extending AbstractPlugin, replace with:
        // return getPluginPermissionManager().hasPermission(user, permission);
        
        System.out.println("Checking permission: " + permission + " for user: " + userId);
        return true; // Default for template
    }

    /**
     * Example of sending a localized message.
     * 
     * @param guildId the guild ID for language context
     * @param key the message key
     * @param defaultValue the default value if translation is not found
     * @return the localized message
     */
    private String getLocalizedMessage(String guildId, String key, String defaultValue) {
        // TODO: When extending AbstractPlugin, replace with:
        // return getPluginLanguageAdapter().getString(guild, key, defaultValue);
        
        System.out.println("Getting localized message: " + key);
        return defaultValue; // Default for template
    }
}
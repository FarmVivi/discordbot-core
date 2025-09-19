package com.example.plugin;

import fr.farmvivi.discordbot.api.event.EventHandler;
import fr.farmvivi.discordbot.api.event.EventPriority;
import fr.farmvivi.discordbot.api.permissions.Permission;
import fr.farmvivi.discordbot.api.permissions.PermissionDefault;
import fr.farmvivi.discordbot.api.plugin.AbstractPlugin;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
 * @see fr.farmvivi.discordbot.api.plugin.AbstractPlugin
 * @since 1.0.0
 */
public class TemplatePlugin extends AbstractPlugin {

    /**
     * Called when the plugin is enabled.
     * This is where you should initialize your plugin, load configuration,
     * register permissions, and set up event handlers.
     */
    @Override
    public void onEnable() {
        // Register permissions
        registerPermissions();

        // Load configuration settings
        loadConfiguration();

        // Initialize services (if any)
        initializeServices();

        logger.info("Template Plugin enabled");
    }

    /**
     * Called when the plugin is disabled.
     * This is where you should save data, clean up resources, and perform shutdown tasks.
     */
    @Override
    public void onDisable() {
        savePluginData();
        logger.info("Template Plugin disabled");
    }

    /**
     * Example event handler that demonstrates how to handle Discord events.
     * This method will be called whenever a message is received in any channel
     * the bot has access to.
     *
     * @param event the message received event
     */
    @EventHandler(priority = EventPriority.NORMAL)
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

        // Example: Log messages
        logger.debug("Message received: {} from {}", event.getMessage().getContentDisplay(), event.getAuthor().getEffectiveName());
    }

    /**
     * Loads configuration values with defaults.
     * This demonstrates how to access plugin configuration.
     */
    private void loadConfiguration() {
        boolean enabled = getConfiguration().getBoolean("example.enabled", true);
        String setting = getConfiguration().getString("example.setting", "default_value");
        int number = getConfiguration().getInt("example.number", 42);
        logger.info("Config loaded: enabled={}, setting={}, number={}", enabled, setting, number);
    }

    /**
     * Initializes plugin services and components.
     * Add your plugin-specific initialization logic here.
     */
    private void initializeServices() {
        // Initialize plugin services (schedulers, caches, etc.)
        logger.debug("Services initialized");
    }

    /**
     * Registers plugin-specific permissions.
     * This demonstrates how to register permissions that can be used
     * to control access to plugin features.
     */
    private void registerPermissions() {
        getPluginPermissionManager().registerPermission(new SimplePermission(
                pluginPrefix("use"),
                "Allows usage of base template features",
                PermissionDefault.TRUE));
        getPluginPermissionManager().registerPermission(new SimplePermission(
                pluginPrefix("admin"),
                "Allows administrative template actions",
                PermissionDefault.OP));
        logger.debug("Permissions registered: {}", getPluginPermissionManager().getRegisteredPermissions());
    }

    /**
     * Saves plugin data to persistent storage.
     * This demonstrates how to use the storage API for data persistence.
     */
    private void savePluginData() {
        getPluginDataStorage().getGlobalStorage().set("lastShutdown", System.currentTimeMillis());
        getPluginDataStorage().saveAll();
        logger.debug("Plugin data saved (global.lastShutdown)");
    }

    /**
     * Example of using the storage API to save player data.
     *
     * @param playerId the player's unique identifier
     * @param data     the data to save
     */
    private void savePlayerData(String playerId, String data) {
        getPluginDataStorage().getUserStorage(playerId).set("data", data);
        logger.trace("Saved data for player {}", playerId);
    }

    /**
     * Example of using the storage API to retrieve player data.
     *
     * @param playerId the player's unique identifier
     * @return the player's data, or null if not found
     */
    private String getPlayerData(String playerId) {
        return getPluginDataStorage().getUserStorage(playerId)
                .get("data", String.class)
                .orElse(null);
    }

    /**
     * Example of checking user permissions.
     *
     * @param userId     the user's Discord ID
     * @param permission the permission to check
     * @return true if the user has the permission
     */
    private boolean hasPermission(String userId, String permission) {
        // Placeholder: utilisation d'un système externe requis pour résolution utilisateur.
        // Ici on vérifie seulement si la permission est enregistrée.
        return getPluginPermissionManager().getRegisteredPermissions().contains(permission);
    }

    /**
     * Example of sending a localized message.
     *
     * @param guildId      the guild ID for language context
     * @param key          the message key
     * @param defaultValue the default value if translation is not found
     * @return the localized message
     */
    private String getLocalizedMessage(String guildId, String key, String defaultValue) {
        // PluginLanguageAdapter gère déjà le préfixe namespace. Ici uniquement clé simple.
        return getPluginLanguageManager().getString(key);
    }

    private String pluginPrefix(String node) {
        return getName().toLowerCase() + "." + node;
    }

    // Simple internal Permission implementation for template usage
    private record SimplePermission(String name, String description,
                                    PermissionDefault getDefault) implements Permission {
        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }
}
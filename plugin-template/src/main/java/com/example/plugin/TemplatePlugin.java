package com.example.plugin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.entities.emoji.Emoji;

/**
 * Template plugin demonstrating the basic structure for
 * DiscordBot Core plugins.
 * 
 * To create your own plugin:
 * 1. Copy this template directory
 * 2. Rename the package and class
 * 3. Update the pom.xml with your plugin details
 * 4. Implement your plugin functionality
 * 5. Build with: mvn clean package
 * 6. Copy the JAR to the plugins/ directory
 * 
 * Note: This template shows the basic structure. 
 * Once you add the discordbot-core dependency, you can extend
 * AbstractPlugin and use the full API.
 */
public class TemplatePlugin {

    public String getName() {
        // TODO: Change this to your plugin name
        return "TemplatePlugin";
    }

    public String getVersion() {
        // TODO: Update version as you develop
        return "1.0.0";
    }

    public void onEnable() {
        // Called when the plugin is enabled
        System.out.println("Template Plugin enabled! Replace this with your initialization code.");
        
        // TODO: When using discordbot-core dependency:
        // - Register configuration values
        // - Setup plugin-specific permissions
        // - Register event handlers
        // - Initialize your plugin services
    }

    public void onDisable() {
        // Called when the plugin is disabled
        System.out.println("Template Plugin disabled! Add cleanup code here.");
        
        // TODO: When using discordbot-core dependency:
        // - Save any pending data
        // - Close any open connections
        // - Clean up resources
    }

    /**
     * Example event handler structure
     * TODO: Add @EventHandler annotation when using discordbot-core dependency
     */
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignore bot messages
        if (event.getAuthor().isBot()) {
            return;
        }
        
        // Example: React to messages containing "template"
        String content = event.getMessage().getContentRaw().toLowerCase();
        if (content.contains("template")) {
            event.getMessage().addReaction(Emoji.fromUnicode("🤖")).queue();
        }
        
        // Example: Log all messages (remove in production)
        System.out.println("Message received: " + event.getMessage().getContentDisplay() + 
                          " from " + event.getAuthor().getEffectiveName());
    }

    /**
     * Example of configuration usage
     * TODO: Use proper configuration API when available
     */
    private void loadConfiguration() {
        // TODO: Replace with actual configuration API
        // String setting = getConfiguration().getString("example.setting", "default_value");
        // int number = getConfiguration().getInt("example.number", 42);
        // boolean enabled = getConfiguration().getBoolean("example.enabled", true);
        
        System.out.println("Configuration loaded - implement with proper API");
    }

    /**
     * Example of data storage
     * TODO: Use proper storage API when available
     */
    private void savePlayerData(String playerId, String data) {
        // TODO: Replace with actual storage API
        // getPluginDataStorage().set("players." + playerId + ".data", data);
        
        System.out.println("Saved data for player: " + playerId + " - implement with proper API");
    }

    private String getPlayerData(String playerId) {
        // TODO: Replace with actual storage API
        // return getPluginDataStorage().getString("players." + playerId + ".data", null);
        
        System.out.println("Retrieved data for player: " + playerId + " - implement with proper API");
        return null;
    }
}
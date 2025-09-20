package com.example.plugin.events;

import fr.farmvivi.fluxcord.api.event.EventHandler;
import fr.farmvivi.fluxcord.api.event.EventPriority;
import fr.farmvivi.fluxcord.api.plugin.AbstractPlugin;
import fr.farmvivi.fluxcord.api.plugin.events.PluginEnableEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Example event listener demonstrating Discord event handling.
 * This listener can be enabled/disabled via configuration.
 */
public class ExampleEventListener {

    private final AbstractPlugin plugin;

    public ExampleEventListener(AbstractPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Example Discord message event handler.
     * Demonstrates event handling with configuration checks.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMessageReceived(MessageReceivedEvent event) {
        // Skip bot messages
        if (event.getAuthor().isBot()) {
            return;
        }

        // Check if event handling is enabled
        if (!plugin.getConfiguration().getBoolean("events.enabled", true)) {
            return;
        }

        // Example: Log messages in debug mode
        if (plugin.getConfiguration().getBoolean("debug.log_messages", false)) {
            plugin.getLogger().debug("Message received from {}: {}",
                    event.getAuthor().getAsTag(),
                    event.getMessage().getContentRaw());
        }

        // Example: Respond to mentions (if enabled)
        if (event.getMessage().getMentions().isMentioned(event.getJDA().getSelfUser()) &&
                plugin.getConfiguration().getBoolean("features.respond_to_mentions", false)) {

            String response = plugin.getPluginLanguageManager()
                    .getString("messages.mention_response", event.getAuthor().getAsMention());

            event.getChannel().sendMessage(response).queue();
        }
    }

    /**
     * Example plugin event handler.
     * Demonstrates handling internal plugin events.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPluginEvent(PluginEnableEvent event) {
        // React to other plugins being enabled
        if (plugin.getConfiguration().getBoolean("debug.log_plugin_events", false)) {
            plugin.getLogger().info("Plugin enabled: {}", event.getPlugin().getName());
        }
    }
}
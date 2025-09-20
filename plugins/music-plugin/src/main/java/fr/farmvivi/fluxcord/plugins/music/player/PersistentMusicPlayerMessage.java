package fr.farmvivi.fluxcord.plugins.music.player;

import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;

import java.util.Map;
import java.util.HashMap;

/**
 * Manages persistent music player messages with interactive controls.
 * These messages show current playing track, queue, and provide buttons for control.
 * Messages persist across bot restarts by storing message/channel IDs.
 */
public class PersistentMusicPlayerMessage {
    private final MusicPlugin plugin;
    private final String guildId;
    
    // Persistent data
    private String channelId;
    private String messageId;
    private boolean enabled = false;
    
    public PersistentMusicPlayerMessage(MusicPlugin plugin, String guildId) {
        this.plugin = plugin;
        this.guildId = guildId;
    }
    
    /**
     * Refresh the music player message with current state.
     */
    public void refresh() {
        // TODO: Implement message refresh with current track info and buttons
        plugin.getLogger().debug("Refreshing persistent player message for guild: {}", guildId);
    }
    
    /**
     * Enable persistent message in the specified channel.
     *
     * @param channelId the channel ID where to create/update the message
     */
    public void enable(String channelId) {
        this.channelId = channelId;
        this.enabled = true;
        refresh();
    }
    
    /**
     * Disable the persistent message.
     */
    public void disable() {
        this.enabled = false;
        // TODO: Delete existing message if present
    }
    
    /**
     * Load persistent message data from storage.
     *
     * @param data the stored data
     */
    public void loadFromData(Map<String, Object> data) {
        this.channelId = (String) data.get("channelId");
        this.messageId = (String) data.get("messageId");
        this.enabled = Boolean.TRUE.equals(data.get("enabled"));
        
        plugin.getLogger().debug("Loaded persistent message data for guild {}: channel={}, message={}, enabled={}", 
            guildId, channelId, messageId, enabled);
    }
    
    /**
     * Save persistent message data to storage.
     *
     * @return the data to store
     */
    public Map<String, Object> saveToData() {
        Map<String, Object> data = new HashMap<>();
        data.put("channelId", channelId);
        data.put("messageId", messageId);
        data.put("enabled", enabled);
        return data;
    }
    
    // Getters and setters
    public String getGuildId() {
        return guildId;
    }
    
    public String getChannelId() {
        return channelId;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
}
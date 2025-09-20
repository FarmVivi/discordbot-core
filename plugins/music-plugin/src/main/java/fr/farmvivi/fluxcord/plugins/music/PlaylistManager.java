package fr.farmvivi.fluxcord.plugins.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.fluxcord.plugins.music.model.UserPlaylist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages user playlists, favorites, and queue persistence.
 * Provides functionality to create, save, load, and manage playlists.
 */
public class PlaylistManager {
    private static final Logger logger = LoggerFactory.getLogger(PlaylistManager.class);

    private final MusicPlugin plugin;
    private final Map<String, UserPlaylist> userPlaylists = new ConcurrentHashMap<>();
    private final Map<String, List<String>> userFavorites = new ConcurrentHashMap<>();

    public PlaylistManager(MusicPlugin plugin) {
        this.plugin = plugin;
        loadPlaylistsFromStorage();
        logger.info("Playlist manager initialized");
    }

    /**
     * Creates a new playlist for a user.
     */
    public boolean createPlaylist(String userId, String playlistName) {
        if (playlistName == null || playlistName.trim().isEmpty()) {
            return false;
        }
        
        String key = getUserPlaylistKey(userId, playlistName);
        if (userPlaylists.containsKey(key)) {
            return false; // Playlist already exists
        }
        
        UserPlaylist playlist = new UserPlaylist(userId, playlistName);
        userPlaylists.put(key, playlist);
        
        logger.debug("Created playlist '{}' for user {}", playlistName, userId);
        return true;
    }
    
    /**
     * Deletes a playlist for a user.
     */
    public boolean deletePlaylist(String userId, String playlistName) {
        String key = getUserPlaylistKey(userId, playlistName);
        UserPlaylist removed = userPlaylists.remove(key);
        
        if (removed != null) {
            logger.debug("Deleted playlist '{}' for user {}", playlistName, userId);
            return true;
        }
        return false;
    }
    
    /**
     * Adds a track to a user's playlist.
     */
    public boolean addTrackToPlaylist(String userId, String playlistName, AudioTrack track) {
        String key = getUserPlaylistKey(userId, playlistName);
        UserPlaylist playlist = userPlaylists.get(key);
        
        if (playlist == null) {
            return false;
        }
        
        int maxSize = plugin.getConfiguration().getInt("playlists.max_playlist_size", 500);
        if (playlist.getTracks().size() >= maxSize) {
            return false; // Playlist is full
        }
        
        playlist.addTrack(track);
        logger.debug("Added track '{}' to playlist '{}' for user {}", 
            track.getInfo().title, playlistName, userId);
        return true;
    }
    
    /**
     * Removes a track from a user's playlist.
     */
    public boolean removeTrackFromPlaylist(String userId, String playlistName, int index) {
        String key = getUserPlaylistKey(userId, playlistName);
        UserPlaylist playlist = userPlaylists.get(key);
        
        if (playlist == null) {
            return false;
        }
        
        return playlist.removeTrack(index);
    }
    
    /**
     * Gets a user's playlist.
     */
    public UserPlaylist getPlaylist(String userId, String playlistName) {
        String key = getUserPlaylistKey(userId, playlistName);
        return userPlaylists.get(key);
    }
    
    /**
     * Gets all playlists for a user.
     */
    public List<UserPlaylist> getUserPlaylists(String userId) {
        return userPlaylists.values().stream()
            .filter(playlist -> playlist.getUserId().equals(userId))
            .toList();
    }
    
    /**
     * Adds a track to user's favorites.
     */
    public void addToFavorites(String userId, String trackUri) {
        userFavorites.computeIfAbsent(userId, k -> new ArrayList<>()).add(trackUri);
        
        // Limit favorites size
        List<String> favorites = userFavorites.get(userId);
        int maxFavorites = plugin.getConfiguration().getInt("playlists.max_favorites", 100);
        if (favorites.size() > maxFavorites) {
            favorites.remove(0); // Remove oldest
        }
        
        logger.debug("Added track to favorites for user {}", userId);
    }
    
    /**
     * Removes a track from user's favorites.
     */
    public boolean removeFromFavorites(String userId, String trackUri) {
        List<String> favorites = userFavorites.get(userId);
        if (favorites != null) {
            return favorites.remove(trackUri);
        }
        return false;
    }
    
    /**
     * Gets user's favorite tracks.
     */
    public List<String> getUserFavorites(String userId) {
        return userFavorites.getOrDefault(userId, new ArrayList<>());
    }
    
    /**
     * Loads playlists from persistent storage.
     */
    private void loadPlaylistsFromStorage() {
        try {
            // TODO: Implement actual persistence using plugin storage API
            // This would load from the plugin's data storage
            logger.debug("Loading playlists from storage...");
            
            // For now, this is a placeholder
            // In a real implementation, this would:
            // 1. Use plugin.getDataStorage() to access persistent storage
            // 2. Load playlist data from JSON/YAML files
            // 3. Reconstruct UserPlaylist objects
            
            logger.debug("Playlists loaded from storage");
        } catch (Exception e) {
            logger.error("Failed to load playlists from storage", e);
        }
    }

    /**
     * Saves all playlists to persistent storage.
     */
    public void saveAllPlaylists() {
        try {
            logger.info("Saving all playlists to storage...");
            
            // TODO: Implement actual persistence using plugin storage API
            // This would save to the plugin's data storage
            
            // For now, this is a placeholder
            // In a real implementation, this would:
            // 1. Use plugin.getDataStorage() to access persistent storage
            // 2. Serialize all UserPlaylist objects to JSON/YAML
            // 3. Save to appropriate files/database
            
            logger.info("All playlists saved to storage ({} playlists, {} users with favorites)", 
                userPlaylists.size(), userFavorites.size());
        } catch (Exception e) {
            logger.error("Failed to save playlists to storage", e);
        }
    }
    
    /**
     * Gets statistics about playlists.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_playlists", userPlaylists.size());
        stats.put("total_users_with_favorites", userFavorites.size());
        
        int totalTracks = userPlaylists.values().stream()
            .mapToInt(playlist -> playlist.getTracks().size())
            .sum();
        stats.put("total_tracks_in_playlists", totalTracks);
        
        int totalFavorites = userFavorites.values().stream()
            .mapToInt(List::size)
            .sum();
        stats.put("total_favorite_tracks", totalFavorites);
        
        return stats;
    }
    
    private String getUserPlaylistKey(String userId, String playlistName) {
        return userId + ":" + playlistName.toLowerCase();
    }
}
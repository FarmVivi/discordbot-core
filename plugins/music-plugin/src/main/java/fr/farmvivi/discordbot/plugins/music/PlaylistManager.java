package fr.farmvivi.discordbot.plugins.music;

/**
 * Manages user playlists, favorites, and queue persistence.
 */
public class PlaylistManager {

    private final MusicPlugin plugin;

    public PlaylistManager(MusicPlugin plugin) {
        this.plugin = plugin;
        // TODO: Load existing playlists from storage
    }

    public void saveAllPlaylists() {
        // TODO: Save all playlists to persistent storage
    }
}
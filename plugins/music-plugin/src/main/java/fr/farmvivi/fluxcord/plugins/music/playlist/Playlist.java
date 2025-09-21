package fr.farmvivi.fluxcord.plugins.music.playlist;

import java.util.*;

/**
 * Represents a music playlist.
 */
public class Playlist {
    private final String name;
    private final String ownerId;
    private final boolean isGuildPlaylist;
    private final List<PlaylistTrack> tracks;
    private final long createdAt;
    private long updatedAt;

    public Playlist(String name, String ownerId, boolean isGuildPlaylist) {
        this.name = name;
        this.ownerId = ownerId;
        this.isGuildPlaylist = isGuildPlaylist;
        this.tracks = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = createdAt;
    }

    /**
     * Creates a playlist from a stored map.
     */
    public static Playlist fromMap(Map<String, Object> map) {
        String name = (String) map.get("name");
        String ownerId = (String) map.get("ownerId");
        boolean isGuildPlaylist = (Boolean) map.get("isGuildPlaylist");

        Playlist playlist = new Playlist(name, ownerId, isGuildPlaylist);

        // Restore timestamps
        if (map.containsKey("createdAt")) {
            // Use reflection or make fields package-private if needed
        }
        if (map.containsKey("updatedAt")) {
            playlist.updatedAt = ((Number) map.get("updatedAt")).longValue();
        }

        // Restore tracks
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trackList = (List<Map<String, Object>>) map.get("tracks");
        if (trackList != null) {
            for (Map<String, Object> trackMap : trackList) {
                playlist.tracks.add(PlaylistTrack.fromMap(trackMap));
            }
        }

        return playlist;
    }

    /**
     * Adds a track to the playlist.
     */
    public void addTrack(String url, String title, String author, long duration) {
        tracks.add(new PlaylistTrack(url, title, author, duration));
        updatedAt = System.currentTimeMillis();
    }

    /**
     * Removes a track at the specified index.
     */
    public boolean removeTrack(int index) {
        if (index < 0 || index >= tracks.size()) {
            return false;
        }
        tracks.remove(index);
        updatedAt = System.currentTimeMillis();
        return true;
    }

    /**
     * Clears all tracks from the playlist.
     */
    public void clear() {
        tracks.clear();
        updatedAt = System.currentTimeMillis();
    }

    /**
     * Converts the playlist to a map for storage.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("ownerId", ownerId);
        map.put("isGuildPlaylist", isGuildPlaylist);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);

        List<Map<String, Object>> trackList = new ArrayList<>();
        for (PlaylistTrack track : tracks) {
            trackList.add(track.toMap());
        }
        map.put("tracks", trackList);

        return map;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public boolean isGuildPlaylist() {
        return isGuildPlaylist;
    }

    public List<PlaylistTrack> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    public int getTrackCount() {
        return tracks.size();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Represents a track in a playlist.
     */
    public static class PlaylistTrack {
        private final String url;
        private final String title;
        private final String author;
        private final long duration;

        public PlaylistTrack(String url, String title, String author, long duration) {
            this.url = url;
            this.title = title;
            this.author = author;
            this.duration = duration;
        }

        public static PlaylistTrack fromMap(Map<String, Object> map) {
            return new PlaylistTrack(
                    (String) map.get("url"),
                    (String) map.get("title"),
                    (String) map.get("author"),
                    ((Number) map.get("duration")).longValue()
            );
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("url", url);
            map.put("title", title);
            map.put("author", author);
            map.put("duration", duration);
            return map;
        }

        // Getters
        public String getUrl() {
            return url;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public long getDuration() {
            return duration;
        }
    }
}
package fr.farmvivi.fluxcord.plugins.music.model;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user's music playlist.
 */
public class UserPlaylist {
    private final String userId;
    private final String name;
    private final List<AudioTrack> tracks;
    private final Instant createdAt;
    private Instant lastModified;
    private boolean isPublic;
    private String description;

    public UserPlaylist(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.tracks = new ArrayList<>();
        this.createdAt = Instant.now();
        this.lastModified = Instant.now();
        this.isPublic = false;
        this.description = "";
    }

    public void addTrack(AudioTrack track) {
        tracks.add(track);
        lastModified = Instant.now();
    }

    public boolean removeTrack(int index) {
        if (index >= 0 && index < tracks.size()) {
            tracks.remove(index);
            lastModified = Instant.now();
            return true;
        }
        return false;
    }

    public void clearTracks() {
        tracks.clear();
        lastModified = Instant.now();
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public List<AudioTrack> getTracks() {
        return new ArrayList<>(tracks);
    }

    public int getSize() {
        return tracks.size();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        this.lastModified = Instant.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.lastModified = Instant.now();
    }

    public long getTotalDuration() {
        return tracks.stream()
            .mapToLong(track -> track.getDuration())
            .sum();
    }
}
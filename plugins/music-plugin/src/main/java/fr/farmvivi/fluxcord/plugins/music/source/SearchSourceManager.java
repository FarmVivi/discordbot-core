package fr.farmvivi.fluxcord.plugins.music.source;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Source manager that handles search queries by delegating to another source manager.
 */
public class SearchSourceManager implements AudioSourceManager {
    private final AudioSourceManager sourceManager;
    private final String searchPrefix;

    public SearchSourceManager(AudioSourceManager sourceManager, String searchPrefix) {
        this.sourceManager = sourceManager;
        this.searchPrefix = searchPrefix;
    }

    @Override
    public String getSourceName() {
        return "search:" + sourceManager.getSourceName();
    }

    @Override
    public AudioItem loadItem(com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager manager, AudioReference reference) {
        String identifier = reference.identifier;

        // If it doesn't start with a protocol (contains ://), treat it as a search
        if (!identifier.contains("://")) {
            identifier = searchPrefix + identifier;
            AudioReference searchReference = new AudioReference(identifier, reference.title);
            return sourceManager.loadItem(manager, searchReference);
        }

        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return sourceManager.isTrackEncodable(track);
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        sourceManager.encodeTrack(track, output);
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return sourceManager.decodeTrack(trackInfo, input);
    }

    @Override
    public void shutdown() {
        // Nothing to shut down
    }
}
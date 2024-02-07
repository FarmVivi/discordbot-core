package fr.farmvivi.discordbot.module.music.sourcemanager;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SearchSourceManager implements AudioSourceManager {
    private final AudioSourceManager otherAudioSourceManager;
    private final String search;

    public SearchSourceManager(AudioSourceManager otherAudioSourceManager, String search) {
        this.otherAudioSourceManager = otherAudioSourceManager;
        this.search = search;
    }

    @Override
    public String getSourceName() {
        return "search";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager audioPlayerManager, AudioReference audioReference) {
        AudioReference searchAudioReference = new AudioReference(search + audioReference.identifier, audioReference.title, audioReference.containerDescriptor);
        return otherAudioSourceManager.loadItem(audioPlayerManager, searchAudioReference);
    }

    @Override
    public boolean isTrackEncodable(AudioTrack audioTrack) {
        return otherAudioSourceManager.isTrackEncodable(audioTrack);
    }

    @Override
    public void encodeTrack(AudioTrack audioTrack, DataOutput dataOutput) throws IOException {
        otherAudioSourceManager.encodeTrack(audioTrack, dataOutput);
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo audioTrackInfo, DataInput dataInput) throws IOException {
        return otherAudioSourceManager.decodeTrack(audioTrackInfo, dataInput);
    }

    @Override
    public void shutdown() {
        otherAudioSourceManager.shutdown();
    }
}

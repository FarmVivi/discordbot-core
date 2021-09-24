package fr.farmvivi.animecity.spotify;

import java.io.IOException;
import java.util.ArrayList;

import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;

import org.apache.hc.core5.http.ParseException;

public class LinkConverter {
    private String id;
    private String type;

    public ArrayList<String> convert(String link) throws ParseException, SpotifyWebApiException, IOException {
        String[] firstSplit = link.split("/");
        String[] secondSplit;

        if (firstSplit.length > 5) {
            secondSplit = firstSplit[6].split("\\?");
            this.type = firstSplit[5];
        } else {
            secondSplit = firstSplit[4].split("\\?");
            this.type = firstSplit[3];
        }
        this.id = secondSplit[0];
        ArrayList<String> listOfTracks = new ArrayList<>();

        if (type.contentEquals("track")) {
            listOfTracks.add(getArtistAndName(id));
            return listOfTracks;
        }

        if (type.contentEquals("playlist")) {
            SpotifyManager.logger.info("Getting playlist tracks with id " + id);
            GetPlaylistRequest playlistRequest = SpotifyManager.getSpotifyApi().getPlaylist(id).build();
            Playlist playlist = playlistRequest.execute();
            Paging<PlaylistTrack> playlistPaging = playlist.getTracks();
            PlaylistTrack[] playlistTracks = playlistPaging.getItems();

            for (PlaylistTrack i : playlistTracks) {
                Track track = (Track) i.getTrack();
                listOfTracks.add(formatArtistAndName(track));
            }

            return listOfTracks;
        }

        return null;
    }

    private String getArtistAndName(String trackID) throws ParseException, SpotifyWebApiException, IOException {
        SpotifyManager.logger.info("Getting track with id " + trackID);
        GetTrackRequest trackRequest = SpotifyManager.getSpotifyApi().getTrack(trackID).build();
        Track track = trackRequest.execute();
        return formatArtistAndName(track);
    }

    private String formatArtistAndName(Track track) {
        String artistNameAndTrackName = track.getName() + " - ";
        ArtistSimplified[] artists = track.getArtists();
        for (ArtistSimplified i : artists)
            artistNameAndTrackName += i.getName() + " ";
        return artistNameAndTrackName;
    }
}
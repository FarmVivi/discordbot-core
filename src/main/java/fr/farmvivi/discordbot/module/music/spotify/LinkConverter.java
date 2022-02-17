package fr.farmvivi.discordbot.module.music.spotify;

import java.io.IOException;
import java.util.ArrayList;

import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import org.apache.hc.core5.http.ParseException;

public class LinkConverter {
    private String id;
    private String type;

    public ArrayList<String> convert(String link) throws ParseException, SpotifyWebApiException, IOException {
        final String[] firstSplit = link.split("/");
        final String[] secondSplit;

        if (firstSplit.length > 5) {
            secondSplit = firstSplit[6].split("\\?");
            this.type = firstSplit[5];
        } else {
            secondSplit = firstSplit[4].split("\\?");
            this.type = firstSplit[3];
        }
        this.id = secondSplit[0];
        final ArrayList<String> listOfTracks = new ArrayList<>();

        if (type.contentEquals("track")) {
            listOfTracks.add(getArtistAndName(id));
            return listOfTracks;
        }

        if (type.contentEquals("playlist")) {
            SpotifyManager.logger.info("Getting playlist tracks with id " + id);
            final GetPlaylistRequest playlistRequest = SpotifyManager.getSpotifyApi().getPlaylist(id).build();
            final Playlist playlist = playlistRequest.execute();
            final Paging<PlaylistTrack> playlistPaging = playlist.getTracks();
            final PlaylistTrack[] playlistTracks = playlistPaging.getItems();

            for (PlaylistTrack i : playlistTracks) {
                final Track track = (Track) i.getTrack();
                listOfTracks.add(formatArtistAndName(track));
            }

            return listOfTracks;
        }

        return null;
    }

    private String getArtistAndName(String trackID) throws ParseException, SpotifyWebApiException, IOException {
        SpotifyManager.logger.info("Getting track with id " + trackID);
        final GetTrackRequest trackRequest = SpotifyManager.getSpotifyApi().getTrack(trackID).build();
        final Track track = trackRequest.execute();
        return formatArtistAndName(track);
    }

    private String formatArtistAndName(Track track) {
        String artistNameAndTrackName = track.getName() + " - ";
        final ArtistSimplified[] artists = track.getArtists();
        for (ArtistSimplified i : artists)
            artistNameAndTrackName += i.getName() + " ";
        return artistNameAndTrackName;
    }
}
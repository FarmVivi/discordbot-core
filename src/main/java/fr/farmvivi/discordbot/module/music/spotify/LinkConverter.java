package fr.farmvivi.discordbot.module.music.spotify;

import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.util.ArrayList;

public class LinkConverter {
    public ArrayList<String> convert(String link) throws ParseException, SpotifyWebApiException, IOException {
        final String[] firstSplit = link.split("/");
        final String[] secondSplit;

        String type;
        if (firstSplit.length > 5) {
            secondSplit = firstSplit[6].split("\\?");
            type = firstSplit[5];
        } else {
            secondSplit = firstSplit[4].split("\\?");
            type = firstSplit[3];
        }
        String id = secondSplit[0];
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
        StringBuilder artistNameAndTrackName = new StringBuilder(track.getName() + " - ");
        final ArtistSimplified[] artists = track.getArtists();
        for (ArtistSimplified i : artists)
            artistNameAndTrackName.append(i.getName()).append(" ");
        return artistNameAndTrackName.toString();
    }
}
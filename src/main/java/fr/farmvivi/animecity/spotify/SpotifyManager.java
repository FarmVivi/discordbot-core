package fr.farmvivi.animecity.spotify;

import java.io.IOException;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.farmvivi.animecity.Bot;

public class SpotifyManager {
    public static final Logger logger = LoggerFactory.getLogger("Spotify");

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder().setClientId(Bot.SPOTIFY_CLIENT_ID)
            .setClientSecret(Bot.SPOTIFY_CLIENT_SECRET).build();

    static {
        logger.info("Connecting to Spotify...");
        try {
            final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
            final ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            // Set access token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error connecting to Spotify", e);
        }
    }

    public static SpotifyApi getSpotifyApi() {
        return spotifyApi;
    }
}

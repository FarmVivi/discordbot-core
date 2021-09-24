package fr.farmvivi.animecity.spotify;

import java.io.IOException;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotifyManager {
    public static final String CLIENT_ID = "029c2603ac3d4850ba355bbf4668649d";
    public static final String CLIENT_SECRET = "04a8b243a7234200bd3e6bf686907c6c";
    public static final Logger logger = LoggerFactory.getLogger("Spotify");

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder().setClientId(CLIENT_ID)
            .setClientSecret(CLIENT_SECRET).build();

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

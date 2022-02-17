package fr.farmvivi.discordbot.module.music.spotify;

import java.io.IOException;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.farmvivi.discordbot.Bot;

public class SpotifyManager {
    public static final Logger logger = LoggerFactory.getLogger(SpotifyManager.class);

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(Bot.getInstance().getConfiguration().spotifyId)
            .setClientSecret(Bot.getInstance().getConfiguration().spotifySecret).build();

    private static ClientCredentials clientCredentials;
    private static long renewedTime;

    public static synchronized SpotifyApi getSpotifyApi() {
        if (spotifyApi.getAccessToken() == null || timeToRenew())
            refreshToken();
        return spotifyApi;
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }

    public static boolean timeToRenew() {
        return renewedTime + clientCredentials.getExpiresIn() < getCurrentTime() - 10;
    }

    public static void refreshToken() {
        logger.info("Connecting to Spotify...");
        try {
            final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
            clientCredentials = clientCredentialsRequest.execute();
            renewedTime = getCurrentTime();

            // Set access token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error connecting to Spotify", e);
        }
    }
}

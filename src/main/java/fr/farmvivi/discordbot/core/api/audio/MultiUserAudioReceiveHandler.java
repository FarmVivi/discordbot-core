package fr.farmvivi.discordbot.core.api.audio;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.entities.User;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * An audio receive handler that can process audio from multiple users.
 * This handler is useful for more advanced audio processing.
 */
public interface MultiUserAudioReceiveHandler extends AudioReceiveHandler {
    /**
     * Sets a handler for combined audio.
     *
     * @param handler The handler for combined audio
     */
    void setCombinedAudioHandler(Consumer<byte[]> handler);

    /**
     * Gets the combined audio handler.
     *
     * @return The combined audio handler, or null if none is set
     */
    Consumer<byte[]> getCombinedAudioHandler();

    /**
     * Adds a handler for a specific user's audio.
     *
     * @param userId The user ID
     * @param handler The handler for the user's audio
     */
    void addUserAudioHandler(String userId, Consumer<byte[]> handler);

    /**
     * Removes a handler for a specific user's audio.
     *
     * @param userId The user ID
     * @return true if a handler was removed, false otherwise
     */
    boolean removeUserAudioHandler(String userId);

    /**
     * Gets all user audio handlers.
     *
     * @return A map of user IDs to audio handlers
     */
    Map<String, Consumer<byte[]>> getUserAudioHandlers();

    /**
     * Gets the user IDs that have audio handlers.
     *
     * @return A set of user IDs
     */
    Set<String> getHandledUserIds();

    /**
     * Clears all user audio handlers.
     */
    void clearUserAudioHandlers();

    /**
     * Mutes a user.
     * Audio from muted users will not be processed.
     *
     * @param userId The user ID
     */
    void muteUser(String userId);

    /**
     * Unmutes a user.
     *
     * @param userId The user ID
     */
    void unmuteUser(String userId);

    /**
     * Checks if a user is muted.
     *
     * @param userId The user ID
     * @return true if the user is muted, false otherwise
     */
    boolean isUserMuted(String userId);

    /**
     * Gets all user muted states.
     *
     * @return A map of user IDs to muted states
     */
    Map<String, Boolean> getUserMutedStates();

    /**
     * Returns the users currently speaking in the voice channel.
     *
     * @return A set of users
     */
    Set<User> getSpeakingUsers();
}
package fr.farmvivi.discordbot.core.audio.handlers;

import fr.farmvivi.discordbot.core.api.audio.MultiUserAudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Implementation of MultiUserAudioReceiveHandler.
 * This handler can process audio from multiple users.
 */
public class SimpleMultiUserAudioReceiveHandler implements MultiUserAudioReceiveHandler {
    private static final Logger logger = LoggerFactory.getLogger(SimpleMultiUserAudioReceiveHandler.class);

    protected final boolean receiveCombined;
    protected final boolean receiveUser;
    protected Consumer<byte[]> combinedAudioHandler;
    protected final Map<String, Consumer<byte[]>> userAudioHandlers;
    protected final Set<String> mutedUsers;
    protected final Set<User> speakingUsers;

    /**
     * Creates a new SimpleMultiUserAudioReceiveHandler.
     *
     * @param receiveCombined Whether to receive combined audio
     * @param receiveUser Whether to receive user audio
     */
    public SimpleMultiUserAudioReceiveHandler(boolean receiveCombined, boolean receiveUser) {
        this.receiveCombined = receiveCombined;
        this.receiveUser = receiveUser;
        this.userAudioHandlers = new ConcurrentHashMap<>();
        this.mutedUsers = ConcurrentHashMap.newKeySet();
        this.speakingUsers = ConcurrentHashMap.newKeySet();
    }

    @Override
    public boolean canReceiveCombined() {
        return receiveCombined && combinedAudioHandler != null;
    }

    @Override
    public boolean canReceiveUser() {
        return receiveUser && !userAudioHandlers.isEmpty();
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        if (combinedAudioHandler != null) {
            // Normalize the volume to a reasonable level
            byte[] data = combinedAudio.getAudioData(1.0); // Volume factor of 1.0
            combinedAudioHandler.accept(data);
        }
    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {
        User user = userAudio.getUser();
        String userId = user.getId();
        
        // Update speaking users set
        speakingUsers.add(user);
        
        // Skip if user is muted
        if (mutedUsers.contains(userId)) {
            return;
        }
        
        // Handle user audio
        Consumer<byte[]> handler = userAudioHandlers.get(userId);
        if (handler != null) {
            // Get audio data with normalized volume
            byte[] data = userAudio.getAudioData(1.0); // Volume factor of 1.0
            handler.accept(data);
        }
    }

    @Override
    public void setCombinedAudioHandler(Consumer<byte[]> handler) {
        this.combinedAudioHandler = handler;
    }

    @Override
    public Consumer<byte[]> getCombinedAudioHandler() {
        return combinedAudioHandler;
    }

    @Override
    public void addUserAudioHandler(String userId, Consumer<byte[]> handler) {
        if (userId == null || handler == null) {
            return;
        }
        
        userAudioHandlers.put(userId, handler);
    }

    @Override
    public boolean removeUserAudioHandler(String userId) {
        if (userId == null) {
            return false;
        }
        
        return userAudioHandlers.remove(userId) != null;
    }

    @Override
    public Map<String, Consumer<byte[]>> getUserAudioHandlers() {
        return Collections.unmodifiableMap(userAudioHandlers);
    }

    @Override
    public Set<String> getHandledUserIds() {
        return Collections.unmodifiableSet(userAudioHandlers.keySet());
    }

    @Override
    public void clearUserAudioHandlers() {
        userAudioHandlers.clear();
    }

    @Override
    public void muteUser(String userId) {
        if (userId == null) {
            return;
        }
        
        mutedUsers.add(userId);
    }

    @Override
    public void unmuteUser(String userId) {
        if (userId == null) {
            return;
        }
        
        mutedUsers.remove(userId);
    }

    @Override
    public boolean isUserMuted(String userId) {
        if (userId == null) {
            return false;
        }
        
        return mutedUsers.contains(userId);
    }

    @Override
    public Map<String, Boolean> getUserMutedStates() {
        Map<String, Boolean> states = new HashMap<>();
        
        // Add all user handlers as unmuted by default
        for (String userId : userAudioHandlers.keySet()) {
            states.put(userId, false);
        }
        
        // Override with muted users
        for (String userId : mutedUsers) {
            states.put(userId, true);
        }
        
        return Collections.unmodifiableMap(states);
    }

    @Override
    public Set<User> getSpeakingUsers() {
        // Remove users who haven't spoken recently (after a few seconds)
        // This is just a simplified implementation; in a real-world scenario,
        // you'd want to track when a user was last heard and remove them after a timeout.
        
        return Collections.unmodifiableSet(speakingUsers);
    }
}
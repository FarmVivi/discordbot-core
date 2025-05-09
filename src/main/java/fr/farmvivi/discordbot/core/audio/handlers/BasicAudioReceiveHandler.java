package fr.farmvivi.discordbot.core.audio.handlers;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Basic implementation of AudioReceiveHandler.
 * This handler is used to receive audio from a voice channel.
 */
public class BasicAudioReceiveHandler implements AudioReceiveHandler {
    private static final Logger logger = LoggerFactory.getLogger(BasicAudioReceiveHandler.class);

    protected final boolean receiveCombined;
    protected final boolean receiveUser;
    protected Consumer<byte[]> combinedAudioHandler;
    protected Consumer<UserAudio> userAudioHandler;

    /**
     * Creates a new BasicAudioReceiveHandler.
     *
     * @param receiveCombined Whether to receive combined audio
     * @param receiveUser Whether to receive user audio
     */
    public BasicAudioReceiveHandler(boolean receiveCombined, boolean receiveUser) {
        this.receiveCombined = receiveCombined;
        this.receiveUser = receiveUser;
    }

    @Override
    public boolean canReceiveCombined() {
        return receiveCombined;
    }

    @Override
    public boolean canReceiveUser() {
        return receiveUser;
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
        if (userAudioHandler != null) {
            userAudioHandler.accept(userAudio);
        }
    }

    /**
     * Sets the handler for combined audio.
     *
     * @param handler The handler for combined audio
     */
    public void setCombinedAudioHandler(Consumer<byte[]> handler) {
        this.combinedAudioHandler = handler;
    }

    /**
     * Gets the combined audio handler.
     *
     * @return The combined audio handler
     */
    public Consumer<byte[]> getCombinedAudioHandler() {
        return combinedAudioHandler;
    }

    /**
     * Sets the handler for user audio.
     *
     * @param handler The handler for user audio
     */
    public void setUserAudioHandler(Consumer<UserAudio> handler) {
        this.userAudioHandler = handler;
    }

    /**
     * Gets the user audio handler.
     *
     * @return The user audio handler
     */
    public Consumer<UserAudio> getUserAudioHandler() {
        return userAudioHandler;
    }
}
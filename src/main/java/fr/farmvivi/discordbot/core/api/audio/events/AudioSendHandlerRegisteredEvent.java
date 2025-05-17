package fr.farmvivi.discordbot.core.api.audio.events;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Événement émis lorsqu'un handler d'envoi audio est enregistré.
 */
public class AudioSendHandlerRegisteredEvent extends AudioHandlerEvent {
    private final int volume;
    private final int priority;

    /**
     * Crée un nouvel événement d'enregistrement de handler d'envoi audio.
     *
     * @param guild    la guilde
     * @param plugin   le plugin
     * @param handler  le handler d'envoi audio
     * @param volume   le volume initial
     * @param priority la priorité
     */
    public AudioSendHandlerRegisteredEvent(Guild guild, Plugin plugin, AudioSendHandler handler, int volume, int priority) {
        super(guild, plugin, handler);
        this.volume = volume;
        this.priority = priority;
    }

    /**
     * Obtient le volume initial du handler.
     *
     * @return le volume (0-100)
     */
    public int getVolume() {
        return volume;
    }

    /**
     * Obtient la priorité du handler.
     *
     * @return la priorité (0-100)
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Obtient le handler d'envoi audio.
     *
     * @return le handler d'envoi audio
     */
    @Override
    public AudioSendHandler getHandler() {
        return (AudioSendHandler) super.getHandler();
    }
}
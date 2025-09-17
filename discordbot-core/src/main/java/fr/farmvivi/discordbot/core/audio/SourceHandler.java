package fr.farmvivi.discordbot.core.audio;

import net.dv8tion.jda.api.audio.AudioSendHandler;

/**
 * Wrapper pour un handler d'envoi audio avec des propriétés supplémentaires
 * (volume et priorité).
 */
public class SourceHandler {
    private final AudioSendHandler handler;
    private int baseVolume;
    private final int priority;

    /**
     * Crée un nouveau wrapper de handler d'envoi audio.
     *
     * @param handler    le handler d'envoi audio
     * @param baseVolume le volume de base (0-100)
     * @param priority   la priorité (0-100)
     */
    public SourceHandler(AudioSendHandler handler, int baseVolume, int priority) {
        this.handler = handler;
        this.baseVolume = baseVolume;
        this.priority = priority;
    }

    /**
     * Obtient le handler d'envoi audio.
     *
     * @return le handler d'envoi audio
     */
    public AudioSendHandler getHandler() {
        return handler;
    }

    /**
     * Obtient le volume de base.
     *
     * @return le volume de base (0-100)
     */
    public int getBaseVolume() {
        return baseVolume;
    }

    /**
     * Définit le volume de base.
     *
     * @param baseVolume le volume de base (0-100)
     */
    public void setBaseVolume(int baseVolume) {
        this.baseVolume = baseVolume;
    }

    /**
     * Obtient la priorité.
     *
     * @return la priorité (0-100)
     */
    public int getPriority() {
        return priority;
    }
}
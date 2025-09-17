package fr.farmvivi.discordbot.core.api.audio.events;

import net.dv8tion.jda.api.entities.Guild;

/**
 * Événement émis après qu'un frame audio a été mixé.
 * Fournit des informations sur le mixage, comme le nombre de sources
 * actives et si le frame a été directement transmis (bypass) ou mixé.
 */
public class AudioFrameMixedEvent extends AudioEvent {
    private final int activeSourceCount;
    private final boolean bypassMode;
    private final boolean containsAudio;

    /**
     * Crée un nouvel événement de frame mixé.
     *
     * @param guild            la guilde
     * @param activeSourceCount le nombre de sources actives
     * @param bypassMode       true si le frame a été directement transmis (bypass)
     * @param containsAudio    true si le frame contient de l'audio
     */
    public AudioFrameMixedEvent(Guild guild, int activeSourceCount, boolean bypassMode, boolean containsAudio) {
        super(guild);
        this.activeSourceCount = activeSourceCount;
        this.bypassMode = bypassMode;
        this.containsAudio = containsAudio;
    }

    /**
     * Obtient le nombre de sources actives.
     *
     * @return le nombre de sources actives
     */
    public int getActiveSourceCount() {
        return activeSourceCount;
    }

    /**
     * Vérifie si le frame a été directement transmis (bypass).
     *
     * @return true si le frame a été directement transmis
     */
    public boolean isBypassMode() {
        return bypassMode;
    }

    /**
     * Vérifie si le frame contient de l'audio.
     *
     * @return true si le frame contient de l'audio
     */
    public boolean containsAudio() {
        return containsAudio;
    }
}
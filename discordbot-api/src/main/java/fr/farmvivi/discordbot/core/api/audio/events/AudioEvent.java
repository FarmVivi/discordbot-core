package fr.farmvivi.discordbot.core.api.audio.events;

import fr.farmvivi.discordbot.core.api.event.Event;
import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Événement de base pour tous les événements audio.
 */
public abstract class AudioEvent implements Event {
    private final Guild guild;

    /**
     * Crée un nouvel événement audio.
     *
     * @param guild la guilde concernée par l'événement
     */
    protected AudioEvent(Guild guild) {
        this.guild = guild;
    }

    /**
     * Obtient la guilde concernée par l'événement.
     *
     * @return la guilde
     */
    public Guild getGuild() {
        return guild;
    }
}
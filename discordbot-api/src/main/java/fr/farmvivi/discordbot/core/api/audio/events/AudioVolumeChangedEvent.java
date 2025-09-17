package fr.farmvivi.discordbot.core.api.audio.events;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Événement émis lorsque le volume d'un handler d'envoi audio change.
 */
public class AudioVolumeChangedEvent extends AudioEvent {
    private final Plugin plugin;
    private final int oldVolume;
    private final int newVolume;
    private final boolean isFading;

    /**
     * Crée un nouvel événement de changement de volume.
     *
     * @param guild     la guilde
     * @param plugin    le plugin
     * @param oldVolume l'ancien volume
     * @param newVolume le nouveau volume
     * @param isFading  true si le changement est dû à un fondu (fade)
     */
    public AudioVolumeChangedEvent(Guild guild, Plugin plugin, int oldVolume, int newVolume, boolean isFading) {
        super(guild);
        this.plugin = plugin;
        this.oldVolume = oldVolume;
        this.newVolume = newVolume;
        this.isFading = isFading;
    }

    /**
     * Obtient le plugin associé au handler.
     *
     * @return le plugin
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Obtient l'ancien volume.
     *
     * @return l'ancien volume (0-100)
     */
    public int getOldVolume() {
        return oldVolume;
    }

    /**
     * Obtient le nouveau volume.
     *
     * @return le nouveau volume (0-100)
     */
    public int getNewVolume() {
        return newVolume;
    }

    /**
     * Vérifie si le changement est dû à un fondu (fade).
     *
     * @return true si le changement est dû à un fondu
     */
    public boolean isFading() {
        return isFading;
    }
}
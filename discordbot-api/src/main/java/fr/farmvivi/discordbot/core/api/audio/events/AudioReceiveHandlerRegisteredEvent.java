package fr.farmvivi.discordbot.core.api.audio.events;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Événement émis lorsqu'un handler de réception audio est enregistré.
 */
public class AudioReceiveHandlerRegisteredEvent extends AudioHandlerEvent {
    /**
     * Crée un nouvel événement d'enregistrement de handler de réception audio.
     *
     * @param guild   la guilde
     * @param plugin  le plugin
     * @param handler le handler de réception audio
     */
    public AudioReceiveHandlerRegisteredEvent(Guild guild, Plugin plugin, AudioReceiveHandler handler) {
        super(guild, plugin, handler);
    }

    /**
     * Obtient le handler de réception audio.
     *
     * @return le handler de réception audio
     */
    @Override
    public AudioReceiveHandler getHandler() {
        return (AudioReceiveHandler) super.getHandler();
    }
}
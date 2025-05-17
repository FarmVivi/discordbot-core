package fr.farmvivi.discordbot.core.api.audio.events;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Événement émis lorsqu'un handler d'envoi audio est désenregistré.
 */
public class AudioSendHandlerRemovedEvent extends AudioHandlerEvent {
    /**
     * Crée un nouvel événement de désenregistrement de handler d'envoi audio.
     *
     * @param guild   la guilde
     * @param plugin  le plugin
     * @param handler le handler d'envoi audio
     */
    public AudioSendHandlerRemovedEvent(Guild guild, Plugin plugin, AudioSendHandler handler) {
        super(guild, plugin, handler);
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
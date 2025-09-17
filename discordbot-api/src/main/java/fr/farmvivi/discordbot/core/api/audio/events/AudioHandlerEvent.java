package fr.farmvivi.discordbot.core.api.audio.events;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Classe de base pour les événements liés aux handlers audio.
 */
public abstract class AudioHandlerEvent extends AudioEvent {
    private final Plugin plugin;
    private final Object handler;

    /**
     * Crée un nouvel événement de handler audio.
     *
     * @param guild   la guilde
     * @param plugin  le plugin
     * @param handler le handler audio (AudioSendHandler ou AudioReceiveHandler)
     */
    protected AudioHandlerEvent(Guild guild, Plugin plugin, Object handler) {
        super(guild);
        this.plugin = plugin;
        this.handler = handler;
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
     * Obtient le handler audio.
     *
     * @return le handler audio
     */
    public Object getHandler() {
        return handler;
    }
}
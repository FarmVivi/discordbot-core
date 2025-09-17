package fr.farmvivi.discordbot.core.api.audio;

import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Service principal pour la gestion audio.
 * Permet l'enregistrement de handlers audio par plugin et par guilde,
 * avec gestion des priorités et du volume.
 */
public interface AudioService {
    /**
     * Valeur minimum pour le volume (0%)
     */
    int MIN_VOLUME = 0;
    
    /**
     * Valeur maximum pour le volume (100%)
     */
    int MAX_VOLUME = 100;
    
    /**
     * Valeur minimum pour la priorité
     */
    int MIN_PRIORITY = 0;
    
    /**
     * Valeur maximum pour la priorité
     */
    int MAX_PRIORITY = 100;
    
    /**
     * Priorité par défaut
     */
    int DEFAULT_PRIORITY = 50;
    
    /**
     * Volume par défaut (100%)
     */
    int DEFAULT_VOLUME = 100;
    
    /**
     * Seuil de priorité par défaut.
     * Les sources audio avec une priorité inférieure seront atténuées
     * lorsqu'une source de priorité supérieure sera active.
     */
    int DEFAULT_PRIORITY_THRESHOLD = 70;

    /**
     * Enregistre un handler d'envoi audio pour une guilde et un plugin spécifiques.
     *
     * @param guild         la guilde où l'audio sera envoyé
     * @param plugin        le plugin qui enregistre le handler
     * @param handler       le handler d'envoi audio
     * @param initialVolume volume initial (0-100)
     * @param priority      niveau de priorité (0-100, plus élevé = plus important)
     * @throws IllegalArgumentException si les paramètres sont invalides
     */
    void registerSendHandler(Guild guild, Plugin plugin, AudioSendHandler handler, int initialVolume, int priority);

    /**
     * Désenregistre un handler d'envoi audio pour une guilde et un plugin spécifiques.
     *
     * @param guild  la guilde
     * @param plugin le plugin
     */
    void deregisterSendHandler(Guild guild, Plugin plugin);

    /**
     * Définit le volume pour le handler d'envoi audio d'un plugin.
     *
     * @param guild  la guilde
     * @param plugin le plugin
     * @param volume le volume (0-100)
     * @throws IllegalArgumentException si le volume est hors limites
     */
    void setVolume(Guild guild, Plugin plugin, int volume);

    /**
     * Enregistre un handler de réception audio pour une guilde et un plugin spécifiques.
     *
     * @param guild   la guilde
     * @param plugin  le plugin
     * @param handler le handler de réception audio
     */
    void registerReceiveHandler(Guild guild, Plugin plugin, AudioReceiveHandler handler);

    /**
     * Désenregistre un handler de réception audio pour une guilde et un plugin spécifiques.
     *
     * @param guild  la guilde
     * @param plugin le plugin
     */
    void deregisterReceiveHandler(Guild guild, Plugin plugin);

    /**
     * Modifie le seuil de priorité pour une guilde.
     * Les sources audio avec une priorité inférieure au seuil seront atténuées
     * lorsqu'une source de priorité supérieure ou égale au seuil sera active.
     *
     * @param guild     la guilde
     * @param threshold le seuil de priorité (0-100)
     * @throws IllegalArgumentException si le seuil est hors limites
     */
    void setPriorityThreshold(Guild guild, int threshold);

    /**
     * Vérifie si un plugin a un handler d'envoi actif pour une guilde.
     *
     * @param guild  la guilde
     * @param plugin le plugin
     * @return true si le plugin a un handler d'envoi actif
     */
    boolean hasActiveSendHandler(Guild guild, Plugin plugin);

    /**
     * Vérifie si un plugin a un handler de réception actif pour une guilde.
     *
     * @param guild  la guilde
     * @param plugin le plugin
     * @return true si le plugin a un handler de réception actif
     */
    boolean hasActiveReceiveHandler(Guild guild, Plugin plugin);

    /**
     * Ferme la connexion audio pour une guilde.
     *
     * @param guild la guilde
     */
    void closeAudioConnection(Guild guild);

    /**
     * Ferme toutes les connexions audio pour un plugin spécifique.
     * À appeler lors de la désactivation d'un plugin.
     *
     * @param plugin le plugin
     */
    void closeAllConnectionsForPlugin(Plugin plugin);
}
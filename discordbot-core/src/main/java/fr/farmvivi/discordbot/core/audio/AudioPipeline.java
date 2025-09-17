package fr.farmvivi.discordbot.core.audio;

import fr.farmvivi.discordbot.core.api.audio.AudioService;
import fr.farmvivi.discordbot.core.api.audio.events.AudioFrameMixedEvent;
import fr.farmvivi.discordbot.core.api.audio.events.AudioVolumeChangedEvent;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Pipeline audio pour une guilde spécifique.
 * Gère le mixage de plusieurs sources audio et la réception audio.
 */
public class AudioPipeline implements AudioSendHandler, AudioReceiveHandler {
    private static final Logger logger = LoggerFactory.getLogger(AudioPipeline.class);
    
    // Constantes pour le fondu audio
    private static final int FADE_DURATION_MS = 200;
    private static final int FRAME_DURATION_MS = 20;
    private static final int FADE_STEPS = FADE_DURATION_MS / FRAME_DURATION_MS;
    
    // Stratégies de traitement audio
    private enum Strategy {
        DIRECT_BYPASS,  // Une seule source, transmission directe
        MIXING          // Plusieurs sources, mixage
    }
    
    private final Guild guild;
    private final EventManager eventManager;
    
    // Gestionnaires pour les handlers et les priorités
    private final Map<String, SourceHandler> sendHandlers = new ConcurrentHashMap<>();
    private final Map<String, AudioReceiveHandler> receiveHandlers = new ConcurrentHashMap<>();
    private final PriorityManager priorityManager;
    
    // Mixeur et état
    private final AudioMixer mixer;
    private Strategy currentStrategy = Strategy.DIRECT_BYPASS;
    private final ReentrantLock strategyLock = new ReentrantLock();
    private int priorityThreshold = AudioService.DEFAULT_PRIORITY_THRESHOLD;
    
    // État cache
    private String lastActivePluginName = null;
    private boolean providedAudioLastFrame = false;

    /**
     * Crée un nouveau pipeline audio pour une guilde.
     *
     * @param guild        la guilde
     * @param eventManager le gestionnaire d'événements
     */
    public AudioPipeline(Guild guild, EventManager eventManager) {
        this.guild = guild;
        this.eventManager = eventManager;
        this.mixer = new AudioMixer();
        this.priorityManager = new PriorityManager(FADE_STEPS);
        
        // Connecte ce pipeline au AudioManager de la guilde
        guild.getAudioManager().setSendingHandler(this);
        guild.getAudioManager().setReceivingHandler(this);
        
        logger.debug("Created audio pipeline for guild {}", guild.getName());
    }

    /**
     * Enregistre un handler d'envoi audio pour un plugin.
     *
     * @param plugin   le plugin
     * @param handler  le handler d'envoi audio
     * @param volume   le volume initial (0-100)
     * @param priority la priorité (0-100)
     */
    public void registerSendHandler(Plugin plugin, AudioSendHandler handler, int volume, int priority) {
        String pluginName = plugin.getName();
        SourceHandler sourceHandler = new SourceHandler(handler, volume, priority);
        sendHandlers.put(pluginName, sourceHandler);
        
        // Met à jour la stratégie si nécessaire
        updateStrategy();
        
        logger.debug("Registered send handler for plugin {} in guild {}", pluginName, guild.getName());
    }

    /**
     * Désenregistre un handler d'envoi audio pour un plugin.
     *
     * @param plugin le plugin
     */
    public void deregisterSendHandler(Plugin plugin) {
        String pluginName = plugin.getName();
        sendHandlers.remove(pluginName);
        
        // Réinitialise l'état du dernier plugin actif si nécessaire
        if (pluginName.equals(lastActivePluginName)) {
            lastActivePluginName = null;
        }
        
        // Met à jour la stratégie si nécessaire
        updateStrategy();
        
        logger.debug("Deregistered send handler for plugin {} in guild {}", pluginName, guild.getName());
    }

    /**
     * Enregistre un handler de réception audio pour un plugin.
     *
     * @param plugin  le plugin
     * @param handler le handler de réception audio
     */
    public void registerReceiveHandler(Plugin plugin, AudioReceiveHandler handler) {
        String pluginName = plugin.getName();
        receiveHandlers.put(pluginName, handler);
        logger.debug("Registered receive handler for plugin {} in guild {}", pluginName, guild.getName());
    }

    /**
     * Désenregistre un handler de réception audio pour un plugin.
     *
     * @param plugin le plugin
     */
    public void deregisterReceiveHandler(Plugin plugin) {
        String pluginName = plugin.getName();
        receiveHandlers.remove(pluginName);
        logger.debug("Deregistered receive handler for plugin {} in guild {}", pluginName, guild.getName());
    }

    /**
     * Définit le volume pour un plugin.
     *
     * @param plugin le plugin
     * @param volume le volume (0-100)
     */
    public void setVolume(Plugin plugin, int volume) {
        String pluginName = plugin.getName();
        SourceHandler sourceHandler = sendHandlers.get(pluginName);
        if (sourceHandler != null) {
            int oldVolume = sourceHandler.getBaseVolume();
            sourceHandler.setBaseVolume(volume);
            
            // Émet un événement de changement de volume
            AudioVolumeChangedEvent event = new AudioVolumeChangedEvent(guild, plugin, oldVolume, volume, false);
            eventManager.fireEvent(event);
            
            logger.debug("Set volume to {} for plugin {} in guild {}", volume, pluginName, guild.getName());
        }
    }

    /**
     * Définit le seuil de priorité pour ce pipeline.
     *
     * @param threshold le seuil de priorité (0-100)
     */
    public void setPriorityThreshold(int threshold) {
        this.priorityThreshold = threshold;
        logger.debug("Set priority threshold to {} for guild {}", threshold, guild.getName());
    }

    /**
     * Vérifie si un plugin a un handler d'envoi actif.
     *
     * @param plugin le plugin
     * @return true si le plugin a un handler d'envoi actif
     */
    public boolean hasSendHandler(Plugin plugin) {
        return sendHandlers.containsKey(plugin.getName());
    }

    /**
     * Vérifie si un plugin a un handler de réception actif.
     *
     * @param plugin le plugin
     * @return true si le plugin a un handler de réception actif
     */
    public boolean hasReceiveHandler(Plugin plugin) {
        return receiveHandlers.containsKey(plugin.getName());
    }

    /**
     * Obtient le handler d'envoi audio pour un plugin.
     *
     * @param plugin le plugin
     * @return le handler d'envoi audio, ou null s'il n'existe pas
     */
    public AudioSendHandler getSendHandler(Plugin plugin) {
        SourceHandler handler = sendHandlers.get(plugin.getName());
        return handler != null ? handler.getHandler() : null;
    }

    /**
     * Obtient le handler de réception audio pour un plugin.
     *
     * @param plugin le plugin
     * @return le handler de réception audio, ou null s'il n'existe pas
     */
    public AudioReceiveHandler getReceiveHandler(Plugin plugin) {
        return receiveHandlers.get(plugin.getName());
    }

    /**
     * Vérifie si le pipeline est vide (aucun handler actif).
     *
     * @return true si le pipeline est vide
     */
    public boolean isEmpty() {
        return sendHandlers.isEmpty() && receiveHandlers.isEmpty();
    }

    /**
     * Ferme le pipeline et libère les ressources.
     */
    public void close() {
        // Déconnecte ce pipeline du AudioManager de la guilde
        guild.getAudioManager().setSendingHandler(null);
        guild.getAudioManager().setReceivingHandler(null);
        guild.getAudioManager().closeAudioConnection();
        
        // Vide les collections
        sendHandlers.clear();
        receiveHandlers.clear();
        
        logger.debug("Closed audio pipeline for guild {}", guild.getName());
    }
    
    /**
     * Met à jour la stratégie de traitement audio en fonction du nombre de sources.
     */
    private void updateStrategy() {
        strategyLock.lock();
        try {
            Strategy newStrategy = sendHandlers.size() <= 1 ? Strategy.DIRECT_BYPASS : Strategy.MIXING;
            if (newStrategy != currentStrategy) {
                currentStrategy = newStrategy;
                logger.debug("Switched to {} strategy for guild {}", 
                        currentStrategy == Strategy.DIRECT_BYPASS ? "direct bypass" : "mixing",
                        guild.getName());
            }
        } finally {
            strategyLock.unlock();
        }
    }
    
    //
    // Implémentation de AudioSendHandler
    //
    
    @Override
    public boolean canProvide() {
        strategyLock.lock();
        try {
            // Détermine si une source fournit de l'audio
            if (currentStrategy == Strategy.DIRECT_BYPASS) {
                // Mode bypass : une seule source
                if (sendHandlers.isEmpty()) {
                    return false;
                }
                
                // La seule source existante
                SourceHandler sourceHandler = sendHandlers.values().iterator().next();
                return sourceHandler.getHandler().canProvide();
            } else {
                // Mode mixage : plusieurs sources
                boolean canProvide = false;
                
                // Préparation pour la détection de sources prioritaires
                boolean highPriorityActive = false;
                String highPriorityPluginName = null;
                
                // Vérifie chaque source
                for (Map.Entry<String, SourceHandler> entry : sendHandlers.entrySet()) {
                    String pluginName = entry.getKey();
                    SourceHandler sourceHandler = entry.getValue();
                    AudioSendHandler handler = sourceHandler.getHandler();
                    
                    // Vérifie si ce handler peut fournir de l'audio
                    if (handler.canProvide()) {
                        canProvide = true;
                        
                        // Détecte les sources de haute priorité
                        int priority = sourceHandler.getPriority();
                        if (priority >= priorityThreshold) {
                            highPriorityActive = true;
                            highPriorityPluginName = pluginName;
                            
                            // Si un plugin de haute priorité devient actif, démarre le fade out des autres
                            if (lastActivePluginName == null || !lastActivePluginName.equals(pluginName)) {
                                startFade(pluginName);
                            }
                            break;
                        }
                    }
                }
                
                // Si aucune source de haute priorité n'est active et qu'il y en avait une avant,
                // démarre le fade in pour toutes les sources
                if (!highPriorityActive && lastActivePluginName != null) {
                    startFadeIn();
                }
                
                // Met à jour l'état du dernier plugin actif
                lastActivePluginName = highPriorityActive ? highPriorityPluginName : null;
                
                return canProvide;
            }
        } finally {
            strategyLock.unlock();
        }
    }
    
    @Override
    public ByteBuffer provide20MsAudio() {
        strategyLock.lock();
        try {
            ByteBuffer audio;
            int activeSourceCount = 0;
            boolean bypassMode = currentStrategy == Strategy.DIRECT_BYPASS;
            
            if (bypassMode) {
                // Mode bypass : transmet directement l'audio d'une seule source
                if (sendHandlers.isEmpty()) {
                    audio = null;
                } else {
                    SourceHandler sourceHandler = sendHandlers.values().iterator().next();
                    AudioSendHandler handler = sourceHandler.getHandler();
                    
                    if (handler.canProvide()) {
                        audio = handler.provide20MsAudio();
                        activeSourceCount = 1;
                    } else {
                        audio = null;
                    }
                }
            } else {
                // Mode mixage : mixe plusieurs sources
                mixer.reset();
                
                // Traite chaque source
                for (Map.Entry<String, SourceHandler> entry : sendHandlers.entrySet()) {
                    String pluginName = entry.getKey();
                    SourceHandler sourceHandler = entry.getValue();
                    AudioSendHandler handler = sourceHandler.getHandler();
                    
                    // Ajoute l'audio de cette source si disponible
                    if (handler.canProvide()) {
                        ByteBuffer sourceAudio = handler.provide20MsAudio();
                        if (sourceAudio != null) {
                            // Calcule le volume effectif en tenant compte des fades
                            float effectiveVolume = calculateEffectiveVolume(pluginName, sourceHandler);
                            
                            // Ajoute au mixeur
                            mixer.addSource(sourceAudio, effectiveVolume);
                            activeSourceCount++;
                        }
                    }
                    
                    // Mets à jour l'état des fades
                    priorityManager.updateFade(pluginName);
                }
                
                // Obtient l'audio mixé
                audio = mixer.mix();
            }
            
            // Émet un événement de mixage
            boolean containsAudio = audio != null;
            AudioFrameMixedEvent event = new AudioFrameMixedEvent(guild, activeSourceCount, bypassMode, containsAudio);
            eventManager.fireEvent(event);
            
            // Met à jour l'état
            providedAudioLastFrame = containsAudio;
            
            return audio;
        } finally {
            strategyLock.unlock();
        }
    }
    
    @Override
    public boolean isOpus() {
        // En mode bypass, utilise le format d'origine
        if (currentStrategy == Strategy.DIRECT_BYPASS && !sendHandlers.isEmpty()) {
            SourceHandler sourceHandler = sendHandlers.values().iterator().next();
            return sourceHandler.getHandler().isOpus();
        }
        
        // En mode mixage, utilise toujours PCM (JDA se chargera de l'encodage)
        return false;
    }
    
    //
    // Implémentation de AudioReceiveHandler
    //
    
    @Override
    public boolean canReceiveCombined() {
        // Vérifie si au moins un handler peut recevoir l'audio combiné
        for (AudioReceiveHandler handler : receiveHandlers.values()) {
            if (handler.canReceiveCombined()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean canReceiveUser() {
        // Vérifie si au moins un handler peut recevoir l'audio par utilisateur
        for (AudioReceiveHandler handler : receiveHandlers.values()) {
            if (handler.canReceiveUser()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean canReceiveEncoded() {
        // Vérifie si au moins un handler peut recevoir l'audio encodé
        for (AudioReceiveHandler handler : receiveHandlers.values()) {
            if (handler.canReceiveEncoded()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        // Propage l'audio combiné à tous les handlers intéressés
        for (AudioReceiveHandler handler : receiveHandlers.values()) {
            if (handler.canReceiveCombined()) {
                handler.handleCombinedAudio(combinedAudio);
            }
        }
    }
    
    @Override
    public void handleUserAudio(UserAudio userAudio) {
        // Propage l'audio par utilisateur à tous les handlers intéressés
        for (AudioReceiveHandler handler : receiveHandlers.values()) {
            if (handler.canReceiveUser()) {
                handler.handleUserAudio(userAudio);
            }
        }
    }
    
    @Override
    public void handleEncodedAudio(net.dv8tion.jda.api.audio.OpusPacket opusPacket) {
        // Propage l'audio encodé à tous les handlers intéressés
        for (AudioReceiveHandler handler : receiveHandlers.values()) {
            if (handler.canReceiveEncoded()) {
                handler.handleEncodedAudio(opusPacket);
            }
        }
    }
    
    //
    // Méthodes de gestion des fades
    //
    
    /**
     * Démarre un fondu sortant (fade out) pour toutes les sources sauf celle spécifiée.
     *
     * @param activePlugin le plugin qui reste à volume normal
     */
    private void startFade(String activePlugin) {
        for (String pluginName : sendHandlers.keySet()) {
            if (!pluginName.equals(activePlugin)) {
                priorityManager.startFadeOut(pluginName);
            }
        }
    }
    
    /**
     * Démarre un fondu entrant (fade in) pour toutes les sources.
     */
    private void startFadeIn() {
        for (String pluginName : sendHandlers.keySet()) {
            priorityManager.startFadeIn(pluginName);
        }
    }
    
    /**
     * Calcule le volume effectif pour une source, en tenant compte des fades.
     *
     * @param pluginName    le nom du plugin
     * @param sourceHandler le handler de source
     * @return le volume effectif (0.0-1.0)
     */
    private float calculateEffectiveVolume(String pluginName, SourceHandler sourceHandler) {
        float baseVolume = sourceHandler.getBaseVolume() / 100.0f;
        float fadeMultiplier = priorityManager.getFadeMultiplier(pluginName);
        return baseVolume * fadeMultiplier;
    }
}
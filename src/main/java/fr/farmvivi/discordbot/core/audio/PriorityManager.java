package fr.farmvivi.discordbot.core.audio;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gère les priorités et les fondus (fades) pour l'audio.
 */
public class PriorityManager {
    private static final float FADE_MIN = 0.0f;
    private static final float FADE_MAX = 1.0f;
    
    // Cartes de l'état des fondus
    private final Map<String, Float> fadeMultipliers = new ConcurrentHashMap<>();
    private final Map<String, Float> fadeIncrements = new ConcurrentHashMap<>();
    
    // Nombre de pas pour un fondu complet
    private final int fadeSteps;
    private final float fadeStepSize;
    
    /**
     * Crée un nouveau gestionnaire de priorités.
     *
     * @param fadeSteps nombre de pas pour un fondu complet
     */
    public PriorityManager(int fadeSteps) {
        this.fadeSteps = fadeSteps;
        this.fadeStepSize = (FADE_MAX - FADE_MIN) / fadeSteps;
    }
    
    /**
     * Démarre un fondu sortant (fade out) pour une source.
     *
     * @param sourceName le nom de la source
     */
    public void startFadeOut(String sourceName) {
        // Initialise le multiplicateur de fondu s'il n'existe pas
        fadeMultipliers.putIfAbsent(sourceName, FADE_MAX);
        
        // Calcule le pas de diminution pour atteindre 0 en fadeSteps pas
        float increment = -fadeStepSize;
        fadeIncrements.put(sourceName, increment);
    }
    
    /**
     * Démarre un fondu entrant (fade in) pour une source.
     *
     * @param sourceName le nom de la source
     */
    public void startFadeIn(String sourceName) {
        // Initialise le multiplicateur de fondu s'il n'existe pas
        fadeMultipliers.putIfAbsent(sourceName, FADE_MIN);
        
        // Calcule le pas d'augmentation pour atteindre 1 en fadeSteps pas
        float increment = fadeStepSize;
        fadeIncrements.put(sourceName, increment);
    }
    
    /**
     * Met à jour l'état de fondu pour une source.
     *
     * @param sourceName le nom de la source
     */
    public void updateFade(String sourceName) {
        // Si pas d'incrément, rien à faire
        Float increment = fadeIncrements.get(sourceName);
        if (increment == null) {
            return;
        }
        
        // Obtient le multiplicateur actuel
        float multiplier = fadeMultipliers.getOrDefault(sourceName, FADE_MAX);
        
        // Applique l'incrément
        multiplier += increment;
        
        // Limite le multiplicateur
        if (multiplier <= FADE_MIN) {
            multiplier = FADE_MIN;
            fadeIncrements.remove(sourceName);  // Arrête le fondu
        } else if (multiplier >= FADE_MAX) {
            multiplier = FADE_MAX;
            fadeIncrements.remove(sourceName);  // Arrête le fondu
        }
        
        // Stocke le nouveau multiplicateur
        fadeMultipliers.put(sourceName, multiplier);
    }
    
    /**
     * Obtient le multiplicateur de fondu pour une source.
     *
     * @param sourceName le nom de la source
     * @return le multiplicateur de fondu (0.0-1.0)
     */
    public float getFadeMultiplier(String sourceName) {
        return fadeMultipliers.getOrDefault(sourceName, FADE_MAX);
    }
    
    /**
     * Réinitialise l'état de fondu pour une source.
     *
     * @param sourceName le nom de la source
     */
    public void resetFade(String sourceName) {
        fadeMultipliers.put(sourceName, FADE_MAX);
        fadeIncrements.remove(sourceName);
    }
    
    /**
     * Réinitialise l'état de fondu pour toutes les sources.
     */
    public void resetAllFades() {
        fadeMultipliers.clear();
        fadeIncrements.clear();
    }
}
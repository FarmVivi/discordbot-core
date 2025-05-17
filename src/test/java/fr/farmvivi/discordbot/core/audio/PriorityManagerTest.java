package fr.farmvivi.discordbot.core.audio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe PriorityManager.
 */
public class PriorityManagerTest {
    private PriorityManager manager;
    private static final int FADE_STEPS = 10;
    private static final String SOURCE_1 = "source1";
    private static final String SOURCE_2 = "source2";
    
    @BeforeEach
    public void setUp() {
        manager = new PriorityManager(FADE_STEPS);
    }
    
    @Test
    public void testInitialState() {
        // Par défaut, le multiplicateur devrait être 1.0
        assertEquals(1.0f, manager.getFadeMultiplier(SOURCE_1), 0.001f);
        assertEquals(1.0f, manager.getFadeMultiplier(SOURCE_2), 0.001f);
    }
    
    @Test
    public void testFadeOut() {
        // Démarre un fondu sortant pour une source
        manager.startFadeOut(SOURCE_1);
        
        // Vérifie que le multiplicateur est toujours à 1.0 initialement
        assertEquals(1.0f, manager.getFadeMultiplier(SOURCE_1), 0.001f);
        
        // Avance d'une étape
        manager.updateFade(SOURCE_1);
        
        // Vérifie que le multiplicateur a diminué
        float expected = 1.0f - (1.0f / FADE_STEPS);
        assertEquals(expected, manager.getFadeMultiplier(SOURCE_1), 0.001f);
        
        // Avance de plusieurs étapes
        for (int i = 0; i < FADE_STEPS - 1; i++) {
            manager.updateFade(SOURCE_1);
        }
        
        // Vérifie que le multiplicateur est à 0 après FADE_STEPS étapes
        assertEquals(0.0f, manager.getFadeMultiplier(SOURCE_1), 0.001f);
        
        // Vérifie que des étapes supplémentaires ne changent pas le multiplicateur
        manager.updateFade(SOURCE_1);
        assertEquals(0.0f, manager.getFadeMultiplier(SOURCE_1), 0.001f);
    }
    
    @Test
    public void testFadeIn() {
        // Pour tester le fondu entrant, il faut d'abord mettre le multiplicateur à 0
        manager.startFadeOut(SOURCE_1);
        for (int i = 0; i < FADE_STEPS; i++) {
            manager.updateFade(SOURCE_1);
        }
        
        // Vérifie que le multiplicateur est à 0
        assertEquals(0.0f, manager.getFadeMultiplier(SOURCE_1), 0.001f);
        
        // Démarre un fondu entrant
        manager.startFadeIn(SOURCE_1);
        
        // Avance d'une étape
        manager.updateFade(SOURCE_1);
        
        // Vérifie que le multiplicateur a augmenté
        float expected = 0.0f + (1.0f / FADE_STEPS);
        assertEquals(expected, manager.getFadeMultiplier(SOURCE_1), 0.001f);
        
        // Avance de plusieurs étapes
        for (int i = 0; i < FADE_STEPS - 1; i++) {
            manager.updateFade(SOURCE_1);
        }
        
        // Vérifie que le multiplicateur est à 1 après FADE_STEPS étapes
        assertEquals(1.0f, manager.getFadeMultiplier(SOURCE_1), 0.001f);
    }
    
    @Test
    public void testMultipleSources() {
        // Démarre des fondus pour des sources différentes
        manager.startFadeOut(SOURCE_1);
        manager.startFadeIn(SOURCE_2);  // Supposons que SOURCE_2 est initialement à 0
        
        // Met à jour les fondus
        manager.updateFade(SOURCE_1);
        manager.updateFade(SOURCE_2);
        
        // Vérifie que chaque source a son propre multiplicateur
        float expected1 = 1.0f - (1.0f / FADE_STEPS);
        float expected2 = 0.0f + (1.0f / FADE_STEPS);  // Si on part de 0
        
        assertEquals(expected1, manager.getFadeMultiplier(SOURCE_1), 0.001f);
        assertEquals(expected2, manager.getFadeMultiplier(SOURCE_2), 0.001f);
    }
    
    @Test
    public void testResetFade() {
        // Démarre un fondu sortant
        manager.startFadeOut(SOURCE_1);
        manager.updateFade(SOURCE_1);
        
        // Vérifie que le multiplicateur a changé
        assertTrue(manager.getFadeMultiplier(SOURCE_1) < 1.0f);
        
        // Réinitialise le fondu
        manager.resetFade(SOURCE_1);
        
        // Vérifie que le multiplicateur est revenu à 1.0
        assertEquals(1.0f, manager.getFadeMultiplier(SOURCE_1), 0.001f);
    }
    
    @Test
    public void testResetAllFades() {
        // Démarre des fondus pour plusieurs sources
        manager.startFadeOut(SOURCE_1);
        manager.startFadeOut(SOURCE_2);
        
        // Met à jour les fondus
        manager.updateFade(SOURCE_1);
        manager.updateFade(SOURCE_2);
        
        // Vérifie que les multiplicateurs ont changé
        assertTrue(manager.getFadeMultiplier(SOURCE_1) < 1.0f);
        assertTrue(manager.getFadeMultiplier(SOURCE_2) < 1.0f);
        
        // Réinitialise tous les fondus
        manager.resetAllFades();
        
        // Vérifie que les multiplicateurs sont revenus à 1.0
        assertEquals(1.0f, manager.getFadeMultiplier(SOURCE_1), 0.001f);
        assertEquals(1.0f, manager.getFadeMultiplier(SOURCE_2), 0.001f);
    }
    
    @Test
    public void testInterruptedFade() {
        // Démarre un fondu sortant
        manager.startFadeOut(SOURCE_1);
        
        // Avance de quelques étapes
        for (int i = 0; i < FADE_STEPS / 2; i++) {
            manager.updateFade(SOURCE_1);
        }
        
        // Vérifie que le multiplicateur est à mi-chemin
        float expected = 1.0f - ((FADE_STEPS / 2) * (1.0f / FADE_STEPS));
        assertEquals(expected, manager.getFadeMultiplier(SOURCE_1), 0.001f);
        
        // Interrompt le fondu sortant et démarre un fondu entrant
        manager.startFadeIn(SOURCE_1);
        
        // Avance d'une étape
        manager.updateFade(SOURCE_1);
        
        // Vérifie que le multiplicateur a augmenté
        assertTrue(manager.getFadeMultiplier(SOURCE_1) > expected);
    }
}
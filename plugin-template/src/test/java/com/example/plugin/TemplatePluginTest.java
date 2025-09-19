package com.example.plugin;

import fr.farmvivi.discordbot.api.plugin.PluginContext;
import fr.farmvivi.discordbot.api.plugin.PluginLifecycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for TemplatePlugin.
 * Demonstrates basic plugin testing patterns.
 */
@ExtendWith(MockitoExtension.class)
public class TemplatePluginTest {

    @Mock
    private PluginContext mockContext;

    @Mock
    private Logger mockLogger;

    private TemplatePlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = new TemplatePlugin();

        // Setup mock context
        when(mockContext.getPluginName()).thenReturn("TemplatePlugin");
        when(mockContext.getPluginVersion()).thenReturn("1.0.0");
        when(mockContext.getLogger()).thenReturn(mockLogger);
        when(mockContext.getDataFolder()).thenReturn("plugins/TemplatePlugin");
    }

    @Test
    void testPluginInitialization() {
        // Test that plugin starts in DISCOVERED state
        assertEquals(PluginLifecycle.DISCOVERED, plugin.getLifecycle());
    }

    @Test
    void testPluginName() {
        // Load the plugin context
        plugin.onLoad(mockContext);

        // Test that plugin returns correct name
        assertEquals("TemplatePlugin", plugin.getName());
        assertEquals("1.0.0", plugin.getVersion());
    }

    @Test
    void testPluginLifecycle() {
        // Test plugin lifecycle transitions
        plugin.onLoad(mockContext);
        plugin.setLifecycle(PluginLifecycle.LOADED);

        plugin.onPreEnable();
        plugin.setLifecycle(PluginLifecycle.PRE_ENABLED);

        plugin.onEnable();
        plugin.setLifecycle(PluginLifecycle.ENABLED);

        assertTrue(plugin.isEnabled());

        plugin.onPreDisable();
        plugin.setLifecycle(PluginLifecycle.PRE_DISABLED);

        plugin.onDisable();
        plugin.setLifecycle(PluginLifecycle.DISABLED);

        assertFalse(plugin.isEnabled());
    }

    @Test
    void testLoggingOccurs() {
        // Test that plugin logs during lifecycle events
        plugin.onLoad(mockContext);
        plugin.onEnable();

        // Verify logging occurred
        verify(mockLogger, atLeastOnce()).info(contains("Loading"));
        verify(mockLogger, atLeastOnce()).info(contains("Enabling"));
    }

    @Test
    void testContextAccess() {
        // Test that plugin correctly accesses context
        plugin.onLoad(mockContext);

        assertEquals(mockContext, plugin.getContext());
        assertEquals("plugins/TemplatePlugin", plugin.getDataFolder());
    }
}
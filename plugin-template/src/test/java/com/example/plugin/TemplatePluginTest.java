package com.example.plugin;

import fr.farmvivi.fluxcord.api.command.CommandService;
import fr.farmvivi.fluxcord.api.config.Configuration;
import fr.farmvivi.fluxcord.api.discord.DiscordAPI;
import fr.farmvivi.fluxcord.api.event.EventManager;
import fr.farmvivi.fluxcord.api.language.LanguageManager;
import fr.farmvivi.fluxcord.api.permissions.PermissionManager;
import fr.farmvivi.fluxcord.api.plugin.PluginContext;
import fr.farmvivi.fluxcord.api.plugin.PluginLifecycle;
import fr.farmvivi.fluxcord.api.storage.DataStorageManager;
import fr.farmvivi.fluxcord.api.storage.binary.BinaryStorageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for TemplatePlugin.
 * Demonstrates basic plugin testing patterns.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TemplatePluginTest {

    @Mock
    private PluginContext mockContext;

    @Mock
    private Logger mockLogger;

    @Mock
    private LanguageManager mockLanguageManager;

    @Mock
    private Configuration mockConfiguration;

    @Mock
    private PermissionManager mockPermissionManager;

    @Mock
    private EventManager mockEventManager;

    @Mock
    private DataStorageManager mockDataStorageManager;

    @Mock
    private BinaryStorageManager mockBinaryStorageManager;

    @Mock
    private CommandService mockCommandService;

    @Mock
    private DiscordAPI mockDiscordAPI;

    private TemplatePlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = new TemplatePlugin();

        // Setup mock context
        when(mockContext.getPluginName()).thenReturn("TemplatePlugin");
        when(mockContext.getPluginVersion()).thenReturn("1.0.0");
        when(mockContext.getLogger()).thenReturn(mockLogger);
        when(mockContext.getLanguageManager()).thenReturn(mockLanguageManager);
        when(mockContext.getConfiguration()).thenReturn(mockConfiguration);
        when(mockContext.getPermissionManager()).thenReturn(mockPermissionManager);
        when(mockContext.getEventManager()).thenReturn(mockEventManager);
        when(mockContext.getDataStorageManager()).thenReturn(mockDataStorageManager);
        when(mockContext.getBinaryStorageManager()).thenReturn(mockBinaryStorageManager);
        when(mockContext.getCommandService()).thenReturn(mockCommandService);
        when(mockContext.getDiscordAPI()).thenReturn(mockDiscordAPI);
        when(mockContext.getDataFolder()).thenReturn("plugins/TemplatePlugin");

        // Default config stubs
        when(mockConfiguration.getBoolean(anyString(), anyBoolean())).thenReturn(false);
        when(mockConfiguration.getInt(anyString(), anyInt())).thenReturn(0);

        // Language stubs
        when(mockLanguageManager.registerNamespace(anyString())).thenReturn(true);
        when(mockLanguageManager.getString(anyString())).thenReturn("ok");
        when(mockLanguageManager.getString(anyString(), any())).thenReturn("ok");
        when(mockLanguageManager.getString(any(), anyString())).thenReturn("ok");
        when(mockLanguageManager.getString(any(), anyString(), any())).thenReturn("ok");
        when(mockLanguageManager.getDefaultLocale()).thenReturn(java.util.Locale.ENGLISH);
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
        plugin.setLifecycle(PluginLifecycle.PRE_ENABLING);

        plugin.onEnable();
        plugin.setLifecycle(PluginLifecycle.ENABLED);

        assertTrue(plugin.isEnabled());

        plugin.onPreDisable();
        plugin.setLifecycle(PluginLifecycle.PRE_DISABLING);

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
        verify(mockLogger, atLeastOnce()).info(contains("Loading"), any(), any());
        verify(mockLogger, atLeastOnce()).info(contains("Enabling"), any(), any());
    }

    @Test
    void testContextAccess() {
        // Test that plugin correctly accesses context
        plugin.onLoad(mockContext);

        assertEquals(mockContext, plugin.getContext());
        assertEquals("plugins/TemplatePlugin", plugin.getDataFolder());
    }
}
package fr.farmvivi.fluxcord.plugins.music;

import fr.farmvivi.fluxcord.api.audio.AudioService;
import fr.farmvivi.fluxcord.api.command.CommandService;
import fr.farmvivi.fluxcord.api.config.Configuration;
import fr.farmvivi.fluxcord.api.language.LanguageManager;
import fr.farmvivi.fluxcord.api.permissions.PermissionManager;
import fr.farmvivi.fluxcord.api.plugin.PluginContext;
import fr.farmvivi.fluxcord.api.storage.DataStorageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MusicPlugin.
 */
public class MusicPluginTest {
    
    @Mock
    private PluginContext context;
    
    @Mock
    private AudioService audioService;
    
    @Mock
    private CommandService commandService;
    
    @Mock
    private Configuration configuration;
    
    @Mock
    private DataStorageManager dataStorageManager;
    
    @Mock
    private LanguageManager languageManager;
    
    @Mock
    private PermissionManager permissionManager;
    
    private MusicPlugin plugin;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup mock context
        when(context.getAudioService()).thenReturn(audioService);
        when(context.getCommandService()).thenReturn(commandService);
    when(context.getConfiguration()).thenReturn(configuration);
    when(context.getDataStorageManager()).thenReturn(dataStorageManager);
    when(context.getLanguageManager()).thenReturn(languageManager);
    when(context.getPermissionManager()).thenReturn(permissionManager);
        
        // Setup default configuration values
        when(configuration.getInt(anyString(), anyInt())).thenReturn(50);
        when(configuration.getBoolean(anyString(), anyBoolean())).thenReturn(true);
        when(configuration.getString(anyString(), anyString())).thenReturn("");
        
        plugin = new MusicPlugin();
    }
    
    @Test
    void testPluginInitialization() {
        // Load the plugin
        plugin.onLoad(context);
        plugin.onEnable();
        
        // Verify plugin is enabled
        assertTrue(plugin.isEnabled());
        assertNotNull(plugin.getMusicManager());
        assertNotNull(plugin.getPlaylistManager());
        
        // Verify permissions were registered
    verify(permissionManager, atLeast(6)).registerPermission(any(), eq(plugin));
        
        // Verify commands were registered
        verify(commandService, atLeast(10)).registerCommand(eq(plugin), any());
    }
    
    @Test
    void testPluginDisable() {
        // Enable the plugin first
        plugin.onLoad(context);
        plugin.onEnable();
        
        // Disable the plugin
        plugin.onDisable();
        
        // Verify plugin is disabled
        assertFalse(plugin.isEnabled());
        
        // Verify managers were shut down
        assertNotNull(plugin.getMusicManager()); // Manager should still exist but be shut down
    }
    
    @Test
    void testConfigurationLoading() {
        // Setup specific config values
        when(configuration.getInt("music.default_volume", 50)).thenReturn(75);
        when(configuration.getInt("music.max_queue_size", 100)).thenReturn(200);
        when(configuration.getBoolean("providers.spotify.enabled", true)).thenReturn(false);
        
        // Load the plugin
        plugin.onLoad(context);
        plugin.onEnable();
        
        // Verify configuration was loaded
        verify(configuration).getInt("music.default_volume", 50);
        verify(configuration).getInt("music.max_queue_size", 100);
        verify(configuration).getBoolean("providers.spotify.enabled", true);
    }
}
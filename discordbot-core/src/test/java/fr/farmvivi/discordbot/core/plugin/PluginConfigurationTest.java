package fr.farmvivi.discordbot.core.plugin;

import fr.farmvivi.discordbot.api.config.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PluginConfiguration versioning and default config copying functionality.
 */
class PluginConfigurationTest {

    @TempDir
    Path tempDir;

    @Mock
    private PluginClassLoader mockClassLoader;

    private String pluginName = "TestPlugin";
    private File pluginFolder;
    private File configFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up temporary plugin structure
        System.setProperty("plugins.dir", tempDir.resolve("plugins").toString());
        pluginFolder = tempDir.resolve("plugins").resolve(pluginName).toFile();
        configFile = new File(pluginFolder, "config.yml");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("plugins.dir");
    }

    @Test
    void testDefaultConfigCopyingFromJAR() throws ConfigurationException {
        // Given: no existing config file, but JAR contains default config
        String defaultConfig = """
            config_version: 1
            plugin_settings:
              enabled: true
              max_items: 100
            """;
        
        InputStream configStream = new ByteArrayInputStream(defaultConfig.getBytes());
        when(mockClassLoader.getResourceAsStream("config.yml")).thenReturn(configStream);
        
        // When: creating plugin configuration
        PluginConfiguration config = new PluginConfiguration(pluginName, mockClassLoader);
        
        // Then: config file is copied from JAR
        assertTrue(configFile.exists());
        assertEquals(1, config.getConfigVersion());
        assertTrue(config.getBoolean("plugin_settings.enabled"));
        assertEquals(100, config.getInt("plugin_settings.max_items"));
        
        verify(mockClassLoader).getResourceAsStream("config.yml");
    }

    @Test
    void testExistingConfigIsNotOverwritten() throws ConfigurationException, IOException {
        // Given: existing config file with custom settings
        pluginFolder.mkdirs();
        String existingConfig = """
            config_version: 1
            plugin_settings:
              enabled: false
              max_items: 200
              custom_setting: "user_value"
            """;
        Files.writeString(configFile.toPath(), existingConfig);
        
        // And: JAR contains different default config
        String defaultConfig = """
            config_version: 1
            plugin_settings:
              enabled: true
              max_items: 100
            """;
        InputStream configStream = new ByteArrayInputStream(defaultConfig.getBytes());
        when(mockClassLoader.getResourceAsStream("config.yml")).thenReturn(configStream);
        
        // When: creating plugin configuration
        PluginConfiguration config = new PluginConfiguration(pluginName, mockClassLoader);
        
        // Then: existing config is preserved
        assertFalse(config.getBoolean("plugin_settings.enabled")); // User's setting
        assertEquals(200, config.getInt("plugin_settings.max_items")); // User's setting
        assertEquals("user_value", config.getString("plugin_settings.custom_setting"));
        
        // And: default config is not copied
        verify(mockClassLoader, never()).getResourceAsStream("config.yml");
    }

    @Test
    void testLegacyConfigMigration() throws ConfigurationException, IOException {
        // Given: existing config without version
        pluginFolder.mkdirs();
        String legacyConfig = """
            plugin_settings:
              enabled: true
              max_items: 150
            """;
        Files.writeString(configFile.toPath(), legacyConfig);
        
        // When: creating plugin configuration
        PluginConfiguration config = new PluginConfiguration(pluginName, mockClassLoader);
        
        // Then: version is added
        assertEquals(1, config.getConfigVersion());
        assertTrue(config.getBoolean("plugin_settings.enabled"));
        assertEquals(150, config.getInt("plugin_settings.max_items"));
        
        // And: backup is created
        File[] backupFiles = pluginFolder.listFiles((dir, name) -> 
            name.startsWith("config.yml.backup."));
        assertNotNull(backupFiles);
        assertTrue(backupFiles.length > 0);
    }

    @Test
    void testNoDefaultConfigInJAR() throws ConfigurationException {
        // Given: no existing config file and no default in JAR
        when(mockClassLoader.getResourceAsStream("config.yml")).thenReturn(null);
        
        // When: creating plugin configuration
        PluginConfiguration config = new PluginConfiguration(pluginName, mockClassLoader);
        
        // Then: empty configuration is created
        assertFalse(configFile.exists()); // No file created if no default
        assertEquals(0, config.getConfigVersion()); // No version if no config
        
        verify(mockClassLoader).getResourceAsStream("config.yml");
    }

    @Test
    void testDataFolderPath() throws ConfigurationException {
        // Given: plugin configuration
        PluginConfiguration config = new PluginConfiguration(pluginName, mockClassLoader);
        
        // When: getting data folder path
        String dataFolder = config.getPluginDataFolder();
        
        // Then: correct path is returned
        assertTrue(dataFolder.endsWith("plugins" + File.separator + pluginName));
        assertTrue(new File(dataFolder).exists());
    }

    @Test
    void testConfigurationPersistence() throws ConfigurationException {
        // Given: plugin configuration with default values
        String defaultConfig = """
            config_version: 1
            test_setting: "initial_value"
            """;
        InputStream configStream = new ByteArrayInputStream(defaultConfig.getBytes());
        when(mockClassLoader.getResourceAsStream("config.yml")).thenReturn(configStream);
        
        PluginConfiguration config = new PluginConfiguration(pluginName, mockClassLoader);
        
        // When: modifying and saving configuration
        config.set("test_setting", "modified_value");
        config.set("new_setting", "new_value");
        config.save();
        
        // Then: changes are persisted
        PluginConfiguration reloadedConfig = new PluginConfiguration(pluginName, null);
        assertEquals("modified_value", reloadedConfig.getString("test_setting"));
        assertEquals("new_value", reloadedConfig.getString("new_setting"));
        assertEquals(1, reloadedConfig.getConfigVersion());
    }

    @Test
    void testConfigurationDefaults() throws ConfigurationException {
        // Given: minimal default config
        String defaultConfig = """
            config_version: 1
            basic_setting: "default"
            """;
        InputStream configStream = new ByteArrayInputStream(defaultConfig.getBytes());
        when(mockClassLoader.getResourceAsStream("config.yml")).thenReturn(configStream);
        
        // When: creating configuration and accessing values with defaults
        PluginConfiguration config = new PluginConfiguration(pluginName, mockClassLoader);
        
        // Then: defaults work correctly
        assertEquals("default", config.getString("basic_setting"));
        assertEquals("fallback", config.getString("missing_setting", "fallback"));
        assertEquals(42, config.getInt("missing_int", 42));
        assertFalse(config.getBoolean("missing_bool", false));
    }

    @Test
    void testFolderCreation() throws ConfigurationException {
        // Given: no existing plugin folder
        assertFalse(pluginFolder.exists());
        
        // When: creating plugin configuration
        new PluginConfiguration(pluginName, mockClassLoader);
        
        // Then: plugin folder is created
        assertTrue(pluginFolder.exists());
        assertTrue(pluginFolder.isDirectory());
    }
}
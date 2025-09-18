package fr.farmvivi.discordbot.core.config;

import fr.farmvivi.discordbot.api.config.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CoreConfiguration versioning and migration functionality.
 */
class CoreConfigurationTest {

    @TempDir
    Path tempDir;
    
    private File configFile;

    @BeforeEach
    void setUp() {
        configFile = tempDir.resolve("config.yml").toFile();
    }

    @Test
    void testNewConfigurationCreation() throws ConfigurationException {
        // Given: no existing configuration file
        assertFalse(configFile.exists());
        
        // When: creating a new CoreConfiguration
        CoreConfiguration config = new CoreConfiguration(configFile);
        
        // Then: config file is created with current version
        assertTrue(configFile.exists());
        assertEquals(1, config.getConfigVersion());
        assertTrue(config.contains("discord.token"));
        assertTrue(config.contains("language.default"));
        assertTrue(config.contains("commands.default-prefix"));
    }

    @Test
    void testLegacyConfigurationMigration() throws ConfigurationException, IOException {
        // Given: a legacy configuration without version
        String legacyConfig = """
            discord:
              token: "test_token"
            language:
              default: "en-US"
            commands:
              default-prefix: "!"
            """;
        Files.writeString(configFile.toPath(), legacyConfig);
        
        // When: loading the configuration
        CoreConfiguration config = new CoreConfiguration(configFile);
        
        // Then: version is added and migration occurs
        assertEquals(1, config.getConfigVersion());
        assertEquals("test_token", config.getString("discord.token"));
        assertEquals("en-US", config.getString("language.default"));
        assertEquals("!", config.getString("commands.default-prefix"));
        
        // And: backup file is created
        File[] backupFiles = configFile.getParentFile().listFiles((dir, name) -> 
            name.startsWith("config.yml.backup."));
        assertNotNull(backupFiles);
        assertTrue(backupFiles.length > 0);
    }

    @Test
    void testConfigurationValidation() throws ConfigurationException, IOException {
        // Given: a configuration with missing token
        String invalidConfig = """
            config_version: 1
            discord:
              token: "YOUR_BOT_TOKEN"
            language:
              default: "en-US"
            commands:
              default-prefix: "!"
            """;
        Files.writeString(configFile.toPath(), invalidConfig);
        
        // When/Then: validation should fail
        CoreConfiguration config = new CoreConfiguration(configFile);
        assertThrows(ConfigurationException.class, config::validateConfiguration);
    }

    @Test
    void testValidConfiguration() throws ConfigurationException, IOException {
        // Given: a valid configuration
        String validConfig = """
            config_version: 1
            discord:
              token: "valid_token_here"
            language:
              default: "en-US"
            commands:
              default-prefix: "!"
            """;
        Files.writeString(configFile.toPath(), validConfig);
        
        // When: validating configuration
        CoreConfiguration config = new CoreConfiguration(configFile);
        
        // Then: validation should pass
        assertDoesNotThrow(config::validateConfiguration);
    }

    @Test
    void testEnvironmentVariableSupport() throws ConfigurationException, IOException {
        // Given: a configuration with environment variable placeholders
        String configWithEnvVars = """
            config_version: 1
            discord:
              token: "${DISCORD_TOKEN:default_token}"
            language:
              default: "en-US"
            commands:
              default-prefix: "!"
            """;
        Files.writeString(configFile.toPath(), configWithEnvVars);
        
        // When: loading configuration
        CoreConfiguration config = new CoreConfiguration(configFile);
        
        // Then: environment variable support is maintained
        // Note: This would resolve to the environment variable or the default
        assertNotNull(config.getString("discord.token"));
    }

    @Test
    void testConfigurationPersistence() throws ConfigurationException {
        // Given: a new configuration
        CoreConfiguration config = new CoreConfiguration(configFile);
        
        // When: modifying and saving
        config.set("custom.setting", "test_value");
        config.save();
        
        // Then: changes are persisted
        CoreConfiguration reloadedConfig = new CoreConfiguration(configFile);
        assertEquals("test_value", reloadedConfig.getString("custom.setting"));
        assertEquals(1, reloadedConfig.getConfigVersion());
    }
}
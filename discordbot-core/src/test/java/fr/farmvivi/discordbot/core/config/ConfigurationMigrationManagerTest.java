package fr.farmvivi.discordbot.core.config;

import fr.farmvivi.discordbot.api.config.Configuration;
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
 * Tests for ConfigurationMigrationManager.
 */
class ConfigurationMigrationManagerTest {
    
    @TempDir
    Path tempDir;
    
    private ConfigurationMigrationManager migrationManager;
    
    @BeforeEach
    void setUp() {
        migrationManager = new ConfigurationMigrationManager();
    }
    
    @Test
    void testRegisterMigrator() {
        TestMigrator migrator = new TestMigrator("1.0.0", "1.1.0");
        migrationManager.registerMigrator(migrator);
        
        // Should not throw exception
        assertDoesNotThrow(() -> migrationManager.registerMigrator(migrator));
    }
    
    @Test
    void testNeedsMigration() throws IOException, ConfigurationException {
        File configFile = tempDir.resolve("test-config.yml").toFile();
        Files.writeString(configFile.toPath(), "config_version: \"1.0.0\"\ntest: value");
        
        YamlConfiguration config = new YamlConfiguration(configFile);
        
        assertTrue(migrationManager.needsMigration(config, "1.1.0"));
        assertFalse(migrationManager.needsMigration(config, "1.0.0"));
        assertFalse(migrationManager.needsMigration(config, "0.9.0"));
    }
    
    @Test
    void testSuccessfulMigration() throws IOException, ConfigurationException {
        File configFile = tempDir.resolve("test-config.yml").toFile();
        Files.writeString(configFile.toPath(), "config_version: \"1.0.0\"\nold_setting: value");
        
        YamlConfiguration config = new YamlConfiguration(configFile);
        
        TestMigrator migrator = new TestMigrator("1.0.0", "1.1.0");
        migrationManager.registerMigrator(migrator);
        
        migrationManager.migrateConfiguration(config, "1.1.0", false);
        
        assertEquals("1.1.0", migrationManager.getCurrentVersion(config));
        assertTrue(config.contains("new_setting"));
        assertEquals("migrated_value", config.getString("new_setting"));
    }
    
    @Test
    void testMigrationPath() throws IOException, ConfigurationException {
        File configFile = tempDir.resolve("test-config.yml").toFile();
        Files.writeString(configFile.toPath(), "config_version: \"1.0.0\"\ntest: value");
        
        YamlConfiguration config = new YamlConfiguration(configFile);
        
        // Register a chain of migrations
        migrationManager.registerMigrator(new TestMigrator("1.0.0", "1.1.0"));
        migrationManager.registerMigrator(new TestMigrator("1.1.0", "1.2.0"));
        
        migrationManager.migrateConfiguration(config, "1.2.0", false);
        
        assertEquals("1.2.0", migrationManager.getCurrentVersion(config));
    }
    
    @Test
    void testMigrationValidationFailure() throws IOException, ConfigurationException {
        File configFile = tempDir.resolve("test-config.yml").toFile();
        Files.writeString(configFile.toPath(), "config_version: \"1.0.0\"\ntest: value");
        
        YamlConfiguration config = new YamlConfiguration(configFile);
        
        FailingMigrator migrator = new FailingMigrator("1.0.0", "1.1.0");
        migrationManager.registerMigrator(migrator);
        
        assertThrows(ConfigurationException.class, () -> {
            migrationManager.migrateConfiguration(config, "1.1.0", false);
        });
    }
    
    @Test
    void testCurrentVersionWithDefault() throws IOException, ConfigurationException {
        File configFile = tempDir.resolve("test-config.yml").toFile();
        Files.writeString(configFile.toPath(), "test: value");
        
        YamlConfiguration config = new YamlConfiguration(configFile);
        
        assertEquals("1.0.0", migrationManager.getCurrentVersion(config));
    }
    
    @Test
    void testSetVersion() throws IOException, ConfigurationException {
        File configFile = tempDir.resolve("test-config.yml").toFile();
        Files.writeString(configFile.toPath(), "test: value");
        
        YamlConfiguration config = new YamlConfiguration(configFile);
        
        migrationManager.setVersion(config, "2.0.0");
        assertEquals("2.0.0", migrationManager.getCurrentVersion(config));
    }
    
    /**
     * Test migrator that adds a new setting.
     */
    private static class TestMigrator implements ConfigurationMigrator {
        private final String fromVersion;
        private final String toVersion;
        
        public TestMigrator(String fromVersion, String toVersion) {
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
        }
        
        @Override
        public String getFromVersion() {
            return fromVersion;
        }
        
        @Override
        public String getToVersion() {
            return toVersion;
        }
        
        @Override
        public void migrate(Configuration configuration) throws ConfigurationException {
            configuration.set("new_setting", "migrated_value");
        }
        
        @Override
        public String getDescription() {
            return "Test migration from " + fromVersion + " to " + toVersion;
        }
    }
    
    /**
     * Test migrator that always fails validation.
     */
    private static class FailingMigrator implements ConfigurationMigrator {
        private final String fromVersion;
        private final String toVersion;
        
        public FailingMigrator(String fromVersion, String toVersion) {
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
        }
        
        @Override
        public String getFromVersion() {
            return fromVersion;
        }
        
        @Override
        public String getToVersion() {
            return toVersion;
        }
        
        @Override
        public void migrate(Configuration configuration) throws ConfigurationException {
            // This migrator doesn't actually migrate anything
        }
        
        @Override
        public boolean canMigrate(Configuration configuration) {
            return false; // Always fail validation
        }
        
        @Override
        public String getDescription() {
            return "Failing test migration";
        }
    }
}
# Configuration Versioning and Migration Guide

This guide explains the new configuration versioning and migration system introduced in DiscordBot Core 2.3.27.

## Overview

The configuration system now supports:
- **Automatic default configuration copying** from plugin JARs
- **Semantic versioning** for configurations
- **Automatic migration** when upgrading plugins or core
- **Backup creation** before migrations
- **Validation** of configuration structure and values

## Core Configuration

### Versioned Configuration

The core configuration (`config.yml`) now includes a `config_version` field:

```yaml
config_version: "2.3.27"

discord:
  token: "YOUR_BOT_TOKEN"
  # ... other settings
```

### Automatic Default Configuration

When the bot starts for the first time, it automatically creates a `config.yml` file with sensible defaults from the embedded `default-core-config.yml` template.

### Migration

When upgrading DiscordBot Core, the configuration is automatically migrated to the new version. Backups are created before migration:

```
config.yml.backup.20231218_143022
```

## Plugin Configuration

### Basic Plugin Configuration

For simple plugins, the existing `PluginConfiguration` class continues to work but now supports automatic default configuration copying:

```java
public class MyPlugin extends AbstractPlugin {
    @Override
    public void onEnable() {
        // Configuration is automatically loaded with defaults from default-config.yml in plugin JAR
        boolean enabled = getConfiguration().getBoolean("enabled", true);
    }
}
```

### Versioned Plugin Configuration

For plugins that need version management and migrations, use `VersionedPluginConfiguration`:

```java
public class MyAdvancedPlugin extends AbstractPlugin {
    private static final String CONFIG_VERSION = "1.2.0";
    
    @Override
    public void onEnable() {
        // Setup migrations
        setupConfigurationMigrations();
        
        // Load configuration - automatically migrated if needed
        loadConfiguration();
    }
    
    private void setupConfigurationMigrations() {
        if (getPluginLoader() instanceof PluginManager pluginManager) {
            ConfigurationMigrationManager migrationManager = pluginManager.getMigrationManager();
            
            // Register migrations
            migrationManager.registerMigrator(new MyPluginMigration_1_0_0_to_1_1_0());
            migrationManager.registerMigrator(new MyPluginMigration_1_1_0_to_1_2_0());
        }
    }
}
```

## Creating Migrations

### Plugin Migration Example

```java
public class MyPluginMigration_1_0_0_to_1_1_0 implements ConfigurationMigrator {
    
    @Override
    public String getFromVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getToVersion() {
        return "1.1.0";
    }
    
    @Override
    public void migrate(Configuration configuration) throws ConfigurationException {
        // Add new settings
        if (!configuration.contains("new_feature.enabled")) {
            configuration.set("new_feature.enabled", true);
        }
        
        // Migrate old settings
        if (configuration.contains("old_setting")) {
            String oldValue = configuration.getString("old_setting");
            configuration.set("new_settings.converted_value", oldValue);
            // Keep old setting for backward compatibility
        }
        
        // Update nested structures
        if (!configuration.contains("advanced.timeout")) {
            configuration.set("advanced.timeout", 30);
        }
    }
    
    @Override
    public boolean canMigrate(Configuration configuration) {
        // Validate that we can safely migrate
        return configuration.contains("config_version");
    }
    
    @Override
    public String getDescription() {
        return "Add new feature settings and convert old configuration format";
    }
}
```

### Core Migration Example

```java
public class CoreMigration_2_3_0_to_2_3_27 implements ConfigurationMigrator {
    
    @Override
    public String getFromVersion() {
        return "2.3.0";
    }
    
    @Override
    public String getToVersion() {
        return "2.3.27";
    }
    
    @Override
    public void migrate(Configuration configuration) throws ConfigurationException {
        // Add new plugin system settings
        if (!configuration.contains("plugins.config.auto_migrate")) {
            configuration.set("plugins.config.auto_migrate", true);
            configuration.set("plugins.config.backup_before_migration", true);
            configuration.set("plugins.config.max_backup_files", 5);
        }
        
        // Add security settings
        if (!configuration.contains("security.rate_limiting.enabled")) {
            configuration.set("security.rate_limiting.enabled", true);
            configuration.set("security.rate_limiting.requests_per_minute", 60);
        }
        
        // Migrate old logging format
        if (configuration.contains("log.level") && !configuration.contains("logging.level")) {
            String oldLevel = configuration.getString("log.level");
            configuration.set("logging.level", oldLevel);
            configuration.set("logging.file.enabled", true);
            configuration.set("logging.console.enabled", true);
        }
    }
}
```

## Default Configuration Files

### Plugin Default Configuration

Create a `default-config.yml` file in your plugin's `src/main/resources/` directory:

```yaml
# Default configuration for MyPlugin
config_version: "1.2.0"

plugin:
  name: "MyPlugin"
  enabled: true

features:
  advanced_mode: false
  auto_save: true
  save_interval: 300

messages:
  welcome: "Welcome to MyPlugin!"
  goodbye: "Thanks for using MyPlugin!"

paths:
  data_dir: "data/"
  backup_dir: "backups/"
```

### Configuration Structure

- `config_version`: **Required** - semantic version of the configuration schema
- `plugin.name`: Plugin name for validation
- `plugin.enabled`: Whether the plugin is enabled
- Feature-specific sections organized logically
- Sensible defaults for all settings

## Best Practices

### Version Management

1. **Use semantic versioning** for configuration versions
2. **Increment versions** when adding/changing configuration structure:
   - **Patch** (1.0.0 → 1.0.1): Bug fixes, no structure changes
   - **Minor** (1.0.0 → 1.1.0): New optional settings, backward compatible
   - **Major** (1.0.0 → 2.0.0): Breaking changes, removed settings

### Migration Best Practices

1. **Always validate** before migrating (`canMigrate()`)
2. **Preserve backward compatibility** when possible
3. **Provide clear descriptions** for each migration
4. **Test migrations thoroughly** with various configuration states
5. **Chain migrations** for complex multi-version updates

### Configuration Design

1. **Group related settings** in logical sections
2. **Provide sensible defaults** for all settings
3. **Document settings** with comments in default config
4. **Validate configuration** after loading
5. **Use environment variables** for sensitive data

### Error Handling

1. **Graceful fallbacks** when migration fails
2. **Detailed logging** of migration process
3. **Backup creation** before risky operations
4. **Rollback capabilities** for failed migrations

## Migration Process

### Automatic Migration Flow

1. **Plugin loads** with target version specified
2. **Current version detected** from `config_version` field
3. **Migration path calculated** using registered migrators
4. **Backup created** (if enabled)
5. **Migrations applied** in sequence
6. **Version updated** after each successful migration
7. **Configuration saved** with new version
8. **Validation performed** on final result

### Manual Migration

You can also trigger migrations manually:

```java
ConfigurationMigrationManager migrationManager = pluginManager.getMigrationManager();

// Check if migration is needed
if (migrationManager.needsMigration(config, "2.0.0")) {
    // Perform migration with backup
    migrationManager.migrateConfiguration(config, "2.0.0", true);
}
```

## Debugging

### Logging

The migration system provides detailed logging:

```
[INFO] Starting configuration migration from 1.0.0 to 1.2.0
[INFO] Created configuration backup: config.yml.backup.20231218_143022
[INFO] Applying migration: Add new feature settings (1.0.0 → 1.1.0)
[INFO] Applying migration: Convert old format to new structure (1.1.0 → 1.2.0)
[INFO] Configuration migration completed successfully to version 1.2.0
```

### Validation

Configuration validation helps catch issues early:

```java
public boolean validateConfiguration() {
    try {
        // Check required fields
        String token = getString("discord.token", "");
        if (token.isEmpty() || "YOUR_BOT_TOKEN".equals(token)) {
            logger.warn("Discord token is not configured properly");
            return false;
        }
        
        return true;
    } catch (Exception e) {
        logger.warn("Configuration validation failed: {}", e.getMessage());
        return false;
    }
}
```

## Migration Examples

See the included examples:
- [`AudioPluginMigration_1_0_0_to_1_1_0`](../examples/audio/src/main/java/fr/farmvivi/discordbot/examples/audio/config/AudioPluginMigration_1_0_0_to_1_1_0.java) - Plugin migration example
- [`CoreMigration_2_3_0_to_2_3_27`](../discordbot-core/src/main/java/fr/farmvivi/discordbot/core/config/migrations/CoreMigration_2_3_0_to_2_3_27.java) - Core migration example
- [`AudioExamplePlugin`](../examples/audio/src/main/java/fr/farmvivi/discordbot/examples/audio/AudioExamplePlugin.java) - Plugin using versioned configuration

## API Reference

### ConfigurationMigrator Interface

```java
public interface ConfigurationMigrator {
    String getFromVersion();
    String getToVersion();
    void migrate(Configuration configuration) throws ConfigurationException;
    boolean canMigrate(Configuration configuration);
    String getDescription();
}
```

### ConfigurationMigrationManager

```java
public class ConfigurationMigrationManager {
    void registerMigrator(ConfigurationMigrator migrator);
    boolean needsMigration(Configuration config, String targetVersion);
    void migrateConfiguration(Configuration config, String targetVersion, boolean backup);
    String getCurrentVersion(Configuration configuration);
    void setVersion(Configuration configuration, String version);
}
```

### VersionUtils

```java
public final class VersionUtils {
    static int compareVersions(String version1, String version2);
    static boolean isValidVersion(String version);
    static Version parseVersion(String versionString);
    static String normalizeVersion(String version);
}
```
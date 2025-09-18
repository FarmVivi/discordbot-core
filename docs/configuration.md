# Configuration Guide

DiscordBot Core uses a flexible YAML-based configuration system with **automatic versioning**, **migration support**, environment variable support, and plugin-specific settings.

## Configuration Versioning

### Automatic Version Management

All configuration files (both core and plugins) now include automatic version tracking:

```yaml
# Configuration version (automatically managed)
config_version: 1
```

**Key Features:**
- **Automatic Migration**: When the bot detects an older configuration version, it automatically migrates to the newer format
- **Backup Creation**: Before any migration, a timestamped backup is created
- **Plugin Config Copying**: Plugin configs are automatically copied from JAR files when missing
- **Validation**: Configuration validation ensures required settings are present

### Version History

- **Version 0**: Legacy configurations without version tracking
- **Version 1**: Current version with structured YAML and versioning support

## Core Configuration

### Main Configuration File (`config.yml`)

The core configuration is automatically created with comprehensive defaults if missing:

```yaml
# Discord Bot Core Configuration
# Configuration version (automatically managed)
config_version: 1

# Discord Bot Configuration
discord:
  # Bot token from Discord Developer Portal
  token: "YOUR_BOT_TOKEN"
  
  # Bot activity settings
  activity:
    type: "PLAYING"  # PLAYING, LISTENING, WATCHING, COMPETING
    text: "with plugins!"
  
  # Default command prefix for text commands
  prefix: "!"
  
  # Enable slash commands
  enable_slash_commands: true

# Database Configuration
database:
  # Database type: file, mysql, postgresql
  type: "file"
  
  # File database settings
  file:
    path: "data/database.db"
  
  # MySQL settings (when type: mysql)
  mysql:
    host: "${DB_HOST:localhost}"
    port: "${DB_PORT:3306}"
    database: "${DB_NAME:discordbot}"
    username: "${DB_USER:root}"
    password: "${DB_PASSWORD:}"
    
# Storage Configuration
storage:
  # Binary storage backend: file, s3
  binary_storage: "file"
  
  # File storage settings
  file:
    base_path: "data/files/"
  
  # S3 storage settings (when binary_storage: s3)
  s3:
    bucket: "${S3_BUCKET}"
    region: "${S3_REGION:us-east-1}"
    access_key: "${S3_ACCESS_KEY}"
    secret_key: "${S3_SECRET_KEY}"

# Language and Localization
language:
  # Default language for the bot
  default: "en-US"
  
  # Available languages
  supported:
    - "en-US"
    - "fr-FR"
    - "es-ES"
    - "de-DE"

# Logging Configuration
logging:
  # Log level: trace, debug, info, warn, error
  level: "info"
  
  # Log to file
  file:
    enabled: true
    path: "logs/bot.log"
    max_size: "10MB"
    max_files: 10
  
  # Log to console
  console:
    enabled: true
    colored: true

# Plugin Configuration
plugins:
  # Plugin directory
  directory: "plugins/"
  
  # Auto-reload plugins on file change (development only)
  auto_reload: false
  
  # Plugin loading timeout
  load_timeout: 30000  # milliseconds

# Security Settings
security:
  # Rate limiting
  rate_limit:
    enabled: true
    requests_per_minute: 60
    burst_size: 10
  
  # Command cooldowns
  cooldowns:
    global: 1000      # milliseconds
    per_user: 3000    # milliseconds
    per_guild: 5000   # milliseconds

# Performance Settings
performance:
  # Thread pool sizes
  threads:
    command_executor: 10
    event_handler: 5
    audio_processor: 3
  
  # Memory management
  memory:
    gc_interval: 300000  # 5 minutes
    max_heap_usage: 0.8  # 80%
  
  # Cache settings
  cache:
    user_cache_size: 10000
    guild_cache_size: 1000
    ttl: 3600000  # 1 hour
```

## Environment Variables

Use environment variables for sensitive data and deployment-specific settings:

```bash
# Required
DISCORD_TOKEN=your_discord_bot_token

# Database (if using MySQL/PostgreSQL)
DB_HOST=localhost
DB_PORT=3306
DB_NAME=discordbot
DB_USER=bot_user
DB_PASSWORD=secure_password

# Storage (if using S3)
S3_BUCKET=my-bot-storage
S3_REGION=us-east-1
S3_ACCESS_KEY=your_access_key
S3_SECRET_KEY=your_secret_key

# Optional overrides
BOT_PREFIX=!
LOG_LEVEL=info
PLUGIN_DIR=plugins/
```

### Environment Variable Syntax

```yaml
# Direct substitution
token: "${DISCORD_TOKEN}"

# With default value
host: "${DB_HOST:localhost}"
port: "${DB_PORT:3306}"

# Complex expressions
database_url: "jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:discordbot}"
```

### Automatic Plugin Configuration Management

**DiscordBot Core now automatically manages plugin configurations:**

#### Default Config File Copying

When a plugin is loaded, the system automatically:

1. **Checks for existing config**: If `plugins/{PluginName}/config.yml` exists, it uses that
2. **Copies from JAR**: If no config exists, it copies the default `config.yml` from the plugin's JAR file
3. **Creates plugin folder**: Automatically creates the plugin's data folder if needed
4. **Logs the process**: Provides clear logging about configuration management

#### Plugin Configuration Versioning

Each plugin configuration includes version tracking:

```yaml
# Example Plugin Configuration
# Configuration version (automatically managed)  
config_version: 1

# Plugin-specific settings
plugin_settings:
  feature_enabled: true
  max_items: 100
```

#### Migration Process

When a plugin's configuration version is outdated:

1. **Backup Creation**: A timestamped backup is created (e.g., `config.yml.backup.1640995200000`)
2. **Step-by-step Migration**: Configurations are migrated incrementally (v1→v2→v3)
3. **Error Handling**: Migration stops on errors to prevent data corruption
4. **Logging**: Detailed logs show the migration progress

### Plugin-Specific Settings

Each plugin can have its own configuration section:

```yaml
# Plugin configurations
plugins:
  MusicBot:
    default_volume: 50
    max_queue_size: 100
    enable_spotify: true
    auto_leave_timeout: 300000
  
  AIAudio:
    openai_api_key: "${OPENAI_API_KEY}"
    transcription_language: "en-US"
    confidence_threshold: 0.8
  
  ModeratorBot:
    auto_moderation: true
    warning_threshold: 3
    ban_duration: 604800  # 7 days
```

### Per-Guild Configuration

Configure different settings for each Discord server:

```yaml
# Guild-specific overrides
guilds:
  "123456789012345678":  # Guild ID
    language: "fr-FR"
    prefix: "?"
    plugins:
      MusicBot:
        default_volume: 75
      ModeratorBot:
        auto_moderation: false
  
  "876543210987654321":  # Another guild
    language: "es-ES"
    prefix: "/"
```

## Development Configuration

### Development Mode

```yaml
# Development settings
development:
  enabled: true
  
  # Hot reload plugins
  hot_reload: true
  
  # Debug logging
  debug_mode: true
  
  # Disable rate limiting
  disable_rate_limits: true
  
  # Test server
  test_guild: "123456789012345678"
```

### Testing Configuration

```yaml
# Testing environment
testing:
  # Use in-memory database
  database:
    type: "memory"
  
  # Disable external services
  disable_apis: true
  
  # Mock services
  mock_discord: true
  mock_storage: true
```

## Docker Configuration

### Docker Compose Environment

```yaml
# docker-compose.yml
version: '3.8'
services:
  discordbot:
    build: .
    environment:
      - DISCORD_TOKEN=${DISCORD_TOKEN}
      - DB_HOST=database
      - DB_NAME=discordbot
      - DB_USER=bot
      - DB_PASSWORD=${DB_PASSWORD}
    volumes:
      - ./data:/app/data
      - ./plugins:/app/plugins
      - ./config.yml:/app/config.yml
    depends_on:
      - database
  
  database:
    image: mysql:8.0
    environment:
      - MYSQL_DATABASE=discordbot
      - MYSQL_USER=bot
      - MYSQL_PASSWORD=${DB_PASSWORD}
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

### Environment File (`.env`)

```bash
# Discord
DISCORD_TOKEN=your_bot_token_here

# Database
DB_PASSWORD=secure_database_password
MYSQL_ROOT_PASSWORD=secure_root_password

# Storage
S3_BUCKET=my-bot-storage
S3_ACCESS_KEY=your_access_key
S3_SECRET_KEY=your_secret_key

# APIs
OPENAI_API_KEY=your_openai_key
GOOGLE_CREDENTIALS_PATH=/app/credentials/google.json
```

## Configuration Validation

### Schema Validation

The bot validates configuration on startup:

```yaml
# Validation rules applied automatically
discord:
  token: # Required, must be valid Discord token format
    required: true
    pattern: "^[A-Za-z0-9._-]+$"
  
database:
  type: # Must be one of supported types
    enum: ["file", "mysql", "postgresql", "memory"]
  
logging:
  level: # Must be valid log level
    enum: ["trace", "debug", "info", "warn", "error"]
```

### Configuration Errors

Common configuration errors and solutions:

```yaml
# ❌ Invalid token format
discord:
  token: "invalid_token"
# ✅ Correct format
discord:
  token: "${DISCORD_TOKEN}"

# ❌ Missing required field
database:
  type: "mysql"
  # mysql section missing
# ✅ Complete configuration
database:
  type: "mysql"
  mysql:
    host: "localhost"
    database: "discordbot"
    username: "bot"
    password: "${DB_PASSWORD}"

# ❌ Invalid enum value
logging:
  level: "verbose"  # Not a valid level
# ✅ Valid level
logging:
  level: "debug"
```

## Advanced Configuration

### Conditional Configuration

```yaml
# Profile-based configuration
profiles:
  production:
    logging:
      level: "warn"
      file:
        enabled: true
    performance:
      threads:
        command_executor: 20
  
  development:
    logging:
      level: "debug"
      console:
        colored: true
    development:
      enabled: true
      hot_reload: true

# Active profile
active_profile: "${ENVIRONMENT:development}"
```

### Dynamic Configuration

```yaml
# Configuration that can be changed at runtime
dynamic:
  # Can be modified via admin commands
  rate_limits:
    modifiable: true
  
  # Plugin settings that can be changed
  plugin_settings:
    modifiable: true
    
  # Static settings that require restart
  database:
    modifiable: false
```

### Configuration Templates

```yaml
# Template for new guilds
guild_template:
  language: "en-US"
  prefix: "!"
  plugins:
    enabled: ["MusicBot", "ModeratorBot"]
    MusicBot:
      default_volume: 50
    ModeratorBot:
      auto_moderation: true

# Plugin defaults
plugin_defaults:
  permissions:
    admin: ["ADMINISTRATOR"]
    moderator: ["MANAGE_MESSAGES", "KICK_MEMBERS"]
  
  rate_limits:
    commands: 30  # per minute
    events: 100   # per minute
```

## Configuration Best Practices

### Security

1. **Never commit secrets**: Use environment variables for tokens and passwords
2. **Validate input**: Ensure configuration values are in expected ranges
3. **Use least privilege**: Configure minimal required permissions
4. **Regular rotation**: Rotate API keys and tokens regularly

### Performance

1. **Cache frequently accessed values**: Store parsed configuration in memory
2. **Validate on startup**: Catch configuration errors early
3. **Use appropriate data types**: Numbers for numeric values, booleans for flags
4. **Configure connection pools**: Set appropriate database connection limits

### Maintainability

1. **Document all options**: Include comments explaining each setting
2. **Use consistent naming**: Follow snake_case for YAML keys
3. **Group related settings**: Organize configuration into logical sections
4. **Provide examples**: Include example configurations for common scenarios

## Troubleshooting

### Common Issues

1. **Bot won't start**: Check Discord token and permissions
2. **Database connection failed**: Verify database credentials and connectivity
3. **Plugins not loading**: Check plugin directory path and permissions
4. **Commands not working**: Verify bot has necessary Discord permissions

### Debug Mode

Enable debug mode for detailed configuration logging:

```yaml
logging:
  level: "debug"

development:
  enabled: true
  debug_mode: true
```

### Configuration Dump

Use the `!config dump` command (if available) to export current configuration for debugging.

## Migration

### Upgrading Configuration

When upgrading DiscordBot Core, configuration migrations may be needed:

```bash
# Backup current configuration
cp config.yml config.yml.backup

# Run migration tool
java -jar discordbot-core.jar --migrate-config

# Verify new configuration
java -jar discordbot-core.jar --validate-config
```

### Automatic Configuration Migration

**DiscordBot Core now provides automatic configuration migration with the following features:**

#### Migration Process

1. **Version Detection**: The system automatically detects configuration versions
2. **Backup Creation**: Before any migration, backups are created with timestamps
3. **Step-by-step Migration**: Configurations are migrated incrementally to prevent data loss
4. **Validation**: Post-migration validation ensures configurations are valid
5. **Rollback Support**: Backups allow manual rollback if needed

#### Migration Commands

While migration is automatic, you can also trigger manual operations:

```bash
# Force configuration validation
java -jar discordbot-core.jar --validate-config

# Show current configuration versions
java -jar discordbot-core.jar --show-config-versions

# Create manual backup (optional)
cp config.yml config.yml.manual.backup
```

#### Migration Logging

The system provides detailed logging during migrations:

```
[INFO] Core configuration version 0 detected, migrating to version 1
[INFO] Created configuration backup at config.yml.backup.1640995200000
[INFO] Applied migration 0 -> 1 for core configuration
[INFO] Successfully migrated core configuration to version 1
[INFO] Plugin MusicBot configuration version 0 detected, migrating to version 1  
[INFO] Applied migration 0 -> 1 for plugin MusicBot
[INFO] Successfully migrated plugin MusicBot configuration to version 1
```

### Version Compatibility

Each version of DiscordBot Core supports specific configuration schema versions:

```yaml
# Configuration version (automatically managed)
config_version: 1

# Compatibility warnings will be shown for older versions
```

**Migration Path:**
- **Version 0** (Legacy): Configurations without version tracking
- **Version 1** (Current): Structured configurations with full versioning support

### Best Practices for Configuration Management

#### For Plugin Developers

1. **Include Default Config**: Always include a `config.yml` in your plugin JAR's resources
2. **Version Your Config**: Start with `config_version: 1` in your default configuration
3. **Plan Migrations**: When changing config structure, increment the version and plan migration logic
4. **Validate Settings**: Use the configuration API to validate required settings
5. **Document Changes**: Clearly document configuration changes in your plugin's README

#### For Bot Administrators

1. **Backup Regularly**: While automatic backups are created, maintain regular manual backups
2. **Test Migrations**: Test configuration migrations in a development environment first
3. **Monitor Logs**: Watch for migration warnings and errors in bot logs
4. **Environment Variables**: Use environment variables for sensitive settings like tokens
5. **Version Control**: Consider using version control for your configuration files
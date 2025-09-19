# Plugin Template

This template provides a **comprehensive starting point** for creating DiscordBot Core plugins with examples of **ALL available features**.

🎯 **What's New**: This template now includes working examples of every core feature:
- **Command System**: Slash commands, options, subcommands, permissions, cooldowns
- **Event System**: Discord events with different priorities and filtering
- **Storage System**: User/Guild/Global data + Binary file storage
- **Audio System**: Send/receive handlers with volume control (demonstration)
- **Internationalization**: Multi-language support with proper namespaces
- **Configuration**: Comprehensive config with validation and defaults
- **Background Tasks**: Scheduled executors for maintenance operations

## 🚀 Quick Start

1. **Copy this template**
   ```bash
   cp -r plugin-template my-awesome-plugin
   cd my-awesome-plugin
   ```

2. **Customize the plugin**
    - Edit `pom.xml`:
        - Change `<artifactId>` to your plugin name
        - Update `<name>` and `<description>`
        - Add `discordbot-core` dependency for full API access
    - Rename the package in `src/main/java/com/example/plugin/` to your own
    - Rename `TemplatePlugin.java` to your plugin class name
    - Update the class name and plugin details in the Java file

3. **Update configuration files**
    - Edit `src/main/resources/plugin.yml` with your plugin metadata
    - Customize `src/main/resources/config.yml` with your settings
    - Update language files in `src/main/resources/lang/` with your text

4. **Build and deploy**
   ```bash
   mvn clean package
   cp target/my-awesome-plugin-*.jar ../plugins/
   ```

## 📁 Plugin Structure

```
my-awesome-plugin/
├── pom.xml                           # Maven configuration
├── README.md                         # Plugin documentation
└── src/main/
    ├── java/
    │   └── com/example/plugin/
    │       └── TemplatePlugin.java   # Main plugin class
    └── resources/
        ├── plugin.yml                # Plugin metadata
        ├── config.yml                # Default configuration
        └── lang/                     # Language files
            ├── en-US.yml             # English translations
            └── fr-FR.yml             # French translations
```

## 🎛️ Template Features

### Core Plugin Features ✅
- **Plugin Lifecycle**: Proper onEnable/onDisable with resource management
- **Configuration System**: Comprehensive YAML config with validation
- **Permission Management**: Plugin-specific permissions with defaults
- **Error Handling**: Robust error handling and logging
- **Resource Cleanup**: Proper shutdown and resource management

### Command System Examples ⚡
- **Basic Commands**: Info, help, configuration management
- **Advanced Options**: All option types (string, int, boolean, user, channel, role, file)
- **Subcommands**: Hierarchical command structure
- **Permissions**: Command-level permission control
- **Autocomplete**: Dynamic option completion
- **Cooldowns**: Rate limiting demonstration (commented examples)

### Event Handling Examples 🎯
- **Message Events**: Message processing with reaction handling
- **Member Events**: Welcome messages and user tracking
- **Priority Levels**: HIGH, NORMAL, LOW priority event handling
- **Event Filtering**: Proper bot message filtering

### Data Storage Examples 💾
- **User Storage**: User profiles, preferences, and statistics
- **Guild Storage**: Server-specific settings and data
- **Global Storage**: Plugin-wide statistics and configuration
- **Binary Storage**: File upload and management system
- **Data Persistence**: Automatic saving and backup

### Internationalization 🌐
- **Multi-language**: English and French language files
- **Namespaced Keys**: Proper plugin namespace for translations
- **Placeholder Support**: Dynamic content with {user}, {server} placeholders
- **Fallback System**: Graceful fallback to default language

### Background Tasks ⏰
- **Scheduled Executors**: Periodic maintenance tasks
- **Data Cleanup**: Automatic old data removal
- **Statistics Updates**: Regular statistics calculation
- **Graceful Shutdown**: Proper task termination

### Audio System (Demo) 🎵
- **Audio Handlers**: Send/receive handler examples
- **Volume Control**: Audio volume management
- **Resource Cleanup**: Proper audio resource management
- **Guild Management**: Per-guild audio handling

## 📚 Learning from Examples

### Start Simple
Remove features you don't need:
```java
// Remove audio features if not needed
private void cleanupAudioHandlers() {
    // Remove this method and related code
}
```

### Add Complexity Gradually
1. Start with basic commands and events
2. Add data storage for user preferences
3. Implement background tasks for maintenance
4. Add advanced features like audio or complex permissions

### Study the Code Comments
Every method includes detailed comments explaining:
- When and why to use each feature
- Best practices and common pitfalls
- Integration patterns with other systems

## 🔧 Available Examples

Explore comprehensive feature examples in the `examples/` directory:

- **[Audio Example](../examples/audio/)**: Complete audio system usage
- **[Commands Example](../examples/commands/)**: Advanced command patterns
- **[Events Example](../examples/events/)**: Comprehensive event handling
- **[Storage Example](../examples/storage/)**: Data persistence and file management

## 🛠️ Development Tips

### Configuration Best Practices
```yaml
# Always provide defaults
features:
  enabled: true  # Default value
  
# Use validation in your code
max_users: 100  # Validate: 1-1000 range
```

### Permission Structure
```java
// Use consistent naming
pluginPrefix("use")      // yourplugin.use
pluginPrefix("admin")    // yourplugin.admin
pluginPrefix("advanced") // yourplugin.advanced
```

### Storage Patterns
```java
// User-specific data
getUserStorage(userId).set("preference", value);

// Server-specific data
getGuildStorage(guildId).increment("message_count", 1);

// Global plugin data
getGlobalStorage().set("total_users", count);
```

### Event Handling
```java
@EventHandler(priority = EventPriority.HIGH)   // Security, anti-spam
@EventHandler(priority = EventPriority.NORMAL) // Main functionality
@EventHandler(priority = EventPriority.LOW)    // Logging, analytics
```

## 🔍 Troubleshooting

### Common Issues

1. **Plugin not loading**: Check plugin.yml syntax and main class path
2. **Commands not appearing**: Verify permissions and command registration
3. **Config not working**: Ensure YAML syntax and proper defaults
4. **Language not found**: Check file paths and key namespacing
5. **Storage errors**: Verify directory permissions and error handling

### Debug Tips

```java
// Enable debug logging
logger.debug("Debug info: {}", value);

// Log configuration values
logger.info("Config loaded: {}", getConfiguration().getKeys());

// Check permission registration
logger.debug("Permissions: {}", getPluginPermissionManager().getRegisteredPermissions());
```

## 📖 Resources

- [Plugin Development Guide](../docs/plugin-development.md)
- [Command System Documentation](../docs/commands.md)
- [Audio API Guide](../docs/audio-api.md)
- [Configuration Reference](../docs/configuration.md)
- [Event System Guide](../docs/events.md)
- [JDA Documentation](https://jda.wiki/)

## 🤝 Contributing

If you find issues with this template or have suggestions for improvements:
1. Create an issue describing the problem
2. Submit a pull request with fixes or enhancements
3. Help improve the documentation and examples

## 📄 License

This template is part of the DiscordBot Core project and follows the same license terms.
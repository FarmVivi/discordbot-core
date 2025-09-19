# DiscordBot Core Examples

This directory contains comprehensive example plugins demonstrating all the features available in the DiscordBot Core plugin system.

## 📁 Available Examples

### 🎵 [Audio Example](audio/)
**Comprehensive audio system demonstration**
- Audio send/receive handlers
- Volume control and mixing
- Priority management
- Voice channel auto-join/leave
- Audio recording and playback

### ⚡ [Commands Example](commands/)
**Complete command system showcase**
- Slash commands with all option types
- Subcommands and command hierarchies
- Permissions and cooldowns
- Autocomplete functionality
- Guild-only and ephemeral commands
- Text command legacy support

### 🎯 [Events Example](events/)
**Event system comprehensive guide**
- Discord event handling with priorities
- Member join/leave events
- Message processing and reaction roles
- Voice channel events
- Background activity monitoring
- Event filtering and processing

### 💾 [Storage Example](storage/)
**Storage system complete demonstration**
- User/Guild/Global data storage
- Binary file storage
- Data persistence and backup
- Statistics and analytics
- Experience/leveling system
- Activity tracking and leaderboards

## 🚀 Quick Start

1. **Explore the examples**: Each example contains:
   - Complete plugin implementation
   - Comprehensive configuration files
   - Multi-language support (EN/FR)
   - Detailed documentation

2. **Run an example**:
   ```bash
   cd examples/commands
   mvn clean package
   cp target/*.jar ../../plugins/
   ```

3. **Study the code**: Each example demonstrates specific features:
   - Check the main plugin class for implementation details
   - Review `config.yml` for configuration options
   - Look at language files for internationalization
   - Study `plugin.yml` for metadata structure

## 📋 Features Demonstrated

### Core System Features
- [x] **Plugin Lifecycle**: onEnable/onDisable with proper resource management
- [x] **Configuration**: YAML config with defaults, validation, and environment variables
- [x] **Internationalization**: Multi-language support with namespaces and placeholders
- [x] **Permissions**: Plugin-specific permissions with defaults and inheritance
- [x] **Logging**: Structured logging with different levels

### Command System
- [x] **Slash Commands**: Modern Discord slash command API
- [x] **Text Commands**: Legacy text command support
- [x] **Options**: All option types (string, integer, boolean, user, channel, role, attachment)
- [x] **Autocomplete**: Dynamic option completion
- [x] **Subcommands**: Command hierarchies and grouping
- [x] **Permissions**: Command-level permission control
- [x] **Cooldowns**: Rate limiting and cooldown management
- [x] **Guild-only**: Server-specific commands
- [x] **Ephemeral**: Private response messages

### Event System
- [x] **Discord Events**: All major Discord API events
- [x] **Event Priorities**: HIGH, NORMAL, LOW priority processing
- [x] **Custom Events**: Plugin-specific event creation
- [x] **Event Cancellation**: Cancellable event support
- [x] **Background Processing**: Non-blocking event handling

### Storage System
- [x] **Data Storage**: User, Guild, and Global data persistence
- [x] **Binary Storage**: File upload and management
- [x] **Storage Backends**: File, Database, and S3 support
- [x] **Data Types**: Automatic serialization/deserialization
- [x] **Transactions**: Atomic data operations
- [x] **Backup**: Automatic data backup and recovery

### Audio System
- [x] **Send Handlers**: Audio playback with volume control
- [x] **Receive Handlers**: Audio recording and processing
- [x] **Mixing**: Multi-source audio mixing
- [x] **Priorities**: Audio priority and fade management
- [x] **Voice Management**: Channel connection handling

## 🛠️ Development Tips

### Best Practices
1. **Resource Management**: Always clean up resources in onDisable()
2. **Error Handling**: Use try-catch blocks for external operations
3. **Async Operations**: Use .queue() for non-blocking Discord API calls
4. **Configuration**: Provide sensible defaults for all config values
5. **Internationalization**: Use language keys for all user-facing text
6. **Permissions**: Check permissions before executing sensitive operations
7. **Logging**: Use appropriate log levels (debug, info, warn, error)

### Performance Tips
1. **Event Handling**: Keep event handlers lightweight
2. **Storage**: Batch storage operations when possible
3. **Background Tasks**: Use scheduled executors for periodic operations
4. **Memory**: Clean up unused data and weak references
5. **Discord API**: Respect rate limits and use bulk operations

### Testing
1. **Unit Tests**: Test core logic without Discord dependencies
2. **Integration Tests**: Test with mock Discord events
3. **Load Testing**: Test with multiple concurrent users
4. **Error Cases**: Test error conditions and edge cases

## 📚 Learning Path

1. **Start with Commands**: Learn basic plugin structure and command system
2. **Add Events**: Understand event handling and user interaction
3. **Implement Storage**: Add data persistence and user profiles
4. **Explore Audio**: Advanced audio processing features
5. **Combine Features**: Build complex plugins combining all systems

## 🔗 Related Documentation

- [Plugin Development Guide](../docs/plugin-development.md)
- [Command System](../docs/commands.md)
- [Audio API](../docs/audio-api.md)
- [Configuration Guide](../docs/configuration.md)
- [Plugin Template](../plugin-template/)

## 🤝 Contributing

Found an issue or want to improve an example?
1. Create an issue describing the problem or enhancement
2. Fork the repository and make your changes
3. Submit a pull request with clear description
4. Ensure all examples still compile and work correctly

## 📄 License

These examples are part of the DiscordBot Core project and follow the same license terms.
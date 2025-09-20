# Fluxcord

A modular, generic Discord bot engine designed for flexibility and extensibility. Built with a multi-module Maven architecture, it provides a comprehensive plugin system allowing developers to create powerful Discord bots with minimal effort.

## 🚀 Features

### Core Engine Features

- **Multi-Module Architecture**: Clean separation between API contracts and implementations
- **Plugin System**: Dynamic plugin loading with dependency resolution and lifecycle management
- **Command Framework**: Unified slash commands and text commands with permissions, cooldowns, and categories
- **Audio API**: Advanced audio processing with send/receive streams, mixing, and priority management
- **Permission System**: Flexible role-based permissions with plugin-specific controls
- **Internationalization**: Full i18n support with language switching and namespace management
- **Configuration Management**: YAML-based configuration with environment variable overrides
- **Event System**: Comprehensive event handling with priority-based execution
- **Storage API**: Pluggable storage backends (File, Database, S3) for data persistence
- **Discord Integration**: Full Discord API coverage through JDA with abstractions

### Built-in Commands

- `help` - Interactive help system with command discovery
- `version` - Display bot and plugin version information
- `shutdown` - Graceful bot shutdown with plugin cleanup

## 🎵 Available Plugins

The Fluxcord ecosystem includes several ready-to-use plugins:

### 🎵 Music Plugin

Advanced music bot functionality with playlist management, queue controls, and audio effects.

- Play music from YouTube, Spotify, SoundCloud
- Playlist creation and management
- Queue controls (skip, shuffle, loop)
- Volume control and audio filters
- Now playing displays with rich embeds

### 🤖 AI Audio Plugin

AI-powered voice processing capabilities for enhanced user interaction.

- Voice recognition and transcription
- Text-to-speech with multiple voices
- Audio analysis and processing
- Integration with popular AI services

### 🔊 Audio Example Plugin

Demonstration plugin showing audio API capabilities.

- Basic audio playback and recording
- Voice channel event handling
- Audio stream processing examples

*More plugins are in development and will be released soon!*

## 🏗️ Architecture

```
fluxcord/
├── fluxcord-api/                          # Public API interfaces and contracts
├── fluxcord-core/                         # Core implementation and engine
├── examples/plugins/plugin-example-audio/ # Example audio plugin
├── plugin-template/                       # Template for creating new plugins
├── plugins/                               # Additional plugin modules
│   ├── music-plugin/                      # Music bot functionality
│   └── ai-audio-plugin/                   # AI voice processing
└── docs/                                  # Documentation and guides
```

## 🚀 Quick Start

### Prerequisites

- **Java 17** or newer
- **Maven 3.6+** for building
- **Discord Bot Token** (obtain from Discord Developer Portal)

### Installation

1. **Clone the repository**

```bash
git clone https://github.com/FarmVivi/fluxcord.git
cd fluxcord
```

2. **Build the project**

```bash
mvn clean package
```

3. **Configure the bot**

```bash
cp config.example.yml config.yml
# Edit config.yml with your bot token and settings
```

4. **Run the bot**

```bash
java -jar target/fluxcord.jar
```

### Docker Deployment

A `docker-compose.yml` is provided for containerized deployment:

```bash
docker compose up --build
```

This automatically builds the application and provides persistent storage for data, plugins, and configuration.

## 🔌 Plugin Development

Creating plugins for Fluxcord is straightforward thanks to the provided template and comprehensive API.

### Using the Plugin Template

1. **Copy the template**

```bash
cp -r plugin-template my-awesome-plugin
cd my-awesome-plugin
```

2. **Customize the plugin**

- Edit `pom.xml` to change artifact ID and details
- Implement your plugin logic in the main class
- Add commands, event handlers, and features

3. **Build and install**

```bash
mvn clean package
cp target/my-awesome-plugin-*.jar ../plugins/
```

### Plugin API Overview

```java
@Plugin(name = "MyPlugin", version = "1.0.0")
public class MyAwesomePlugin extends AbstractPlugin {
    
    @Override
    public void onEnable() {
        // Plugin initialization
        registerCommands();
        setupEventHandlers();
    }
    
    @Command(name = "hello", description = "Say hello!")
    public void helloCommand(CommandContext ctx) {
        ctx.reply("Hello from my plugin!");
    }
    
    @EventHandler
    public void onMessageReceived(MessageReceivedEvent event) {
        // Handle Discord events
    }
}
```

### Available APIs

- **Command API**: Create slash commands and text commands
- **Event API**: Handle Discord and plugin events
- **Audio API**: Process audio streams and voice channels
- **Permission API**: Manage user permissions and roles
- **Storage API**: Persist data across restarts
- **Configuration API**: Manage plugin settings
- **Language API**: Support multiple languages

## 📖 Documentation

Comprehensive documentation is available in the `docs/` directory:

- [Plugin Development Guide](docs/plugin-development.md)
- [Command System](docs/commands.md)
- [Audio API](docs/audio-api.md)
- [Configuration Guide](docs/configuration.md)
- [Event System](docs/events.md)
- [Core Features Matrix](docs/core-features.md): Complete overview of all 12 framework features
- [Plugin Template Guide](docs/plugins/template-quickstart.md): Step-by-step plugin development guide
- [Example Plugins Overview](docs/plugins/examples-overview.md): Learning resources for all features

## 🔧 Development

### Building from Source

```bash
# Compile all modules
mvn clean compile

# Run tests
mvn test

# Package with dependencies
mvn clean package

# Install to local repository
mvn clean install
```

### Module Structure

- **fluxcord-api**: Lightweight API interfaces for plugin development
- **fluxcord-core**: Main engine implementation with all features
- **plugin-example-audio**: Reference implementation for audio plugins
- **plugin-example-commands**: Reference implementation for command handling
- **plugin-template**: Starter template for new plugin development

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details on:

- Code style and standards
- Pull request process
- Issue reporting
- Plugin submission guidelines

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: Check the `docs/` directory
- **Issues**: Report bugs or request features on GitHub Issues
- **Discussions**: Join community discussions on GitHub Discussions
- **Discord**: Join our development Discord server (link in issues)

## 🌟 Showcase

Built something amazing with Fluxcord? We'd love to showcase it! Open an issue or discussion to share your creation.

---

**Fluxcord** - *Empowering Discord bot development with modular architecture and comprehensive APIs.*

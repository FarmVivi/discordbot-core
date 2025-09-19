# Repository Inventory - FarmVivi/discordbot-core

This document provides a comprehensive inventory of all files tracked by Git in the DiscordBot Core repository.

**Total Files**: 225  
**Generated**: $(date)  
**Repository**: https://github.com/FarmVivi/discordbot-core

## Repository Structure

```
discordbot-core/
├── .dockerignore
├── .gitattributes
├── .github/                          # GitHub Actions workflows and config
│   ├── dependabot.yml
│   └── workflows/
│       ├── bump-major-version.yml
│       ├── bump-minor-version.yml
│       ├── bump-patch-version.yml
│       ├── docker-image-ci.yml
│       ├── release-build.yml
│       └── sonarcloud-analysis.yml
├── .gitignore
├── .idea/                            # IntelliJ IDEA configuration
│   ├── copilot.data.migration.agent.xml
│   ├── copilot.data.migration.edit.xml
│   ├── discord.xml
│   ├── encodings.xml
│   ├── misc.xml
│   ├── sonarlint.xml
│   ├── sqldialects.xml
│   └── vcs.xml
├── .vscode/                          # Visual Studio Code configuration
│   ├── settings.json
│   └── tasks.json
├── Dockerfile                        # Docker containerization
├── LICENSE                           # MIT License
├── README.md                         # Main project documentation
├── build.sh                          # Build script
├── discordbot-api/                   # Public API interfaces and contracts
│   ├── pom.xml
│   └── src/main/java/fr/farmvivi/discordbot/api/
│       ├── audio/                    # Audio system APIs
│       │   ├── AudioService.java
│       │   └── events/               # Audio-related events
│       │       ├── AudioEvent.java
│       │       ├── AudioFrameMixedEvent.java
│       │       ├── AudioHandlerEvent.java
│       │       ├── AudioReceiveHandlerRegisteredEvent.java
│       │       ├── AudioReceiveHandlerRemovedEvent.java
│       │       ├── AudioSendHandlerRegisteredEvent.java
│       │       ├── AudioSendHandlerRemovedEvent.java
│       │       └── AudioVolumeChangedEvent.java
│       ├── command/                  # Command system APIs
│       │   ├── Command.java
│       │   ├── CommandBuilder.java
│       │   ├── CommandContext.java
│       │   ├── CommandRegistry.java
│       │   ├── CommandResult.java
│       │   ├── CommandService.java
│       │   ├── PluginCommandAdapter.java
│       │   ├── event/               # Command events
│       │   │   ├── CommandEvent.java
│       │   │   ├── CommandExecuteEvent.java
│       │   │   └── CommandExecutedEvent.java
│       │   ├── exception/           # Command exceptions
│       │   │   ├── CommandException.java
│       │   │   ├── CommandExecutionException.java
│       │   │   ├── CommandParseException.java
│       │   │   └── CommandPermissionException.java
│       │   └── option/              # Command options
│       │       ├── CommandOption.java
│       │       ├── OptionChoice.java
│       │       └── OptionType2.java
│       ├── config/                  # Configuration APIs
│       │   ├── Configuration.java
│       │   └── ConfigurationException.java
│       ├── discord/                 # Discord integration APIs
│       │   └── DiscordAPI.java
│       ├── event/                   # Event system APIs
│       │   ├── Cancellable.java
│       │   ├── Event.java
│       │   ├── EventHandler.java
│       │   ├── EventManager.java
│       │   ├── EventPriority.java
│       │   ├── EventRegistry.java
│       │   └── EventTypeInfo.java
│       ├── language/                # Internationalization APIs
│       │   ├── LanguageManager.java
│       │   ├── PluginLanguageAdapter.java
│       │   └── events/              # Language events
│       │       ├── LanguageEvent.java
│       │       ├── LanguageLoadedEvent.java
│       │       ├── NamespaceRegisteredEvent.java
│       │       └── StringRetrievalEvent.java
│       ├── permissions/             # Permission system APIs
│       │   ├── Permission.java
│       │   ├── PermissionDefault.java
│       │   ├── PermissionDeniedException.java
│       │   ├── PermissionManager.java
│       │   ├── PluginPermissionAdapter.java
│       │   └── events/              # Permission events
│       │       ├── PermissionChangeEvent.java
│       │       └── PermissionCheckEvent.java
│       ├── plugin/                  # Plugin system APIs
│       │   ├── AbstractPlugin.java
│       │   ├── ConfigurableMigrationPlugin.java
│       │   ├── Plugin.java
│       │   ├── PluginContext.java
│       │   ├── PluginDataStorage.java
│       │   ├── PluginInfo.java
│       │   ├── PluginLifecycle.java
│       │   ├── PluginManager.java
│       │   ├── PluginState.java
│       │   └── events/              # Plugin events
│       │       ├── PluginDisableEvent.java
│       │       ├── PluginEnableEvent.java
│       │       ├── PluginEvent.java
│       │       ├── PluginLoadEvent.java
│       │       ├── PluginPostDisableEvent.java
│       │       ├── PluginPostEnableEvent.java
│       │       ├── PluginPreDisableEvent.java
│       │       ├── PluginPreEnableEvent.java
│       │       └── PluginUnloadEvent.java
│       └── storage/                 # Storage system APIs
│           ├── BinaryStorage.java
│           ├── BinaryStorageKey.java
│           ├── BinaryStorageManager.java
│           ├── DataStorage.java
│           ├── DataStorageManager.java
│           ├── DataStorageKey.java
│           ├── StorageException.java
│           └── events/              # Storage events
│               ├── DataStorageEvent.java
│               ├── StorageLoadEvent.java
│               └── StorageSaveEvent.java
├── discordbot-core/                 # Core implementation and engine
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/fr/farmvivi/discordbot/core/
│       │   │   ├── Discobocor.java  # Main application class
│       │   │   ├── audio/           # Audio system implementation
│       │   │   │   ├── AudioMixer.java
│       │   │   │   ├── AudioReceiveHandlerManager.java
│       │   │   │   ├── AudioSendHandlerManager.java
│       │   │   │   ├── AudioServiceImpl.java
│       │   │   │   ├── GuildAudioManager.java
│       │   │   │   ├── PriorityManager.java
│       │   │   │   └── VoiceChannelManager.java
│       │   │   ├── command/         # Command system implementation
│       │   │   │   ├── CommandManager.java
│       │   │   │   ├── CommandParameterResolver.java
│       │   │   │   ├── CommandServiceImpl.java
│       │   │   │   ├── ConsoleCommandService.java
│       │   │   │   ├── discord/      # Discord-specific commands
│       │   │   │   │   ├── DiscordCommandContext.java
│       │   │   │   │   ├── DiscordCommandManager.java
│       │   │   │   │   └── DiscordSlashCommandHandler.java
│       │   │   │   └── system/      # System commands
│       │   │   │       ├── HelpCommand.java
│       │   │   │       ├── ShutdownCommand.java
│       │   │   │       └── VersionCommand.java
│       │   │   ├── config/          # Configuration implementation
│       │   │   │   ├── CoreConfiguration.java
│       │   │   │   ├── FileConfiguration.java
│       │   │   │   └── YamlConfiguration.java
│       │   │   ├── discord/         # Discord integration implementation
│       │   │   │   ├── DiscordAPIImpl.java
│       │   │   │   └── listeners/   # Discord event listeners
│       │   │   │       ├── DiscordEventListener.java
│       │   │   │       └── MessageEventHandler.java
│       │   │   ├── event/           # Event system implementation
│       │   │   │   └── SimpleEventManager.java
│       │   │   ├── language/        # Internationalization implementation
│       │   │   │   ├── LanguageFileLoader.java
│       │   │   │   └── SimpleLanguageManager.java
│       │   │   ├── permissions/     # Permission system implementation
│       │   │   │   ├── PermissionBuilder.java
│       │   │   │   ├── PermissionChecker.java
│       │   │   │   ├── PermissionImpl.java
│       │   │   │   └── SimplePermissionManager.java
│       │   │   ├── plugin/          # Plugin system implementation
│       │   │   │   ├── DependencyResolver.java
│       │   │   │   ├── PluginClassLoader.java
│       │   │   │   ├── PluginConfiguration.java
│       │   │   │   ├── PluginContextImpl.java
│       │   │   │   ├── PluginDescriptor.java
│       │   │   │   └── PluginManager.java
│       │   │   ├── storage/         # Storage system implementation
│       │   │   │   ├── AbstractDataStorage.java
│       │   │   │   ├── StorageFactory.java
│       │   │   │   ├── binary/      # Binary storage implementations
│       │   │   │   │   ├── AbstractBinaryStorage.java
│       │   │   │   │   ├── BinaryStorageFactory.java
│       │   │   │   │   ├── file/    # File-based binary storage
│       │   │   │   │   │   └── FileBinaryStorage.java
│       │   │   │   │   └── s3/      # S3-based binary storage
│       │   │   │   │       └── S3BinaryStorage.java
│       │   │   │   ├── db/          # Database storage
│       │   │   │   │   └── DatabaseDataStorage.java
│       │   │   │   └── file/        # File-based storage
│       │   │   │       └── FileDataStorage.java
│       │   │   └── util/            # Utility classes
│       │   │       ├── Debouncer.java
│       │   │       ├── DiscordColor.java
│       │   │       └── EnvironmentUtils.java
│       │   └── resources/           # Core resources
│       │       ├── config.yml       # Default core configuration
│       │       ├── lang/            # Core language files
│       │       │   ├── en-US.yml    # English translations
│       │       │   └── fr-FR.yml    # French translations
│       │       └── project.properties # Project metadata
│       └── test/                    # Unit tests
│           └── java/fr/farmvivi/discordbot/core/
│               ├── audio/           # Audio system tests
│               │   ├── AudioMixerTest.java
│               │   ├── AudioServiceImplTest.java
│               │   └── PriorityManagerTest.java
│               ├── config/          # Configuration tests
│               │   └── CoreConfigurationTest.java
│               └── plugin/          # Plugin system tests
│                   └── PluginConfigurationTest.java
├── docker-compose.yml               # Docker Compose configuration
├── docs/                            # Documentation
│   ├── audio-api.md                 # Audio API documentation
│   ├── commands.md                  # Command system guide
│   ├── configuration.md             # Configuration guide
│   └── plugin-development.md        # Plugin development guide
├── examples/                        # Example implementations
│   └── audio/                       # Audio example plugin
│       ├── README.md
│       ├── pom.xml
│       ├── src/main/
│       │   ├── java/fr/farmvivi/discordbot/examples/audio/
│       │   │   └── AudioExamplePlugin.java
│       │   └── resources/
│       │       ├── config.yml
│       │       ├── lang/
│       │       │   ├── en-US.yml
│       │       │   └── fr-FR.yml
│       │       └── plugin.yml
├── gource-nvenc.bat                 # Git visualization script (Windows)
├── gource-nvenc.sh                  # Git visualization script (Unix)
├── plugin-template/                 # Plugin template
│   ├── README.md
│   ├── pom.xml
│   ├── src/main/
│   │   ├── java/com/example/plugin/
│   │   │   └── TemplatePlugin.java
│   │   └── resources/
│   │       ├── config.yml
│   │       ├── lang/
│   │       │   ├── en-US.yml
│   │       │   └── fr-FR.yml
│   │       └── plugin.yml
├── plugins/                         # Additional plugin modules
│   ├── ai-audio-plugin/             # AI audio processing plugin
│   │   ├── README.md
│   │   ├── pom.xml
│   │   ├── src/main/
│   │   │   ├── java/fr/farmvivi/discordbot/plugins/aiaudio/
│   │   │   │   ├── AIAudioPlugin.java
│   │   │   │   ├── AudioAnalysisService.java
│   │   │   │   ├── SpeechRecognitionService.java
│   │   │   │   └── TextToSpeechService.java
│   │   │   └── resources/
│   │   │       ├── config.yml
│   │   │       ├── lang/
│   │   │       │   ├── en-US.yml
│   │   │       │   └── fr-FR.yml
│   │   │       └── plugin.yml
│   └── music-plugin/                # Music bot functionality
│       ├── README.md
│       ├── pom.xml
│       ├── src/main/
│       │   ├── java/fr/farmvivi/discordbot/plugins/music/
│       │   │   ├── MusicManager.java
│       │   │   ├── MusicPlugin.java
│       │   │   └── PlaylistManager.java
│       │   └── resources/
│       │       ├── config.yml
│       │       ├── lang/
│       │       │   ├── en-US.yml
│       │       │   └── fr-FR.yml
│       │       └── plugin.yml
├── pom.xml                          # Root Maven POM
└── sonar-project.properties         # SonarCloud configuration
```

## Complete File Listing

### Root Level Files (12)
- .dockerignore
- .gitattributes
- .gitignore
- Dockerfile
- LICENSE
- README.md
- build.sh
- docker-compose.yml
- gource-nvenc.bat
- gource-nvenc.sh
- pom.xml
- sonar-project.properties

### GitHub Configuration (7)
- .github/dependabot.yml
- .github/workflows/bump-major-version.yml
- .github/workflows/bump-minor-version.yml
- .github/workflows/bump-patch-version.yml
- .github/workflows/docker-image-ci.yml
- .github/workflows/release-build.yml
- .github/workflows/sonarcloud-analysis.yml

### IDE Configuration (10)
- .idea/copilot.data.migration.agent.xml
- .idea/copilot.data.migration.edit.xml
- .idea/discord.xml
- .idea/encodings.xml
- .idea/misc.xml
- .idea/sonarlint.xml
- .idea/sqldialects.xml
- .idea/vcs.xml
- .vscode/settings.json
- .vscode/tasks.json

### Documentation (4)
- docs/audio-api.md
- docs/commands.md
- docs/configuration.md
- docs/plugin-development.md

### DiscordBot API Module (71)
**Configuration**: 1 file
- discordbot-api/pom.xml

**Audio APIs**: 9 files
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/audio/AudioService.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/audio/events/AudioEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/audio/events/AudioFrameMixedEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/audio/events/AudioHandlerEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/audio/events/AudioReceiveHandlerRegisteredEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/audio/events/AudioReceiveHandlerRemovedEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/audio/events/AudioSendHandlerRegisteredEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/audio/events/AudioSendHandlerRemovedEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/audio/events/AudioVolumeChangedEvent.java

**Command APIs**: 16 files
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/Command.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/CommandBuilder.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/CommandContext.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/CommandRegistry.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/CommandResult.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/CommandService.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/PluginCommandAdapter.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/event/CommandEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/event/CommandExecuteEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/event/CommandExecutedEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/exception/CommandException.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/exception/CommandExecutionException.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/exception/CommandParseException.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/exception/CommandPermissionException.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/option/CommandOption.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/option/OptionChoice.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/command/option/OptionType2.java

**Configuration APIs**: 2 files
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/config/Configuration.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/config/ConfigurationException.java

**Discord APIs**: 1 file
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/discord/DiscordAPI.java

**Event APIs**: 7 files
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/event/Cancellable.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/event/Event.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/event/EventHandler.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/event/EventManager.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/event/EventPriority.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/event/EventRegistry.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/event/EventTypeInfo.java

**Language APIs**: 6 files
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/language/LanguageManager.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/language/PluginLanguageAdapter.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/language/events/LanguageEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/language/events/LanguageLoadedEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/language/events/NamespaceRegisteredEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/language/events/StringRetrievalEvent.java

**Permission APIs**: 7 files
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/permissions/Permission.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/permissions/PermissionDefault.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/permissions/PermissionDeniedException.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/permissions/PermissionManager.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/permissions/PluginPermissionAdapter.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/permissions/events/PermissionChangeEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/permissions/events/PermissionCheckEvent.java

**Plugin APIs**: 18 files
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/AbstractPlugin.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/ConfigurableMigrationPlugin.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/Plugin.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/PluginContext.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/PluginDataStorage.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/PluginInfo.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/PluginLifecycle.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/PluginManager.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/PluginState.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/events/PluginDisableEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/events/PluginEnableEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/events/PluginEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/events/PluginLoadEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/events/PluginPostDisableEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/events/PluginPostEnableEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/events/PluginPreDisableEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/events/PluginPreEnableEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/plugin/events/PluginUnloadEvent.java

**Storage APIs**: 11 files
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/storage/BinaryStorage.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/storage/BinaryStorageKey.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/storage/BinaryStorageManager.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/storage/DataStorage.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/storage/DataStorageManager.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/storage/DataStorageKey.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/storage/StorageException.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/storage/events/DataStorageEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/storage/events/StorageLoadEvent.java
- discordbot-api/src/main/java/fr/farmvivi/discordbot/api/storage/events/StorageSaveEvent.java

### DiscordBot Core Module (38)
**Configuration**: 1 file
- discordbot-core/pom.xml

**Core Implementation**: 29 files
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/Discobocor.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/audio/AudioMixer.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/audio/AudioReceiveHandlerManager.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/audio/AudioSendHandlerManager.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/audio/AudioServiceImpl.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/audio/GuildAudioManager.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/audio/PriorityManager.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/audio/VoiceChannelManager.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/command/CommandManager.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/command/CommandParameterResolver.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/command/CommandServiceImpl.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/command/ConsoleCommandService.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/command/discord/DiscordCommandContext.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/command/discord/DiscordCommandManager.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/command/discord/DiscordSlashCommandHandler.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/command/system/HelpCommand.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/command/system/ShutdownCommand.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/command/system/VersionCommand.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/config/CoreConfiguration.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/config/FileConfiguration.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/config/YamlConfiguration.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/discord/DiscordAPIImpl.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/discord/listeners/DiscordEventListener.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/discord/listeners/MessageEventHandler.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/event/SimpleEventManager.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/language/LanguageFileLoader.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/language/SimpleLanguageManager.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/permissions/PermissionBuilder.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/permissions/PermissionChecker.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/permissions/PermissionImpl.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/permissions/SimplePermissionManager.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/plugin/DependencyResolver.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/plugin/PluginClassLoader.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/plugin/PluginConfiguration.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/plugin/PluginContextImpl.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/plugin/PluginDescriptor.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/plugin/PluginManager.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/storage/AbstractDataStorage.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/storage/StorageFactory.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/storage/binary/AbstractBinaryStorage.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/storage/binary/BinaryStorageFactory.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/storage/binary/file/FileBinaryStorage.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/storage/binary/s3/S3BinaryStorage.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/storage/db/DatabaseDataStorage.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/storage/file/FileDataStorage.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/util/Debouncer.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/util/DiscordColor.java
- discordbot-core/src/main/java/fr/farmvivi/discordbot/core/util/EnvironmentUtils.java

**Core Resources**: 4 files
- discordbot-core/src/main/resources/config.yml
- discordbot-core/src/main/resources/lang/en-US.yml
- discordbot-core/src/main/resources/lang/fr-FR.yml
- discordbot-core/src/main/resources/project.properties

**Core Tests**: 5 files
- discordbot-core/src/test/java/fr/farmvivi/discordbot/core/audio/AudioMixerTest.java
- discordbot-core/src/test/java/fr/farmvivi/discordbot/core/audio/AudioServiceImplTest.java
- discordbot-core/src/test/java/fr/farmvivi/discordbot/core/audio/PriorityManagerTest.java
- discordbot-core/src/test/java/fr/farmvivi/discordbot/core/config/CoreConfigurationTest.java
- discordbot-core/src/test/java/fr/farmvivi/discordbot/core/plugin/PluginConfigurationTest.java

### Examples (8)
**Audio Example Plugin**:
- examples/audio/README.md
- examples/audio/pom.xml
- examples/audio/src/main/java/fr/farmvivi/discordbot/examples/audio/AudioExamplePlugin.java
- examples/audio/src/main/resources/config.yml
- examples/audio/src/main/resources/lang/en-US.yml
- examples/audio/src/main/resources/lang/fr-FR.yml
- examples/audio/src/main/resources/plugin.yml

### Plugin Template (7)
- plugin-template/README.md
- plugin-template/pom.xml
- plugin-template/src/main/java/com/example/plugin/TemplatePlugin.java
- plugin-template/src/main/resources/config.yml
- plugin-template/src/main/resources/lang/en-US.yml
- plugin-template/src/main/resources/lang/fr-FR.yml
- plugin-template/src/main/resources/plugin.yml

### Additional Plugins (22)

**AI Audio Plugin** (11 files):
- plugins/ai-audio-plugin/README.md
- plugins/ai-audio-plugin/pom.xml
- plugins/ai-audio-plugin/src/main/java/fr/farmvivi/discordbot/plugins/aiaudio/AIAudioPlugin.java
- plugins/ai-audio-plugin/src/main/java/fr/farmvivi/discordbot/plugins/aiaudio/AudioAnalysisService.java
- plugins/ai-audio-plugin/src/main/java/fr/farmvivi/discordbot/plugins/aiaudio/SpeechRecognitionService.java
- plugins/ai-audio-plugin/src/main/java/fr/farmvivi/discordbot/plugins/aiaudio/TextToSpeechService.java
- plugins/ai-audio-plugin/src/main/resources/config.yml
- plugins/ai-audio-plugin/src/main/resources/lang/en-US.yml
- plugins/ai-audio-plugin/src/main/resources/lang/fr-FR.yml
- plugins/ai-audio-plugin/src/main/resources/plugin.yml

**Music Plugin** (11 files):
- plugins/music-plugin/README.md
- plugins/music-plugin/pom.xml
- plugins/music-plugin/src/main/java/fr/farmvivi/discordbot/plugins/music/MusicManager.java
- plugins/music-plugin/src/main/java/fr/farmvivi/discordbot/plugins/music/MusicPlugin.java
- plugins/music-plugin/src/main/java/fr/farmvivi/discordbot/plugins/music/PlaylistManager.java
- plugins/music-plugin/src/main/resources/config.yml
- plugins/music-plugin/src/main/resources/lang/en-US.yml
- plugins/music-plugin/src/main/resources/lang/fr-FR.yml
- plugins/music-plugin/src/main/resources/plugin.yml

## File Type Distribution

- **Java Source Files**: 113 (50.2%)
- **Configuration Files (YAML/XML/Properties)**: 46 (20.4%)
- **Documentation (MD)**: 17 (7.6%)
- **GitHub/CI Configuration**: 7 (3.1%)
- **IDE Configuration**: 10 (4.4%)
- **Build/Deployment Scripts**: 6 (2.7%)
- **License/Legal**: 1 (0.4%)
- **Other**: 25 (11.1%)

## Module Summary

| Module | Files | Java Classes | Configuration | Documentation |
|--------|-------|--------------|---------------|---------------|
| discordbot-api | 71 | 70 | 1 | 0 |
| discordbot-core | 38 | 29 | 4 | 0 |
| examples/audio | 8 | 1 | 4 | 1 |
| plugin-template | 7 | 1 | 4 | 1 |
| plugins/ai-audio-plugin | 11 | 4 | 4 | 1 |
| plugins/music-plugin | 11 | 3 | 4 | 1 |
| docs | 4 | 0 | 0 | 4 |
| root & config | 75 | 0 | 25 | 1 |

**Total**: 225 files across 8 modules with 108 Java classes, 46 configuration files, and 9 documentation files.

---

*This inventory was generated automatically from Git repository data and provides a complete, non-truncated listing of all tracked files.*
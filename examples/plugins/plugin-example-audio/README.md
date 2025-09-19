# Audio Example Plugin

This plugin demonstrates the audio capabilities of DiscordBot Core, showing how to:

- Play audio files in voice channels
- Record voice channel audio
- Handle voice channel events
- Manage audio streams and mixing
- Implement audio send and receive handlers
- Use configuration values
- Implement internationalization

## Features

- **Audio Playback**: Play audio files when users join voice channels (configurable)
- **Audio Recording**: Record voice channel conversations with configurable format
- **Stream Processing**: Process audio data in real-time
- **Event Handling**: Respond to voice channel events
- **Configuration**: Extensive configuration options for audio behavior
- **Internationalization**: Support for multiple languages (English, French)

## Configuration

The plugin uses configuration values from `config.yml`:

```yaml
# Audio settings
audio:
  auto_join: false              # Auto join voice channel when user joins
  default_volume: 50            # Default volume for audio playback (0-100)
  max_recording_duration: 300   # Maximum recording duration in seconds
  recording_format: wav         # Audio file format for recordings
  enable_enhancement: true      # Enable automatic audio enhancement

# Voice channel settings
voice:
  auto_leave: true              # Auto leave when no users in channel
  auto_leave_timeout: 30        # Auto leave timeout in seconds

# File paths
paths:
  recordings_dir: "recordings/" # Directory for storing audio recordings
  samples_dir: "samples/"       # Directory for audio samples
```

## Language Support

The plugin supports multiple languages with localized messages:

- **English** (`en-US.yml`): Default language
- **French** (`fr-FR.yml`): French translations

Language files are located in `src/main/resources/lang/` and contain:

- Plugin lifecycle messages
- Audio operation notifications
- Error messages
- Command descriptions

## Usage

1. **Build the plugin**: `mvn clean package`
2. **Copy to plugins directory**: `cp target/discordbot-example-audio-*.jar ../../../plugins/`
3. **Configure the plugin**: Edit your bot's configuration
4. **Restart the bot**
5. **Join a voice channel** to trigger audio functionality (if auto_join is enabled)
6. **Check the recordings directory** for captured audio

## Code Structure

- `AudioExamplePlugin.java`: Main plugin class with:
    - Configuration loading and usage
    - Language support integration
    - Lifecycle management
    - Voice event handlers
- `MySendHandler`: Demonstrates audio sending to voice channels
- `MyReceiveHandler`: Shows how to receive and process voice channel audio
- Language files: Localized messages and text
- Configuration files: Plugin metadata and default settings

## API Demonstrations

This plugin showcases:

- **Configuration API**: Loading and using configuration values
- **Language API**: Internationalization and localized messages
- **Audio API**: Send and receive audio handlers
- **Event API**: Voice channel event handling
- **Storage API**: File management for recordings and samples

This plugin serves as a comprehensive example for developing audio-enabled plugins with proper configuration management and internationalization support.
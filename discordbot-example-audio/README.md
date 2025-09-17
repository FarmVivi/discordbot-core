# Audio Example Plugin

This plugin demonstrates the audio capabilities of DiscordBot Core, showing how to:

- Play audio files in voice channels
- Record voice channel audio
- Handle voice channel events
- Manage audio streams and mixing
- Implement audio send and receive handlers

## Features

- **Audio Playback**: Play audio files when users join voice channels
- **Audio Recording**: Record voice channel conversations
- **Stream Processing**: Process audio data in real-time
- **Event Handling**: Respond to voice channel events
- **Configuration**: Customizable audio settings

## Commands

This plugin serves as an example and doesn't provide user commands. The functionality is triggered automatically by voice channel events.

## Configuration

Edit `config.yml` to customize:

- Audio volume and quality settings
- Recording parameters and file formats
- Voice channel behavior
- File storage locations

## Usage

1. Build the plugin: `mvn clean package`
2. Copy to plugins directory: `cp target/discordbot-example-audio-*.jar ../plugins/`
3. Restart the bot
4. Join a voice channel to trigger audio playback
5. Check the recordings directory for captured audio

## Code Structure

- `AudioExamplePlugin.java`: Main plugin class with lifecycle management
- `MySendHandler`: Demonstrates audio sending to voice channels
- `MyReceiveHandler`: Shows how to receive and process voice channel audio
- Voice event handlers for automatic audio management

This plugin is primarily educational and should be used as a reference for developing your own audio-enabled plugins.
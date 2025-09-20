# Music Plugin

Advanced music bot functionality for Fluxcord with comprehensive playlist management, modern audio processing, and enhanced user experience.

## Features

### 🎵 Music Playback

- **Multi-platform Support**: Play music from YouTube, Spotify, SoundCloud, and HTTP sources
- **High-quality Audio**: LavaPlayer integration with configurable quality settings
- **Smart Search**: Intelligent search across multiple platforms with fallback sources
- **URL Support**: Direct links, playlists, and album imports
- **Queue Management**: Advanced queue controls with shuffle, loop, and manipulation

### 📜 Queue & Playback Controls

- **Dynamic Queue**: Add, remove, skip, and reorder tracks
- **Loop Modes**: Track loop, queue loop, and normal playback
- **Shuffle Mode**: Randomize queue playback order
- **Skip Controls**: Individual skip, vote skip, and admin force skip
- **Playback Controls**: Pause, resume, stop with voice channel management

### 📋 Playlist System

- **Personal Playlists**: Create and manage custom playlists (up to 500 tracks)
- **Favorites System**: Quick access to favorite tracks (up to 100 per user)
- **Playlist Persistence**: Save and restore playlists across restarts
- **Privacy Controls**: Public and private playlist options
- **Import/Export**: Load playlists from URLs and export for backup

### 🔊 Audio Control

- **Volume Control**: Per-server volume settings (0-100%)
- **Voice Management**: Auto-join, auto-leave, and voice state tracking
- **Audio Quality**: Configurable quality settings (low/medium/high)
- **Connection Handling**: Robust reconnection and error recovery

### 📊 Rich Interface

- **Now Playing**: Beautiful embeds with track information and progress bars
- **Queue Display**: Comprehensive queue view with time estimates
- **Status Indicators**: Visual feedback for all playback states
- **Interactive Controls**: Rich embeds with detailed track metadata

### 🛡️ Permissions & Moderation

- **Role-based Permissions**: Granular control over music commands
- **DJ Role Support**: Enhanced permissions for designated users
- **Vote Skip System**: Democratic skip control for shared listening
- **Admin Controls**: Override and management commands

## Commands

### Basic Playback

| Command                    | Description                         | Permission         |
|----------------------------|-------------------------------------|--------------------|
| `/play <query>`           | Play music from search or URL      | `musicplugin.play` |
| `/pause`                  | Pause/resume playback               | `musicplugin.play` |
| `/skip`                   | Skip current track                  | `musicplugin.skip` |
| `/stop`                   | Stop playback and clear queue      | `musicplugin.admin`|
| `/nowplaying` (aliases: `np`, `current`) | Show current track info | `musicplugin.queue` |

### Queue Management

| Command                    | Description                         | Permission         |
|----------------------------|-------------------------------------|--------------------|
| `/queue`                  | Show current queue                  | `musicplugin.queue`|
| `/shuffle`                | Shuffle the queue                   | `musicplugin.queue`|
| `/loop [mode]`            | Set loop mode (off/track/queue)     | `musicplugin.queue`|
| `/clear`                  | Clear the entire queue              | `musicplugin.admin`|

### Audio Control

| Command                    | Description                         | Permission         |
|----------------------------|-------------------------------------|--------------------|
| `/volume [0-100]`         | Set or view playback volume         | `musicplugin.volume`|

### Playlist Management (Coming Soon)

| Command                    | Description                         | Permission         |
|----------------------------|-------------------------------------|--------------------|
| `/playlist create <name>` | Create a new playlist               | `musicplugin.playlist`|
| `/playlist add <name>`    | Add current track to playlist       | `musicplugin.playlist`|
| `/playlist play <name>`   | Load and play a playlist            | `musicplugin.playlist`|
| `/playlist list`          | Show your playlists                 | `musicplugin.playlist`|

## Installation

### Prerequisites

- Java 17 or newer
- Fluxcord framework (develop branch)
- Discord bot with appropriate permissions

### Setup

1. **Build the plugin**
   ```bash
   cd plugins/music-plugin
   mvn clean package
   ```

2. **Install the plugin**
   ```bash
   # Copy the built JAR to plugins directory
   cp target/music-plugin-*.jar ../../plugins/
   ```

3. **Configure the plugin**
   - Edit `plugins/music-plugin/config.yml` to customize settings
   - Set up API keys for external services (optional)
   - Configure permissions and limits

4. **Restart the bot** to load the music plugin

### Discord Permissions Required

The bot needs the following Discord permissions in voice channels:
- **Connect**: Join voice channels
- **Speak**: Send audio to voice channels
- **Use Voice Activity**: Transmit audio efficiently

## Configuration

The plugin uses a comprehensive YAML configuration system:

```yaml
# Core music settings
music:
  default_volume: 50          # Default playback volume (0-100)
  max_queue_size: 100         # Maximum tracks per queue
  max_track_duration: 600000  # Max track length (10 minutes)
  audio_quality: "medium"     # Audio quality setting

# Platform support
platforms:
  enable_youtube: true        # YouTube integration
  enable_spotify: true        # Spotify integration (requires API)
  enable_soundcloud: true     # SoundCloud integration

# Voice channel behavior
voice:
  auto_leave: true           # Leave when channel is empty
  auto_leave_timeout: 300    # Timeout before leaving (seconds)
  require_same_channel: true # Require users to be in bot's channel

# Playlist settings
playlists:
  max_playlist_size: 500     # Maximum tracks per playlist
  max_favorites: 100         # Maximum favorite tracks per user
  enable_persistence: true   # Save playlists across restarts

# Permission system
permissions:
  allow_self_skip: true      # Users can skip their own tracks
  skip_vote_threshold: 0.5   # Percentage needed for vote skip
  dj_role_name: "DJ"         # Enhanced permissions role
```

## Permissions

The plugin implements a comprehensive permission system:

### User Permissions
- `musicplugin.play` - Play music and basic controls (Default: TRUE)
- `musicplugin.skip` - Skip tracks and vote skip (Default: TRUE)
- `musicplugin.queue` - View and manage queue (Default: TRUE)
- `musicplugin.playlist` - Create and manage playlists (Default: TRUE)

### Moderator Permissions
- `musicplugin.volume` - Change server volume (Default: OP)
- `musicplugin.admin` - Administrative controls (Default: OP)

### DJ Role
Users with the configured DJ role get enhanced permissions without requiring OP status.

## Architecture

The plugin follows modern software architecture principles:

```
music-plugin/
├── service/              # Business logic services
│   ├── AudioPlayerService      # LavaPlayer integration
│   ├── VoiceChannelService     # Voice connection management
│   └── AudioPlayerSendHandler  # JDA audio bridge
├── model/               # Data models
│   ├── GuildMusicPlayer       # Per-guild audio state
│   └── UserPlaylist           # Playlist data structure
├── command/             # Command implementations
│   ├── PlayCommand           # Music playback
│   ├── QueueCommand          # Queue management
│   └── [Other commands]      # Additional functionality
├── util/                # Utility classes
│   └── FormatUtil           # Time formatting and display
├── MusicPlugin          # Main plugin class
├── MusicManager         # Service coordination
└── PlaylistManager      # Playlist persistence
```

### Key Improvements Over Legacy

✅ **Modern Architecture**: Service-oriented design with proper separation of concerns  
✅ **Enhanced Audio**: Integration with Fluxcord's AudioPipeline for mixing capabilities  
✅ **Better Error Handling**: Comprehensive error handling with user-friendly messages  
✅ **Configuration-Driven**: Extensive configuration options with environment variable support  
✅ **Internationalization**: Multi-language support (English and French included)  
✅ **Robust Voice Management**: Auto-leave, reconnection, and voice state handling  
✅ **Queue Persistence**: Save and restore queues across restarts (configurable)  
✅ **Permission Integration**: Proper integration with Fluxcord permission system  
✅ **Rich Embeds**: Beautiful Discord embeds with progress bars and metadata  
✅ **Modular Commands**: Clean command architecture with proper validation  

## Development Status

### ✅ Implemented Features

- **Core Audio System**: LavaPlayer integration with multi-source support
- **Voice Management**: Connection handling, auto-leave, voice state tracking
- **Basic Commands**: Play, pause, skip, queue, volume, stop, loop, shuffle, nowplaying, clear
- **Queue System**: Advanced queue management with loop and shuffle modes
- **Configuration**: Comprehensive YAML-based configuration system
- **Permissions**: Integration with Fluxcord permission system
- **Rich UI**: Discord embeds with progress bars and track information

### 🚧 In Development

- **Advanced Sources**: Spotify and SoundCloud API integration
- **Playlist Persistence**: Database/file storage for playlists
- **Search Command**: Multi-platform search functionality
- **Command Registration**: Integration with Fluxcord command system
- **Audio Effects**: Equalizer and audio processing features

### 📋 Planned Features

- **Lyrics Integration**: Show lyrics for current tracks
- **Web Dashboard**: Web interface for playlist management
- **Statistics**: Play count and usage analytics
- **Advanced Queue**: Priority queuing and queue manipulation
- **Radio Mode**: Continuous playback with recommendations

## Troubleshooting

### Common Issues

**Plugin Not Loading?**
- Verify Java 17+ compatibility
- Check `plugin.yml` format and main class path
- Review logs for dependency conflicts

**Audio Not Playing?**
- Ensure bot has voice permissions in Discord
- Check voice channel connection status
- Verify LavaPlayer dependencies are present

**Commands Not Working?**
- Confirm command registration with Fluxcord command system
- Check user permissions for specific commands
- Verify plugin is enabled in configuration

**Performance Issues?**
- Reduce `max_queue_size` for lower memory usage
- Set `audio_quality` to "low" for better performance
- Disable `enable_caching` if experiencing memory issues

### Debug Mode

Enable debug logging in configuration:
```yaml
logging:
  debug: true
  log_commands: true
```

## Contributing

This plugin is part of the Fluxcord ecosystem. Contributions are welcome!

### Development Setup

1. Fork the Fluxcord repository
2. Set up the development environment
3. Make changes in the `plugins/music-plugin` directory
4. Test with the Fluxcord framework
5. Submit a pull request

### Code Standards

- Follow Java 17 conventions
- Use proper logging with SLF4J
- Implement comprehensive error handling
- Add unit tests for new features
- Update documentation for changes

## License

This plugin is part of Fluxcord and is licensed under the MIT License. See the main repository LICENSE file for details.

## Support

- **Documentation**: Check the Fluxcord documentation
- **Issues**: Report bugs on GitHub Issues
- **Discussions**: Join community discussions
- **Discord**: Get help in the development Discord server

---

**Music Plugin for Fluxcord** - *Bringing modern music bot functionality to the Fluxcord ecosystem.*
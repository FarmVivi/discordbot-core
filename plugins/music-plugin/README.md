# Music Plugin

Advanced music bot functionality for Fluxcord with comprehensive playlist management and audio processing.

## Features

### 🎵 Music Playback

- **Multi-platform Support**: Play music from YouTube, Spotify, SoundCloud
- **High-quality Audio**: Lossless audio streaming with configurable bitrates
- **Smart Search**: Intelligent search across multiple platforms
- **URL Support**: Direct links and playlist imports

### 📜 Queue Management

- **Dynamic Queue**: Add, remove, and reorder tracks
- **Queue Persistence**: Save and restore queues across restarts
- **Shuffle & Loop**: Advanced playback modes
- **Skip Voting**: Democratic skip system for shared listening

### 📋 Playlist System

- **Personal Playlists**: Create and manage custom playlists
- **Shared Playlists**: Collaborative playlists for servers
- **Import/Export**: Backup and transfer playlists
- **Favorites**: Quick access to favorite tracks

### 🎛️ Audio Control

- **Volume Control**: Per-server volume settings
- **Audio Effects**: Equalizer, bass boost, nightcore, and more
- **Audio Filters**: Real-time audio processing
- **Quality Settings**: Adaptive bitrate based on connection

### 📊 Rich Interface

- **Now Playing**: Beautiful embeds with track information
- **Progress Bars**: Visual playback progress
- **Album Art**: Display track artwork and metadata
- **Interactive Controls**: Reaction-based playback controls

## Commands

| Command              | Description                     | Permission       |
|----------------------|---------------------------------|------------------|
| `/play <query>`      | Play music from search or URL   | `music.play`     |
| `/pause`             | Pause/resume playback           | `music.play`     |
| `/skip`              | Skip current track              | `music.skip`     |
| `/queue`             | Show current queue              | `music.queue`    |
| `/volume <0-100>`    | Set playback volume             | `music.volume`   |
| `/nowplaying`        | Show current track info         | `music.queue`    |
| `/playlist <action>` | Manage playlists                | `music.playlist` |
| `/search <query>`    | Search for tracks               | `music.play`     |
| `/shuffle`           | Toggle queue shuffle            | `music.queue`    |
| `/loop <mode>`       | Set loop mode (off/track/queue) | `music.queue`    |
| `/clear`             | Clear the queue                 | `music.admin`    |
| `/remove <index>`    | Remove track from queue         | `music.queue`    |

## Installation

1. **Build the plugin**
   ```bash
   cd plugins/music-plugin
   mvn clean package
   ```

2. **Install dependencies**
   ```bash
   # Copy the built JAR to plugins directory
   cp target/music-plugin-*.jar ../../plugins/
   ```

3. **Configure the plugin**
    - Edit server configuration to set up API keys (optional)
    - Configure default volume and queue settings
    - Set up custom prefixes and permissions

4. **Restart the bot** to load the music plugin

## Configuration

```yaml
music:
  # Default playback volume (0-100)
  default_volume: 50
  
  # Maximum queue size per server
  max_queue_size: 100
  
  # Maximum track duration (milliseconds)
  max_track_duration: 600000  # 10 minutes
  
  # Platform support
  enable_spotify: true
  enable_soundcloud: true
  
  # Auto-leave settings
  auto_leave_timeout: 300000  # 5 minutes
  
  # Audio quality (low/medium/high)
  audio_quality: "medium"
```

## Permissions

- `music.play` - Play music and basic controls
- `music.skip` - Skip tracks and vote skip
- `music.queue` - View and manage queue
- `music.volume` - Change server volume
- `music.playlist` - Create and manage playlists
- `music.admin` - Administrative controls (clear queue, force skip)

## Development Status

🚧 **Under Development** - This plugin is currently being implemented.

Core features planned:

- [x] Basic plugin structure and commands
- [ ] LavaPlayer integration for audio playback
- [ ] Queue management system
- [ ] Playlist persistence and management
- [ ] Multi-platform source support
- [ ] Audio effects and filters
- [ ] Rich embed interfaces
- [ ] Permission system integration

## Contributing

This plugin is part of the Fluxcord ecosystem. Contributions are welcome!

1. Fork the repository
2. Create a feature branch
3. Implement your changes in the music plugin
4. Test with the Fluxcord framework
5. Submit a pull request

## License

Part of Fluxcord - Licensed under MIT License
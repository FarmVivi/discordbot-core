# Migration Notes: Legacy Music Module to Modern Music Plugin

This document provides detailed information about migrating from the legacy music module (main branch) to the new modern music plugin (develop branch).

## Overview

The music functionality has been completely redesigned as a plugin for the new Fluxcord architecture, bringing significant improvements in performance, maintainability, and features.

## Key Architectural Changes

### From Monolithic Module to Plugin System

**Legacy (Main Branch)**
- Integrated directly into core bot functionality
- Single `MusicModule.java` with 29 Java files
- Tightly coupled with bot core systems
- No plugin lifecycle management

**Modern (Develop Branch)**
- Standalone plugin with proper lifecycle management
- Service-oriented architecture with clear separation
- Loose coupling through well-defined APIs
- Plugin hot-loading and configuration management

### Service Architecture

**Legacy Structure**
```
src/main/java/fr/farmvivi/discordbot/module/music/
├── MusicModule.java (monolithic)
├── MusicPlayer.java
├── TrackScheduler.java
├── MusicEventHandler.java
├── command/ (21+ command files)
├── sourcemanager/
└── utils/
```

**Modern Structure**
```
plugins/music-plugin/src/main/java/fr/farmvivi/fluxcord/plugins/music/
├── MusicPlugin.java (plugin entry point)
├── MusicManager.java (service coordinator)
├── PlaylistManager.java (playlist persistence)
├── service/ (business logic services)
├── model/ (data models)
├── command/ (modular commands)
└── util/ (utilities)
```

## Feature Comparison

### Core Features

| Feature | Legacy | Modern | Status | Notes |
|---------|--------|--------|--------|-------|
| Play Music | ✅ | ✅ | **Improved** | Better source handling |
| Queue Management | ✅ | ✅ | **Enhanced** | More controls, better UI |
| Voice Channel Management | ✅ | ✅ | **Improved** | Auto-leave, reconnection |
| Volume Control | ✅ | ✅ | **Same** | 0-100% range |
| Loop Modes | ✅ | ✅ | **Enhanced** | Track/queue/off modes |
| Shuffle | ✅ | ✅ | **Improved** | Better randomization |
| Skip/Pause/Stop | ✅ | ✅ | **Same** | Full compatibility |

### Advanced Features

| Feature | Legacy | Modern | Status | Notes |
|---------|--------|--------|--------|-------|
| Equalizer | ✅ | 🚧 | **Planned** | Redesigned for new audio system |
| Search Command | ✅ | 🚧 | **In Development** | Multi-platform integration |
| Radio Mode | ✅ | 📋 | **Planned** | Will be reimplemented |
| Seek Command | ✅ | 📋 | **Planned** | Track position control |
| Now Playing Display | ✅ | ✅ | **Enhanced** | Rich embeds, progress bars |

### New Features (Not in Legacy)

| Feature | Description | Status |
|---------|-------------|--------|
| Playlist Persistence | Save playlists across restarts | ✅ **Available** |
| Rich Discord Embeds | Beautiful UI with progress bars | ✅ **Available** |
| Multi-language Support | English and French translations | ✅ **Available** |
| Configuration System | YAML-based plugin configuration | ✅ **Available** |
| Permission Integration | Fluxcord permission system | ✅ **Available** |
| Service Architecture | Modular, maintainable codebase | ✅ **Available** |

## Configuration Migration

### Legacy Configuration
The legacy module used hard-coded values and bot-wide configuration:

```java
public static final int QUIT_TIMEOUT = 900;
public static final int DEFAULT_VOICE_VOLUME = 5;
public static final int DEFAULT_RADIO_VOLUME = 25;
```

### Modern Configuration
The modern plugin uses comprehensive YAML configuration:

```yaml
# plugins/music-plugin/config.yml
music:
  default_volume: 50
  max_queue_size: 100
  max_track_duration: 600000

voice:
  auto_leave: true
  auto_leave_timeout: 300

permissions:
  allow_self_skip: true
  skip_vote_threshold: 0.5
```

### Migration Steps
1. Review legacy bot configuration
2. Map existing settings to new YAML format
3. Configure new features as desired
4. Test with development environment

## Command Changes

### Command Naming
Most commands remain the same, but some have been enhanced:

| Legacy Command | Modern Command | Changes |
|----------------|----------------|---------|
| `/play` | `/play` | Enhanced with better error handling |
| `/pause` | `/pause` | Now properly toggles pause/resume |
| `/skip` | `/skip` | Improved feedback and error messages |
| `/queue` | `/queue` | Rich embeds with progress bars |
| `/volume` | `/volume` | Same functionality, better validation |
| `/current` | `/nowplaying` | Alias: `np`, `current` - Enhanced display |
| `/clearqueue` | `/clear` | Renamed for consistency |

### New Commands
- `/stop` - Stops playback and optionally leaves channel
- Enhanced `/loop` with modes (off/track/queue)
- Better `/shuffle` with queue randomization

### Removed Commands (Temporarily)
These legacy commands are being reimplemented:
- `/seek` - Track position seeking
- `/replay` - Restart current track
- `/radio` - Radio mode playback
- Equalizer commands (`/eqstart`, `/eqstop`, etc.)

## Permission System Migration

### Legacy Permissions
The legacy system used basic Discord permissions and role checks.

### Modern Permissions
The new system integrates with Fluxcord's permission framework:

```yaml
# Permission nodes
musicplugin.play       # Play music (default: TRUE)
musicplugin.skip       # Skip tracks (default: TRUE)  
musicplugin.queue      # Queue management (default: TRUE)
musicplugin.volume     # Volume control (default: OP)
musicplugin.playlist   # Playlist management (default: TRUE)
musicplugin.admin      # Admin controls (default: OP)
```

## Data Migration

### Queue Persistence
**Legacy**: Queues were lost on restart
**Modern**: Optional queue persistence available

### Playlists
**Legacy**: No persistent playlists
**Modern**: Full playlist system with user accounts

### Migration Process
1. Export any important data from legacy system
2. Configure new playlist system
3. Manually recreate important playlists
4. Train users on new playlist commands

## Performance Improvements

### Memory Usage
- **Legacy**: Higher memory usage due to monolithic design
- **Modern**: Optimized memory usage with proper resource management

### Audio Quality
- **Legacy**: Fixed audio settings
- **Modern**: Configurable quality levels (low/medium/high)

### Connection Handling
- **Legacy**: Basic reconnection logic
- **Modern**: Robust reconnection with auto-leave functionality

## Installation Process

### Prerequisites
1. Ensure you're running Fluxcord develop branch
2. Java 17+ environment
3. Updated bot permissions in Discord

### Step-by-Step Migration

1. **Backup Current Setup**
   ```bash
   # Backup current configuration
   cp config.yml config.yml.backup
   ```

2. **Build the New Plugin**
   ```bash
   cd plugins/music-plugin
   mvn clean package
   ```

3. **Install the Plugin**
   ```bash
   cp target/music-plugin-*.jar ../../plugins/
   ```

4. **Configure the Plugin**
   ```bash
   # Edit configuration
   nano plugins/music-plugin/config.yml
   ```

5. **Update Bot Configuration**
   - Remove legacy music module references
   - Ensure plugin loading is enabled

6. **Restart and Test**
   ```bash
   # Restart the bot
   ./restart.sh
   ```

### Testing Checklist

After migration, verify these functions work:

- [ ] Basic playback (`/play`, `/pause`, `/skip`)
- [ ] Queue display (`/queue`)
- [ ] Volume control (`/volume`)
- [ ] Voice channel connection
- [ ] Auto-leave functionality
- [ ] Permission system
- [ ] Loop and shuffle modes
- [ ] Now playing display

## Troubleshooting

### Common Migration Issues

**Plugin Not Loading**
- Check Java version (requires 17+)
- Verify plugin JAR is in correct directory
- Review logs for dependency issues

**Commands Not Working**
- Ensure command registration is complete
- Check permission configuration
- Verify user has required permissions

**Audio Issues**
- Confirm Discord bot permissions
- Check voice channel connectivity
- Review audio source configuration

**Performance Problems**
- Reduce queue size limits
- Lower audio quality setting
- Disable unnecessary features

### Getting Help

1. **Check Logs**: Review bot logs for error messages
2. **Configuration**: Verify YAML syntax and values
3. **Permissions**: Ensure proper Discord and plugin permissions
4. **Documentation**: Refer to plugin README and configuration docs
5. **Community**: Ask for help in Discord or GitHub discussions

## Benefits of Migration

### For Users
- **Better User Experience**: Rich embeds, progress bars, better feedback
- **More Reliable**: Improved connection handling and error recovery
- **New Features**: Playlists, favorites, enhanced queue management
- **Faster Response**: Optimized command processing

### For Administrators
- **Easier Configuration**: YAML-based configuration system
- **Better Monitoring**: Enhanced logging and debugging options
- **Flexible Permissions**: Granular permission control
- **Maintenance**: Cleaner codebase, easier to update

### For Developers
- **Modern Architecture**: Service-oriented, maintainable design
- **Plugin System**: Easy to extend and customize
- **Better Testing**: Comprehensive test coverage
- **Documentation**: Well-documented APIs and examples

## Future Roadmap

### Short Term (Next Release)
- Complete command registration integration
- Implement remaining legacy commands
- Enhanced source manager configuration
- Playlist management commands

### Medium Term
- Audio effects and equalizer
- Web dashboard integration
- Advanced queue manipulation
- Statistics and analytics

### Long Term
- AI-powered recommendations
- Cross-server playlist sharing
- Advanced audio processing
- Mobile app integration

---

This migration brings the music functionality into the modern Fluxcord ecosystem while maintaining compatibility and adding significant improvements. The new architecture ensures better performance, maintainability, and extensibility for future development.
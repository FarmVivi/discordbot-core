# Migration Notes: Legacy Music Module to Fluxcord Music Plugin

This document details the migration from the legacy music module (branch `main`) to the new Fluxcord plugin architecture (branch `develop`).

## Overview

The music functionality has been completely reimplemented as a Fluxcord plugin, utilizing the framework's modern APIs for audio, commands, internationalization, and storage.

## Key Differences

### Architecture

**Legacy (main branch)**
- Monolithic module within the main bot codebase
- Direct JDA audio manager usage
- Custom command handling
- Hard-coded French messages
- In-memory state management

**New Plugin (develop branch)**
- Standalone plugin following Fluxcord architecture
- Uses Fluxcord Audio Service API
- Integrated with Fluxcord Command Service
- Full i18n support via Language Service
- Persistent storage via Storage Service

### Command Changes

| Legacy Command | New Command | Notes |
|----------------|-------------|-------|
| `!play <query>` | `/play <query>` | Now uses slash commands |
| `!pause` | `/pause` | Same functionality |
| `!skip` / `!next` | `/skip` | Unified command |
| `!stop` / `!leave` | `/stop` | Unified command |
| `!queue` | `/queue [page]` | Added pagination |
| `!np` / `!now` / `!current` | `/nowplaying` | Unified command |
| `!volume <level>` | `/volume [level]` | Shows current if no level |
| `!loop` | `/loop [mode]` | Added mode selection |
| `!loopqueue` | `/loop queue` | Part of loop command |
| `!shuffle` | `/shuffle` | Same functionality |
| `!clearqueue` | `/clear` | Renamed for clarity |
| `!seek <time>` | `/seek <time>` | Same functionality |
| `!replay` | `/seek 0` | Use seek to start |
| `!radio` | Not implemented | Feature removed |
| `!eq*` commands | Not implemented | Equalizer pending |

### Feature Parity

✅ **Implemented**
- All core playback functionality
- Queue management (add, remove, clear, shuffle)
- Loop modes (track and queue)
- Volume control
- Seek functionality
- Multi-source support (YouTube, Spotify, SoundCloud, etc.)
- Persistent player message with button controls
- Auto-disconnect after inactivity
- Multi-language support

❌ **Not Implemented** (from legacy)
- Radio streaming from local files
- Equalizer commands (can be added if needed)
- Vote skip (configuration exists but not implemented)

✨ **New Features**
- Slash commands with autocomplete
- Playlist management system
- Button-based player controls
- Better error messages with i18n
- Configurable provider API keys
- Performance tuning options
- Plugin-based architecture for easy updates

### Dependencies

The following dependencies from the legacy module have been integrated:

```xml
<!-- LavaPlayer and sources -->
<dependency>
    <groupId>com.github.walkyst</groupId>
    <artifactId>lavaplayer-fork</artifactId>
    <version>1.4.3</version>
</dependency>

<!-- YouTube support -->
<dependency>
    <groupId>dev.lavalink.youtube</groupId>
    <artifactId>common</artifactId>
    <version>1.13.2</version>
</dependency>
<dependency>
    <groupId>dev.lavalink.youtube</groupId>
    <artifactId>v2</artifactId>
    <version>1.13.2</version>
</dependency>

<!-- Additional sources (Spotify, Deezer, etc.) -->
<dependency>
    <groupId>com.github.topi314.lavasrc</groupId>
    <artifactId>lavasrc</artifactId>
    <version>4.6.0</version>
</dependency>
<dependency>
    <groupId>com.github.topi314.lavasrc</groupId>
    <artifactId>protocol-jvm</artifactId>
    <version>4.6.0</version>
</dependency>
```

### Configuration Migration

**Legacy Configuration (hardcoded)**
```java
public static final int DEFAULT_VOICE_VOLUME = 5;
public static final int DEFAULT_RADIO_VOLUME = 25;
public static final int QUIT_TIMEOUT = 900; // seconds
```

**New Configuration (config.yml)**
```yaml
music:
  default_volume: 50  # Percentage (legacy 5 = 50%)
  auto_leave_timeout: 300000  # Milliseconds (legacy 900s = 300000ms)

providers:
  spotify:
    client_id: ""  # Now configurable
    client_secret: ""
  # ... other providers
```

### API Usage

**Audio Handling**

Legacy:
```java
guild.getAudioManager().setSendingHandler(audioSendHandler);
guild.getAudioManager().openAudioConnection(voiceChannel);
```

New Plugin:
```java
audioService.registerSendHandler(guild, plugin, sendHandler, volume, priority);
guild.getAudioManager().openAudioConnection(voiceChannel); // Still used for connection
```

**Command Registration**

Legacy:
```java
commandsModule.registerCommand(module, new PlayCommand(this));
```

New Plugin:
```java
commandService.registerCommand(plugin, builder -> {
    builder.name("play")
           .description(lang.getString("music.command.play.description"))
           .stringOption("query", lang.getString("music.command.play.option.query"), true)
           .executor((ctx, cmd) -> { /* ... */ });
});
```

**Internationalization**

Legacy:
```java
// Hard-coded French messages
reply.error("Impossible de trouver la piste demandée.");
```

New Plugin:
```java
// Uses Language Service with multiple locales
ctx.replyError(lang.getString(guild, "music.error.no_matches"));
```

### Persistent Player Message

The player message implementation has been significantly improved:

**Legacy**
- Used debouncing for updates
- Basic button layout
- No persistence across restarts
- Manual message management

**New Plugin**
- Throttled updates with better performance
- Enhanced button layout with visual states
- Persists message ID/channel ID across restarts
- Automatic cleanup and recreation
- Utilizes Fluxcord's storage API

## Migration Steps

1. **Remove Legacy Module**
   - Remove music module files from main branch codebase
   - Remove music module registration from `ModulesManager`

2. **Install New Plugin**
   - Build the music plugin: `mvn clean package`
   - Copy JAR to `plugins/` directory
   - Start bot to generate configuration

3. **Configure Providers**
   - Add API keys to `plugins/music-plugin/config.yml`
   - Adjust settings as needed
   - Restart bot

4. **Update Permissions**
   - Legacy used basic command permissions
   - New plugin uses: `music.play`, `music.skip`, `music.queue`, `music.volume`, `music.admin`
   - Update your permission system accordingly

5. **Test Functionality**
   - Test all commands with various sources
   - Verify player message appears and updates
   - Check button interactions work
   - Test persistence across restarts

## Troubleshooting

### Player message not appearing
- Ensure bot has permission to send embeds and add reactions
- Check logs for storage errors
- Verify message wasn't manually deleted

### Audio not playing
- Check provider configuration (API keys)
- Verify Fluxcord Audio Service is functioning
- Review logs for source loading errors

### Commands not working
- Ensure slash commands are registered (may take up to an hour)
- Verify permissions are set correctly
- Check command service is enabled

## Future Enhancements

The new plugin architecture makes it easy to add:
- Equalizer support (if requested)
- Additional audio sources
- Advanced queue features (priority queue, fair queue)
- Integration with other Fluxcord plugins
- Web dashboard support

## Support

For issues or questions about the migration:
1. Check the plugin README
2. Review Fluxcord documentation
3. Check logs for detailed error messages
4. Open an issue with reproduction steps
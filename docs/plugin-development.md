# Plugin Development Guide

This guide covers everything you need to know about developing plugins for DiscordBot Core.

## Quick Start

1. **Copy the plugin template**
   ```bash
   cp -r plugin-template my-awesome-plugin
   cd my-awesome-plugin
   ```

2. **Customize your plugin**
   - Edit `pom.xml` with your plugin details
   - Rename the package and main class
   - Implement your plugin functionality

3. **Build and test**
   ```bash
   mvn clean package
   cp target/my-awesome-plugin-*.jar ../plugins/
   ```

## Plugin Structure

### Basic Plugin Class

```java
package com.example.myplugin;

import fr.farmvivi.discordbot.core.api.plugin.AbstractPlugin;

public class MyPlugin extends AbstractPlugin {
    
    @Override
    public String getName() {
        return "MyAwesome Plugin";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public void onEnable() {
        // Plugin initialization
        logger.info("Plugin enabled!");
    }
    
    @Override
    public void onDisable() {
        // Plugin cleanup
        logger.info("Plugin disabled!");
    }
}
```

### Plugin Lifecycle

1. **Loading**: Plugin JAR is discovered and loaded
2. **Pre-Enable**: Early initialization phase
3. **Enable**: Main plugin activation
4. **Post-Enable**: Final setup after all plugins are enabled
5. **Running**: Normal operation
6. **Pre-Disable**: Preparation for shutdown
7. **Disable**: Main cleanup phase
8. **Post-Disable**: Final cleanup after all plugins are disabled

## Commands

### Creating Commands

```java
@Command(name = "hello", description = "Say hello to someone")
public CommandResult helloCommand(CommandContext ctx) {
    String target = ctx.getOption("user", String.class);
    if (target == null) {
        target = ctx.getUser().getEffectiveName();
    }
    
    ctx.reply("Hello, " + target + "! 👋");
    return CommandResult.SUCCESS;
}
```

### Command Options

```java
@Command(
    name = "ban", 
    description = "Ban a user from the server",
    options = {
        @CommandOption(
            name = "user",
            description = "The user to ban",
            type = OptionType.USER,
            required = true
        ),
        @CommandOption(
            name = "reason", 
            description = "Reason for the ban",
            type = OptionType.STRING,
            required = false
        )
    }
)
public CommandResult banCommand(CommandContext ctx) {
    User user = ctx.getOption("user", User.class);
    String reason = ctx.getOption("reason", "No reason provided");
    
    // Implementation
    return CommandResult.SUCCESS;
}
```

### Permission Checks

```java
@Command(name = "admin", description = "Admin-only command")
public CommandResult adminCommand(CommandContext ctx) {
    if (!getPluginPermissionManager().hasPermission(ctx.getUser(), "myplugin.admin")) {
        ctx.reply("❌ You don't have permission to use this command!");
        return CommandResult.NO_PERMISSION;
    }
    
    // Admin functionality
    return CommandResult.SUCCESS;
}
```

## Event Handling

### Discord Events

```java
@EventHandler
public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) return;
    
    String content = event.getMessage().getContentRaw();
    if (content.contains("hello")) {
        event.getMessage().addReaction("👋").queue();
    }
}

@EventHandler(priority = EventPriority.HIGH)
public void onGuildMemberJoin(GuildMemberJoinEvent event) {
    // Welcome new members
    String welcome = String.format("Welcome %s to %s!", 
        event.getUser().getAsMention(), 
        event.getGuild().getName());
    
    // Send to system channel
    event.getGuild().getSystemChannel().sendMessage(welcome).queue();
}
```

### Custom Events

```java
// Create custom event
public class PlayerLevelUpEvent extends Event {
    private final User player;
    private final int newLevel;
    
    public PlayerLevelUpEvent(User player, int newLevel) {
        this.player = player;
        this.newLevel = newLevel;
    }
    
    // Getters...
}

// Fire custom event
PlayerLevelUpEvent event = new PlayerLevelUpEvent(user, newLevel);
getEventManager().fireEvent(event);

// Handle custom event
@EventHandler
public void onPlayerLevelUp(PlayerLevelUpEvent event) {
    // Congratulate player
}
```

## Configuration

### Reading Configuration

```java
@Override
public void onEnable() {
    // Load configuration with defaults
    int maxLevel = getPluginConfig().getInt("gameplay.max_level", 100);
    String currency = getPluginConfig().getString("economy.currency", "coins");
    boolean enablePvP = getPluginConfig().getBoolean("gameplay.pvp", false);
    
    logger.info("Max level: {}, Currency: {}, PvP: {}", maxLevel, currency, enablePvP);
}
```

### Setting Configuration

```java
public void updateSettings(int newMaxLevel) {
    getPluginConfig().set("gameplay.max_level", newMaxLevel);
    // Configuration is automatically saved
}
```

### Configuration File Structure

```yaml
# config.yml
gameplay:
  max_level: 100
  enable_pvp: false
  respawn_time: 30

economy:
  currency: "coins"
  starting_balance: 1000
  daily_bonus: 100

messages:
  welcome: "Welcome to the server!"
  goodbye: "Thanks for playing!"
```

## Data Storage

### Simple Storage

```java
// Store player data
public void savePlayerLevel(String playerId, int level) {
    getPluginDataStorage().set("players." + playerId + ".level", level);
}

// Retrieve player data
public int getPlayerLevel(String playerId) {
    return getPluginDataStorage().getInt("players." + playerId + ".level", 1);
}

// Store complex objects
public void savePlayerData(String playerId, PlayerData data) {
    getPluginDataStorage().set("players." + playerId, data.toMap());
}
```

### Binary Storage

```java
// Store files
public void savePlayerAvatar(String playerId, byte[] imageData) {
    BinaryStorageKey key = new BinaryStorageKey("avatars", playerId + ".png");
    try (OutputStream out = getPluginBinaryStorage().getOutputStream(key, true)) {
        out.write(imageData);
    } catch (IOException e) {
        logger.error("Failed to save avatar", e);
    }
}

// Retrieve files
public byte[] getPlayerAvatar(String playerId) {
    BinaryStorageKey key = new BinaryStorageKey("avatars", playerId + ".png");
    return getPluginBinaryStorage().getInputStream(key)
        .map(this::readAllBytes)
        .orElse(null);
}
```

## Permissions

### Registering Permissions

```java
@Override
public void onEnable() {
    PluginPermissionAdapter perms = getPluginPermissionManager();
    
    // Basic permissions
    perms.registerPermission("myplugin.use", PermissionDefault.TRUE);
    perms.registerPermission("myplugin.admin", PermissionDefault.OPERATOR);
    
    // Hierarchical permissions
    perms.registerPermission("myplugin.economy.use", PermissionDefault.TRUE);
    perms.registerPermission("myplugin.economy.admin", PermissionDefault.OPERATOR);
}
```

### Checking Permissions

```java
public boolean canUseFeature(User user, String feature) {
    return getPluginPermissionManager().hasPermission(user, "myplugin." + feature);
}

public void restrictedAction(CommandContext ctx) {
    if (!canUseFeature(ctx.getUser(), "admin")) {
        ctx.reply("❌ Insufficient permissions!");
        return;
    }
    
    // Perform restricted action
}
```

## Internationalization

### Setting Up Languages

```java
@Override
public void onEnable() {
    // Register language namespace
    getPluginLanguageAdapter().registerNamespace("myplugin");
    
    // Load language files
    getPluginLanguageAdapter().loadLanguageFile("en-US", "lang/en.yml");
    getPluginLanguageAdapter().loadLanguageFile("fr-FR", "lang/fr.yml");
}
```

### Language Files

```yaml
# lang/en.yml
myplugin:
  commands:
    hello: "Hello, {player}!"
    goodbye: "Goodbye, {player}!"
  errors:
    permission_denied: "You don't have permission!"
    player_not_found: "Player not found!"
```

```yaml
# lang/fr.yml
myplugin:
  commands:
    hello: "Bonjour, {player} !"
    goodbye: "Au revoir, {player} !"
  errors:
    permission_denied: "Vous n'avez pas la permission !"
    player_not_found: "Joueur introuvable !"
```

### Using Translations

```java
public void greetPlayer(CommandContext ctx) {
    String message = getPluginLanguageAdapter().getString(
        ctx.getGuild(), 
        "myplugin.commands.hello", 
        "Hello, {player}!"
    );
    
    // Replace placeholders
    message = message.replace("{player}", ctx.getUser().getEffectiveName());
    
    ctx.reply(message);
}
```

## Audio Processing

### Basic Audio Handler

```java
public class MyAudioHandler implements AudioSendHandler {
    private final AudioInputStream audioStream;
    
    public MyAudioHandler(File audioFile) throws Exception {
        this.audioStream = AudioSystem.getAudioInputStream(audioFile);
    }
    
    @Override
    public boolean canProvide() {
        return audioStream.available() > 0;
    }
    
    @Override
    public ByteBuffer provide() {
        byte[] data = new byte[3840]; // 20ms of 48KHz 16-bit stereo PCM
        try {
            int read = audioStream.read(data);
            return ByteBuffer.wrap(data, 0, read);
        } catch (IOException e) {
            return null;
        }
    }
    
    @Override
    public boolean isOpus() {
        return false; // We're providing PCM
    }
}
```

### Using Audio Service

```java
@Command(name = "play", description = "Play audio in voice channel")
public CommandResult playCommand(CommandContext ctx) {
    VoiceChannel channel = ctx.getMember().getVoiceState().getChannel();
    if (channel == null) {
        ctx.reply("❌ You must be in a voice channel!");
        return CommandResult.ERROR;
    }
    
    try {
        File audioFile = new File("sounds/example.wav");
        MyAudioHandler handler = new MyAudioHandler(audioFile);
        
        getAudioService().registerSendHandler(
            ctx.getGuild(), 
            this, // plugin instance
            handler, 
            AudioPriority.NORMAL
        );
        
        ctx.reply("🎵 Playing audio!");
        return CommandResult.SUCCESS;
    } catch (Exception e) {
        ctx.reply("❌ Failed to play audio: " + e.getMessage());
        return CommandResult.ERROR;
    }
}
```

## Best Practices

### Error Handling

```java
@Command(name = "risky", description = "Command that might fail")
public CommandResult riskyCommand(CommandContext ctx) {
    try {
        // Risky operation
        performRiskyOperation();
        ctx.reply("✅ Operation successful!");
        return CommandResult.SUCCESS;
    } catch (ValidationException e) {
        ctx.reply("❌ Invalid input: " + e.getMessage());
        return CommandResult.ERROR;
    } catch (PermissionException e) {
        ctx.reply("❌ Permission denied!");
        return CommandResult.NO_PERMISSION;
    } catch (Exception e) {
        logger.error("Unexpected error in risky command", e);
        ctx.reply("❌ An unexpected error occurred!");
        return CommandResult.ERROR;
    }
}
```

### Async Operations

```java
@Command(name = "slow", description = "Long-running command")
public CommandResult slowCommand(CommandContext ctx) {
    ctx.deferReply(); // Show "thinking" indicator
    
    // Perform long operation asynchronously
    CompletableFuture.supplyAsync(() -> {
        // Long operation here
        return performLongOperation();
    }).thenAccept(result -> {
        ctx.followUp("✅ Operation completed: " + result);
    }).exceptionally(throwable -> {
        logger.error("Long operation failed", throwable);
        ctx.followUp("❌ Operation failed!");
        return null;
    });
    
    return CommandResult.SUCCESS;
}
```

### Resource Management

```java
@Override
public void onDisable() {
    // Close database connections
    if (database != null) {
        database.close();
    }
    
    // Stop scheduled tasks
    if (scheduler != null) {
        scheduler.shutdown();
    }
    
    // Save pending data
    saveAllPlayerData();
    
    logger.info("Plugin disabled and cleaned up!");
}
```

## Testing

### Unit Testing

```java
public class MyPluginTest {
    private MyPlugin plugin;
    private TestPluginContext context;
    
    @BeforeEach
    void setUp() {
        plugin = new MyPlugin();
        context = new TestPluginContext();
        plugin.onLoad(context);
        plugin.onEnable();
    }
    
    @Test
    void testHelloCommand() {
        TestCommandContext ctx = new TestCommandContext();
        CommandResult result = plugin.helloCommand(ctx);
        
        assertEquals(CommandResult.SUCCESS, result);
        assertTrue(ctx.getReply().contains("Hello"));
    }
}
```

## Examples

See the following example plugins:

- **[Audio Example](../discordbot-example-audio/)**: Basic audio processing
- **[Music Plugin](../plugins/music-plugin/)**: Advanced music bot
- **[AI Audio Plugin](../plugins/ai-audio-plugin/)**: AI-powered voice processing
- **[Plugin Template](../plugin-template/)**: Starting template

## Troubleshooting

### Common Issues

1. **Plugin not loading**: Check plugin.yml format and main class
2. **Commands not registering**: Verify @Command annotations and permissions
3. **Events not firing**: Check @EventHandler annotations and event types
4. **Configuration not saving**: Ensure proper error handling
5. **Permissions not working**: Register permissions in onEnable()

### Debugging

```java
@Override
public void onEnable() {
    logger.debug("Debug mode enabled");
    logger.info("Plugin configuration: {}", getPluginConfig().getValues());
    logger.warn("This is a warning message");
    logger.error("This is an error message");
}
```

## Resources

- [DiscordBot Core API Documentation](api-reference.md)
- [Command System Guide](commands.md)
- [Audio API Guide](audio-api.md)
- [JDA Documentation](https://jda.wiki/)
- [Discord Developer Portal](https://discord.com/developers/docs)
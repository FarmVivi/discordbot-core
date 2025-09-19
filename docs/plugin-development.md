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

### Automatic Configuration Management

**DiscordBot Core now provides automatic configuration management for plugins:**

#### Default Configuration Files

When your plugin is loaded, the system automatically:

1. **Copies default config**: If no `config.yml` exists in the plugin folder, it copies from your JAR's resources
2. **Creates directories**: Automatically creates the plugin's data folder structure
3. **Handles versioning**: Manages configuration versions and migrations automatically

**Best Practice**: Always include a `config.yml` file in your plugin's `src/main/resources/` directory.

#### Configuration Versioning

Include version tracking in your default configuration:

```yaml
# config.yml in your plugin's resources
# Configuration version (automatically managed)
config_version: 1

# Your plugin settings
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

#### Migration Support

DiscordBot Core provides two ways to implement configuration migration in your plugin:

1. **Separate Migration Class (Recommended)** - Keeps your main plugin class clean
2. **Direct Implementation** - Implement migration logic directly in your plugin class

##### Option 1: Separate Migration Class (Recommended)

Create a separate class for handling migration logic and specify it in your main plugin class:

```java
package com.example.myplugin;

import fr.farmvivi.discordbot.api.plugin.AbstractPlugin;
import fr.farmvivi.discordbot.api.plugin.ConfigurableMigrationPlugin;

public class MyPlugin extends AbstractPlugin {
    
    @Override
    public Class<? extends ConfigurableMigrationPlugin> getMigrationClass() {
        return MyPluginMigrator.class;
    }
    
    @Override
    public void onEnable() {
        // Your plugin logic here - migration is handled automatically
        logger.info("Plugin enabled with configuration version: {}", 
                   getPluginConfig().getConfigVersion());
    }
}
```

Then create the migration class:

```java
package com.example.myplugin;

import fr.farmvivi.discordbot.api.plugin.ConfigurableMigrationPlugin;
import fr.farmvivi.discordbot.api.config.Configuration;
import fr.farmvivi.discordbot.api.config.ConfigurationException;

public class MyPluginMigrator implements ConfigurableMigrationPlugin {
    
    private static final int CURRENT_CONFIG_VERSION = 2;
    
    @Override
    public int getExpectedConfigVersion() {
        return CURRENT_CONFIG_VERSION;
    }
    
    @Override
    public void migrateConfiguration(Configuration config, int fromVersion, int toVersion) 
            throws ConfigurationException {
        // Migrate step by step
        for (int version = fromVersion; version < toVersion; version++) {
            switch (version) {
                case 0 -> migrateFrom0To1(config);
                case 1 -> migrateFrom1To2(config);
                // Add more migration cases as needed
                default -> throw new ConfigurationException("No migration available from version " + version);
            }
        }
    }
    
    @Override
    public void validateConfiguration(Configuration config) throws ConfigurationException {
        // Validate required settings
        if (!config.contains("api.endpoint")) {
            throw new ConfigurationException("Missing required setting: api.endpoint");
        }
        
        // Validate value ranges
        int maxLevel = config.getInt("gameplay.max_level", 100);
        if (maxLevel < 1 || maxLevel > 1000) {
            throw new ConfigurationException("max_level must be between 1 and 1000, got: " + maxLevel);
        }
    }
    
    private void migrateFrom0To1(Configuration config) throws ConfigurationException {
        // Example: Add new default settings
        if (!config.contains("new_feature")) {
            config.set("new_feature.enabled", true);
            config.set("new_feature.timeout", 30);
        }
    }
    
    private void migrateFrom1To2(Configuration config) throws ConfigurationException {
        // Example: Restructure configuration
        if (config.contains("permissions")) {
            // Move flat permissions to hierarchical structure
            boolean adminPerm = config.getBoolean("permissions.admin", false);
            config.set("permissions.roles.admin.enabled", adminPerm);
        }
    }
}
```

##### Option 2: Direct Implementation

Alternatively, you can implement migration logic directly in your plugin class:

```java
package com.example.myplugin;

import fr.farmvivi.discordbot.api.plugin.AbstractPlugin;
import fr.farmvivi.discordbot.api.plugin.ConfigurableMigrationPlugin;
import fr.farmvivi.discordbot.api.config.Configuration;
import fr.farmvivi.discordbot.api.config.ConfigurationException;

public class MyPlugin extends AbstractPlugin implements ConfigurableMigrationPlugin {
    
    private static final int CURRENT_CONFIG_VERSION = 2;
    
    @Override
    public int getExpectedConfigVersion() {
        return CURRENT_CONFIG_VERSION;
    }
    
    @Override
    public void migrateConfiguration(Configuration config, int fromVersion, int toVersion) 
            throws ConfigurationException {
        logger.info("Migrating {} configuration from version {} to {}", getName(), fromVersion, toVersion);
        
        // Migrate step by step
        for (int version = fromVersion; version < toVersion; version++) {
            switch (version) {
                case 0 -> migrateFrom0To1(config);
                case 1 -> migrateFrom1To2(config);
                // Add more migration cases as needed
                default -> logger.warn("No migration available from version {}", version);
            }
        }
    }
    
    @Override
    public void validateConfiguration(Configuration config) throws ConfigurationException {
        // Validate required settings
        if (!config.contains("api.endpoint")) {
            throw new ConfigurationException("Missing required setting: api.endpoint");
        }
        
        // Validate value ranges
        int maxLevel = config.getInt("gameplay.max_level", 100);
        if (maxLevel < 1 || maxLevel > 1000) {
            throw new ConfigurationException("max_level must be between 1 and 1000, got: " + maxLevel);
        }
        
        logger.info("Configuration validation passed for plugin {}", getName());
    }
    
    private void migrateFrom0To1(Configuration config) throws ConfigurationException {
        // Example: Add new default settings
        if (!config.contains("new_feature")) {
            config.set("new_feature.enabled", true);
            config.set("new_feature.timeout", 30);
        }
        
        // Example: Rename old settings
        if (config.contains("old_setting_name")) {
            Object value = config.getString("old_setting_name", "default");
            config.set("new_setting_name", value);
            // Note: Don't remove old keys here, the system handles cleanup
        }
    }
    
    private void migrateFrom1To2(Configuration config) throws ConfigurationException {
        // Example: Restructure configuration
        if (config.contains("permissions")) {
            // Move flat permissions to hierarchical structure
            boolean adminPerm = config.getBoolean("permissions.admin", false);
            boolean moderatorPerm = config.getBoolean("permissions.moderator", false);
            
            config.set("permissions.roles.admin.enabled", adminPerm);
            config.set("permissions.roles.moderator.enabled", moderatorPerm);
        }
    }
}
```

#### Migration Best Practices

1. **Always increment versions**: When changing config structure, increment `CURRENT_CONFIG_VERSION`
2. **Step-by-step migration**: Migrate one version at a time (v0→v1→v2) to ensure data integrity
3. **Preserve user data**: Never delete user settings without migration
4. **Test thoroughly**: Test migration with real configuration files
5. **Log changes**: Provide clear logging during migration process
6. **Handle errors**: Use try-catch blocks and meaningful error messages

#### Migration Example

```yaml
# Version 0 (original)
permissions:
  admin: true
  moderator: false

# Version 1 (after migrateFrom0To1)
permissions:
  admin: true
  moderator: false
new_feature:
  enabled: true
  timeout: 30

# Version 2 (after migrateFrom1To2)  
permissions:
  roles:
    admin:
      enabled: true
    moderator:
      enabled: false
new_feature:
  enabled: true
  timeout: 30
config_version: 2
```

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
    // Configuration is automatically saved with version tracking
}
```

### Configuration Best Practices

#### For New Plugins

1. **Include Default Config**: Always provide a comprehensive `config.yml` in your JAR's resources
2. **Start with Version 1**: Use `config_version: 1` in your initial configuration
3. **Document Settings**: Include comments explaining each configuration option
4. **Use Sensible Defaults**: Provide working defaults that require minimal changes
5. **Implement Migration**: If your plugin needs configuration migration, implement `ConfigurableMigrationPlugin`
6. **Validate Settings**: Use `validateConfiguration()` to check required settings during plugin initialization

```

### Configuration File Structure

Now handled automatically by the versioning system. Your `config.yml` should include the version and be well-documented:

```yaml
# MyPlugin Configuration 
# Configuration version (automatically managed)
config_version: 1

# Gameplay settings
gameplay:
  max_level: 100          # Maximum player level (1-1000)
  enable_pvp: false       # Allow player vs player combat
  respawn_time: 30        # Respawn delay in seconds

# Economy settings  
economy:
  currency: "coins"       # Name of the currency
  starting_balance: 1000  # Starting money for new players
  daily_bonus: 100        # Daily login bonus

# User interface messages
messages:
  welcome: "Welcome to the server!"
  goodbye: "Thanks for playing!"

# Advanced settings
advanced:
  debug_mode: false       # Enable debug logging
  cache_size: 1000        # Cache size for performance
  api_timeout: 30         # API timeout in seconds
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
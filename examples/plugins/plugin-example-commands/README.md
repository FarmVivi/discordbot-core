# Command Example Plugin

This plugin demonstrates comprehensive command system features in DiscordBot Core, including slash commands, permissions, cooldowns, argument handling, and embed responses.

## Features Demonstrated

### 🎯 Core Command Features
- **Slash Command Registration**: Multiple command types with different complexity levels
- **Command Arguments**: String options with validation and required parameters
- **Permission System**: Command-specific permissions with role-based access control
- **Cooldown Management**: Per-command and per-user cooldown systems
- **Error Handling**: Comprehensive error responses with user feedback
- **Internationalization**: Multi-language command descriptions and responses

### 📊 Advanced Features
- **Embed Responses**: Rich embed formatting with colors and fields
- **Command Statistics**: Execution counting and performance monitoring
- **Configuration-Driven**: All commands configurable via YAML settings
- **Debug Logging**: Detailed command usage logging and debugging
- **Uptime Tracking**: Plugin and bot uptime calculation

## Available Commands

### `/ping`
- **Description**: Simple ping-pong command to test bot responsiveness
- **Features**: Basic command execution, embed response, cooldown support
- **Permission**: `commandexample.use` (default: true)
- **Cooldown**: 3 seconds (configurable)

### `/echo <message>`
- **Description**: Echo back user input with validation
- **Features**: Required string argument, length validation, error handling
- **Permission**: `commandexample.use` (default: true)
- **Cooldown**: 5 seconds (configurable)
- **Arguments**: 
  - `message` (required): Text to echo back (max 200 characters)

### `/info`
- **Description**: Display bot and plugin information with rich embeds
- **Features**: Complex embed creation, uptime calculation, memory usage, statistics
- **Permission**: `commandexample.use` (default: true)
- **Cooldown**: None

### `/admin`
- **Description**: Administrative command demonstrating permission checks
- **Features**: Permission validation, admin-only access, audit logging
- **Permission**: `commandexample.admin` (default: OP only)
- **Cooldown**: None

## Configuration

### Enabling the Plugin

**Important**: This plugin is **disabled by default**. Enable it in `config.yml`:

```yaml
enabled: true  # Set to true to activate the plugin
```

### Command Configuration

```yaml
commands:
  ping:
    enabled: true
    description: "Simple ping-pong command"
    cooldown: 3
    
  echo:
    enabled: true
    max_length: 200  # Maximum message length
    cooldown: 5
    
  info:
    enabled: true
    show_detailed: false  # Show additional technical info
    
  admin:
    enabled: true
    permission_required: true
```

### Response Configuration

```yaml
responses:
  use_embeds: true          # Use rich embeds for responses
  embed_color: "#7289DA"    # Embed color (hex)
  show_execution_time: false # Include execution time in responses
```

### Cooldown Settings

```yaml
cooldowns:
  global_cooldown: 1        # Global cooldown between any commands
  per_user: true           # Track cooldowns per user
  bypass_permission: "commands.cooldown.bypass"
```

## Permissions

The plugin registers the following permissions:

| Permission | Description | Default |
|------------|-------------|---------|
| `commandexample.use` | Allows usage of basic commands | `TRUE` |
| `commandexample.admin` | Allows usage of admin commands | `OP` |
| `commandexample.cooldown.bypass` | Bypasses command cooldowns | `OP` |

### Setting Permissions

Use the bot's permission system to grant/revoke permissions:

```
/permissions user <user> set commandexample.admin true
/permissions role <role> set commandexample.use false
```

## Installation and Usage

### 1. Build the Plugin

```bash
cd examples/plugins/plugin-example-commands
mvn clean package
```

### 2. Install Plugin

```bash
cp target/plugin-example-commands-*.jar /path/to/bot/plugins/
```

### 3. Configure Plugin

Create or edit `plugins/CommandExample/config.yml`:

```yaml
enabled: true

commands:
  ping:
    enabled: true
  echo:
    enabled: true
  info:
    enabled: true
  admin:
    enabled: true
```

### 4. Restart Bot

Restart your DiscordBot Core instance to load the plugin.

### 5. Test Commands

Try the following commands in Discord:
- `/ping` - Test basic functionality
- `/echo Hello World` - Test argument handling
- `/info` - View plugin information
- `/admin` - Test permission system (admin only)

## Code Examples

### Basic Command Registration

```java
commandService.registerCommand(this, builder -> {
    builder.name("ping")
           .description("Check bot responsiveness")
           .executor(this::executePingCommand);
});
```

### Command with Arguments

```java
commandService.registerCommand(this, builder -> {
    builder.name("echo")
           .description("Echo user message")
           .addStringOption(option -> 
               option.name("message")
                     .description("Message to echo")
                     .required(true))
           .executor(this::executeEchoCommand);
});
```

### Permission Check

```java
if (!getPluginPermissionManager().hasPermission(
        context.getUser().getId(), "commandexample.admin")) {
    context.reply("No permission!");
    return CommandResult.error("No permission");
}
```

### Cooldown Check

```java
if (commandService.isOnCooldown(userId, "ping")) {
    int remaining = commandService.getRemainingCooldown(userId, "ping");
    context.reply("Wait " + remaining + " seconds");
    return CommandResult.error("On cooldown");
}
```

### Embed Response

```java
EmbedBuilder embed = new EmbedBuilder();
embed.setTitle("🏓 Ping");
embed.setDescription("Pong! Bot is responsive.");
embed.setColor(Color.GREEN);
embed.setTimestamp(Instant.now());

context.replyEmbeds(embed.build());
```

## Internationalization

The plugin supports multiple languages with namespace isolation:

### English (`lang/en-US.yml`)
```yaml
commands:
  ping:
    description: "Check if the bot is responsive"
    response: "🏓 Pong! Bot is online and responsive."
```

### French (`lang/fr-FR.yml`)
```yaml
commands:
  ping:
    description: "Vérifier si le bot répond"
    response: "🏓 Pong ! Le bot est en ligne et réactif."
```

### Usage in Code
```java
String response = getPluginLanguageManager().getString("ping.response");
String formatted = getPluginLanguageManager().getString("echo.response", message);
```

## Debugging

Enable debug logging in configuration:

```yaml
debug:
  log_command_usage: true      # Log each command execution
  log_execution_times: false   # Log command execution times
  detailed_errors: false       # Show detailed error messages
```

Debug output example:
```
[INFO] Ping command executed by: User#1234
[DEBUG] Command execution time: 15ms
[DEBUG] Command result: SUCCESS
```

## Common Issues

### Commands Not Appearing
- Ensure plugin is enabled: `enabled: true`
- Check individual command enablement in config
- Verify bot has permission to register slash commands
- Restart bot after configuration changes

### Permission Errors
- Check user has required permissions
- Verify permission nodes are correctly configured
- Use `/permissions` commands to debug permission issues

### Cooldown Issues
- Check cooldown configuration values
- Verify cooldown bypass permissions for admins
- Consider global vs per-command cooldowns

## Integration with Core Features

This example demonstrates integration with:

- **[Core Features Documentation](../../../docs/core-features.md#command-system)**: Command System
- **[Permission System](../plugin-example-permissions/)**: Advanced permission examples
- **[Configuration System](../plugin-example-config/)**: Configuration management
- **[Internationalization](../plugin-example-i18n/)**: Multi-language support

## Next Steps

After exploring this example:

1. **Study the code**: Review `CommandExamplePlugin.java` for implementation details
2. **Experiment**: Modify configuration and test different command behaviors
3. **Extend**: Add your own commands using the patterns demonstrated
4. **Learn more**: Explore other example plugins for additional features

---

*This example plugin is part of the DiscordBot Core documentation project and demonstrates production-ready command system usage.*
# Plugin Template

This template provides a starting point for creating DiscordBot Core plugins.

⚠️ **Note**: This template shows the basic plugin structure. For full functionality, you'll need to:

1. Add the `discordbot-core` dependency to your `pom.xml`
2. Extend `AbstractPlugin` instead of the basic structure shown
3. Use proper API annotations like `@EventHandler` and `@Command`

## Getting Started

1. **Copy this template**
   ```bash
   cp -r plugin-template my-awesome-plugin
   cd my-awesome-plugin
   ```

2. **Customize the plugin**
    - Edit `pom.xml`:
        - Change `<artifactId>` to your plugin name
        - Update `<name>` and `<description>`
        - Add `discordbot-core` dependency for full API access
    - Rename the package in `src/main/java/com/example/plugin/` to your own
    - Rename `TemplatePlugin.java` to your plugin class name
    - Update the class name and plugin details in the Java file

3. **Add DiscordBot Core dependency** (for full API access)
   ```xml
   <dependency>
       <groupId>fr.farmvivi.discordbot</groupId>
       <artifactId>discordbot-core</artifactId>
   </dependency>
   ```

4. **Extend AbstractPlugin**
   ```java
   public class MyPlugin extends AbstractPlugin {
       // Your implementation
   }
   ```

5. **Build and deploy**
   ```bash
   mvn clean package
   cp target/my-awesome-plugin-*.jar ../plugins/
   ```

6. **Restart the bot** to load your new plugin

## Full API Usage (with discordbot-core dependency)

### Commands

```java
@Command(name = "mycommand", description = "My awesome command")
public CommandResult myCommand(CommandContext ctx) {
    // Implementation
    return CommandResult.SUCCESS;
}
```

### Event Handlers

```java
@EventHandler
public void onMessageReceived(MessageReceivedEvent event) {
    // Handle Discord events
}
```

### Configuration

```java
// Get config values with defaults
String value = getConfiguration().getString("my.setting", "default");
getConfiguration().set("my.setting", "new_value");
```

### Storage

```java
// Persist data across restarts
getPluginDataStorage().set("key", "value");
String value = getPluginDataStorage().getString("key", "default");
```

### Permissions

```java
// Register permissions
getPluginPermissionManager().registerPermission("myplugin.use", PermissionDefault.TRUE);

// Check permissions
boolean hasPermission = getPluginPermissionManager().hasPermission(user, "myplugin.use");
```

## Plugin Structure

```
my-awesome-plugin/
├── pom.xml                           # Maven configuration
├── README.md                         # Plugin documentation
└── src/main/java/
    └── com/example/plugin/
        └── TemplatePlugin.java       # Main plugin class
```

## Available Examples

- **[Audio Example](../discordbot-example-audio/)**: Working audio plugin example
- **[Music Plugin](../plugins/music-plugin/)**: Advanced music bot (structure only)
- **[AI Audio Plugin](../plugins/ai-audio-plugin/)**: AI voice processing (structure only)

## Resources

- [Plugin Development Guide](../docs/plugin-development.md)
- [DiscordBot Core Documentation](../docs/)
- [JDA Documentation](https://jda.wiki/)
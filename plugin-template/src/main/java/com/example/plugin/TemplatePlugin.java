package com.example.plugin;

import fr.farmvivi.discordbot.api.command.CommandResult;
import fr.farmvivi.discordbot.api.command.option.OptionChoice;
import fr.farmvivi.discordbot.api.event.EventHandler;
import fr.farmvivi.discordbot.api.event.EventPriority;
import fr.farmvivi.discordbot.api.permissions.Permission;
import fr.farmvivi.discordbot.api.permissions.PermissionDefault;
import fr.farmvivi.discordbot.api.plugin.AbstractPlugin;
import fr.farmvivi.discordbot.api.storage.binary.BinaryStorageKey;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Template plugin demonstrating the complete structure and ALL features available to DiscordBot Core plugins.
 *
 * <p>This plugin serves as a comprehensive starting point for plugin development and showcases:
 * <ul>
 *   <li><strong>Plugin Lifecycle:</strong> onEnable/onDisable with proper resource management</li>
 *   <li><strong>Configuration System:</strong> YAML configuration with defaults and validation</li>
 *   <li><strong>Command System:</strong> Slash commands, options, permissions, cooldowns, subcommands</li>
 *   <li><strong>Event Handling:</strong> Discord events with different priorities</li>
 *   <li><strong>Internationalization:</strong> Multi-language support with namespaces</li>
 *   <li><strong>Storage System:</strong> User/Guild/Global data storage + Binary file storage</li>
 *   <li><strong>Permission Management:</strong> Plugin-specific permissions with defaults</li>
 *   <li><strong>Audio System:</strong> Send/receive audio handlers with volume control</li>
 *   <li><strong>Background Tasks:</strong> Scheduled executors for periodic operations</li>
 * </ul>
 *
 * <p>To create your own plugin based on this template:
 * <ol>
 *   <li>Copy this template directory: {@code cp -r plugin-template my-awesome-plugin}</li>
 *   <li>Rename the package and class to match your plugin</li>
 *   <li>Update the plugin.yml with your plugin details</li>
 *   <li>Customize the configuration and language files</li>
 *   <li>Remove/modify the example features you don't need</li>
 *   <li>Build with: {@code mvn clean package}</li>
 *   <li>Copy the JAR to the plugins/ directory</li>
 * </ol>
 *
 * <p><strong>Key Files Structure:</strong>
 * <pre>
 * my-awesome-plugin/
 * ├── pom.xml                           # Maven configuration
 * ├── README.md                         # Plugin documentation
 * └── src/main/
 *     ├── java/com/example/plugin/
 *     │   └── TemplatePlugin.java       # Main plugin class (this file)
 *     └── resources/
 *         ├── plugin.yml                # Plugin metadata
 *         ├── config.yml                # Default configuration
 *         └── lang/                     # Language files
 *             ├── en-US.yml             # English translations
 *             └── fr-FR.yml             # French translations
 * </pre>
 *
 * <p><strong>Note:</strong> Plugin metadata (name, version, etc.) is automatically loaded from
 * the plugin.yml file, so you don't need to implement getName() and getVersion() methods.
 *
 * @author YourName
 * @version 1.0.0
 * @see fr.farmvivi.discordbot.api.plugin.AbstractPlugin
 * @since 1.0.0
 */
public class TemplatePlugin extends AbstractPlugin {

    // Background task scheduler
    private ScheduledExecutorService scheduler;
    
    // Audio handlers (for audio feature demonstration)
    private ExampleAudioSendHandler audioSendHandler;
    private ExampleAudioReceiveHandler audioReceiveHandler;

    /**
     * Called when the plugin is enabled.
     * This is where you should initialize your plugin, load configuration,
     * register permissions, and set up all features.
     */
    @Override
    public void onEnable() {
        // Initialize background scheduler
        scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // 1. Register permissions (must be done before commands)
        registerPermissions();

        // 2. Load and validate configuration settings
        loadConfiguration();

        // 3. Register all commands
        registerCommands();

        // 4. Initialize services and features
        initializeFeatures();

        // 5. Start background tasks
        startBackgroundTasks();

        logger.info(getLocalizedMessage("messages.plugin_enabled", "Template Plugin enabled"));
    }

    /**
     * Called when the plugin is disabled.
     * This is where you should save data, clean up resources, and perform shutdown tasks.
     */
    @Override
    public void onDisable() {
        // 1. Stop background tasks
        stopBackgroundTasks();
        
        // 2. Clean up audio handlers
        cleanupAudioHandlers();
        
        // 3. Save all data
        savePluginData();
        
        logger.info(getLocalizedMessage("messages.plugin_disabled", "Template Plugin disabled"));
    }

    // ===== CONFIGURATION MANAGEMENT =====

    /**
     * Loads configuration values with defaults and validation.
     * This demonstrates how to access plugin configuration properly.
     */
    private void loadConfiguration() {
        // Load configuration with defaults
        boolean featuresEnabled = getConfiguration().getBoolean("features.enabled", true);
        String welcomeMessage = getConfiguration().getString("messages.welcome", "Welcome {user}!");
        int maxUsers = getConfiguration().getInt("limits.max_users", 100);
        List<String> allowedChannels = getConfiguration().getStringList("channels.allowed", Arrays.asList());
        
        // Validate configuration
        if (maxUsers < 1 || maxUsers > 1000) {
            logger.warn("Invalid max_users value: {}. Using default: 100", maxUsers);
            getConfiguration().set("limits.max_users", 100);
        }
        
        // Save defaults if they don't exist
        getConfiguration().set("features.enabled", featuresEnabled);
        getConfiguration().set("messages.welcome", welcomeMessage);
        getConfiguration().set("limits.max_users", maxUsers);
        getConfiguration().set("channels.allowed", allowedChannels);
        
        logger.info("Configuration loaded - Features: {}, Max Users: {}, Channels: {}", 
                   featuresEnabled, maxUsers, allowedChannels.size());
    }

    // ===== PERMISSION MANAGEMENT =====

    /**
     * Registers plugin-specific permissions.
     * This demonstrates how to register permissions that can be used
     * to control access to plugin features.
     */
    private void registerPermissions() {
        // Basic user permission
        getPluginPermissionManager().registerPermission(new SimplePermission(
                pluginPrefix("use"),
                getLocalizedMessage("permissions.use.description", "Allows usage of basic template features"),
                PermissionDefault.TRUE));
        
        // Admin permission
        getPluginPermissionManager().registerPermission(new SimplePermission(
                pluginPrefix("admin"),
                getLocalizedMessage("permissions.admin.description", "Allows administrative template actions"),
                PermissionDefault.OP));
        
        // Advanced features permission
        getPluginPermissionManager().registerPermission(new SimplePermission(
                pluginPrefix("advanced"),
                getLocalizedMessage("permissions.advanced.description", "Allows access to advanced features"),
                PermissionDefault.FALSE));
        
        logger.debug("Permissions registered: {}", getPluginPermissionManager().getRegisteredPermissions());
    }

    // ===== COMMAND SYSTEM =====

    /**
     * Registers all plugin commands demonstrating various command features.
     */
    private void registerCommands() {
        registerBasicCommands();
        registerAdvancedCommands();
        registerDataCommands();
        registerAudioCommands();
    }

    /**
     * Register basic commands (info, help, config)
     */
    private void registerBasicCommands() {
        // Plugin info command
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("templateinfo")
                .description(getLocalizedMessage("commands.info.description", "Show plugin information"))
                .permission(pluginPrefix("use"))
                .execute(context -> {
                    String info = String.format(
                        "🤖 **%s v%s**\n" +
                        "📊 %s\n" +
                        "👥 Active Users: %d\n" +
                        "💾 Data Storage: %s\n" +
                        "🌐 Language: %s",
                        getName(), getVersion(),
                        getLocalizedMessage("commands.info.status", "Status: Active"),
                        getPluginDataStorage().getGlobalStorage().get("active_users", Integer.class).orElse(0),
                        getConfiguration().getBoolean("features.data_storage", true) ? "Enabled" : "Disabled",
                        getPluginLanguageManager().getCurrentLanguage()
                    );
                    context.reply(info);
                    return CommandResult.SUCCESS;
                })
        );

        // Configuration management command
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("templateconfig")
                .description(getLocalizedMessage("commands.config.description", "Manage plugin configuration"))
                .permission(pluginPrefix("admin"))
                .subcommand("reload", getLocalizedMessage("commands.config.reload.description", "Reload configuration"))
                    .execute(context -> {
                        loadConfiguration();
                        context.reply(getLocalizedMessage("commands.config.reloaded", "✅ Configuration reloaded!"));
                        return CommandResult.SUCCESS;
                    })
                .parent()
                .subcommand("set", getLocalizedMessage("commands.config.set.description", "Set configuration value"))
                    .stringOption("key", "Configuration key", true)
                    .stringOption("value", "New value", true)
                    .execute(context -> {
                        String key = context.getStringOption("key");
                        String value = context.getStringOption("value");
                        
                        getConfiguration().set(key, value);
                        context.reply(String.format("✅ Set `%s` = `%s`", key, value));
                        return CommandResult.SUCCESS;
                    })
        );
    }

    /**
     * Register advanced commands with complex options
     */
    private void registerAdvancedCommands() {
        // Multi-option demonstration command
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("templatedemo")
                .description(getLocalizedMessage("commands.demo.description", "Demonstrate various option types"))
                .permission(pluginPrefix("use"))
                .stringOption("text", "Enter some text", true)
                .integerOption("number", "Enter a number", false)
                .booleanOption("flag", "Enable feature", false)
                .userOption("user", "Select a user", false)
                .channelOption("channel", "Select a channel", false)
                .stringOption("choice", "Pick an option", false)
                    .choices(
                        OptionChoice.of("Option A", "a"),
                        OptionChoice.of("Option B", "b"),
                        OptionChoice.of("Option C", "c")
                    )
                .execute(context -> {
                    StringBuilder response = new StringBuilder("🎯 **Demo Results:**\n");
                    
                    response.append("📝 Text: ").append(context.getStringOption("text")).append("\n");
                    
                    context.getIntegerOption("number").ifPresent(num -> 
                        response.append("🔢 Number: ").append(num).append("\n"));
                    
                    context.getBooleanOption("flag").ifPresent(flag -> 
                        response.append("🚩 Flag: ").append(flag ? "Enabled" : "Disabled").append("\n"));
                    
                    context.getUserOption("user").ifPresent(user -> 
                        response.append("👤 User: ").append(user.getAsMention()).append("\n"));
                    
                    context.getChannelOption("channel").ifPresent(channel -> 
                        response.append("📺 Channel: ").append(channel.getAsMention()).append("\n"));
                    
                    context.getStringOption("choice").ifPresent(choice -> 
                        response.append("✅ Choice: ").append(choice).append("\n"));
                    
                    context.reply(response.toString());
                    
                    // Store command usage statistics
                    getPluginDataStorage().getGlobalStorage().increment("demo_command_uses", 1);
                    
                    return CommandResult.SUCCESS;
                })
        );

        // Autocomplete command
        List<String> categories = Arrays.asList("Technology", "Gaming", "Music", "Art", "Science", "Sports");
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("templatecategory")
                .description(getLocalizedMessage("commands.category.description", "Select a category"))
                .permission(pluginPrefix("use"))
                .stringOption("category", "Choose a category", true,
                    input -> categories.stream()
                        .filter(cat -> cat.toLowerCase().contains(input.toLowerCase()))
                        .limit(10)
                        .map(cat -> OptionChoice.of(cat, cat.toLowerCase()))
                        .collect(Collectors.toList())
                )
                .execute(context -> {
                    String category = context.getStringOption("category");
                    context.reply(String.format("🎯 You selected category: **%s**", category));
                    
                    // Store user preference
                    String userId = context.getUser().getId();
                    getPluginDataStorage().getUserStorage(userId).set("preferred_category", category);
                    
                    return CommandResult.SUCCESS;
                })
        );
    }

    /**
     * Register data storage demonstration commands
     */
    private void registerDataCommands() {
        // User profile management
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("templateprofile")
                .description(getLocalizedMessage("commands.profile.description", "Manage your profile"))
                .permission(pluginPrefix("use"))
                .subcommand("view", "View your profile")
                    .execute(context -> {
                        String userId = context.getUser().getId();
                        var userStorage = getPluginDataStorage().getUserStorage(userId);
                        
                        String nickname = userStorage.get("nickname", String.class)
                            .orElse(context.getUser().getEffectiveName());
                        int points = userStorage.get("points", Integer.class).orElse(0);
                        String joinDate = userStorage.get("first_seen", String.class).orElse("Unknown");
                        
                        String profile = String.format(
                            "👤 **%s's Profile**\n" +
                            "⭐ Points: %d\n" +
                            "📅 First seen: %s\n" +
                            "🎯 Category: %s",
                            nickname, points, joinDate,
                            userStorage.get("preferred_category", String.class).orElse("None")
                        );
                        
                        context.reply(profile);
                        return CommandResult.SUCCESS;
                    })
                .parent()
                .subcommand("nickname", "Set your nickname")
                    .stringOption("name", "Your new nickname", true)
                    .execute(context -> {
                        String userId = context.getUser().getId();
                        String nickname = context.getStringOption("name");
                        
                        getPluginDataStorage().getUserStorage(userId).set("nickname", nickname);
                        context.reply("✅ Nickname updated to: **" + nickname + "**");
                        return CommandResult.SUCCESS;
                    })
        );

        // File storage demonstration
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("templatefile")
                .description(getLocalizedMessage("commands.file.description", "File storage operations"))
                .permission(pluginPrefix("use"))
                .subcommand("save", "Save text to file")
                    .stringOption("filename", "File name", true)
                    .stringOption("content", "File content", true)
                    .execute(context -> {
                        String filename = context.getStringOption("filename");
                        String content = context.getStringOption("content");
                        
                        try {
                            BinaryStorageKey key = new BinaryStorageKey("user_files", 
                                context.getUser().getId() + "_" + filename);
                            
                            getPluginBinaryStorage().storeFile(key, 
                                new ByteArrayInputStream(content.getBytes()));
                            
                            context.reply("💾 File saved: **" + filename + "**");
                            return CommandResult.SUCCESS;
                            
                        } catch (IOException e) {
                            context.reply("❌ Failed to save file: " + e.getMessage());
                            return CommandResult.ERROR;
                        }
                    })
                .parent()
                .subcommand("list", "List your saved files")
                    .execute(context -> {
                        try {
                            BinaryStorageKey key = new BinaryStorageKey("user_files", "");
                            List<String> files = getPluginBinaryStorage().listFiles(key);
                            
                            String userPrefix = context.getUser().getId() + "_";
                            List<String> userFiles = files.stream()
                                .filter(f -> f.startsWith(userPrefix))
                                .map(f -> f.substring(userPrefix.length()))
                                .collect(Collectors.toList());
                            
                            if (userFiles.isEmpty()) {
                                context.reply("📁 No files found.");
                            } else {
                                context.reply("📁 **Your files:**\n" + 
                                             String.join(", ", userFiles));
                            }
                            return CommandResult.SUCCESS;
                            
                        } catch (Exception e) {
                            context.reply("❌ Failed to list files: " + e.getMessage());
                            return CommandResult.ERROR;
                        }
                    })
        );
    }

    /**
     * Register audio system commands (demonstration)
     */
    private void registerAudioCommands() {
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("templateaudio")
                .description(getLocalizedMessage("commands.audio.description", "Audio system demonstration"))
                .permission(pluginPrefix("advanced"))
                .guildOnly(true)
                .subcommand("start", "Start audio demo")
                    .execute(context -> {
                        if (context.getGuild() == null) {
                            context.reply("❌ This command requires a server!");
                            return CommandResult.ERROR;
                        }
                        
                        // Register audio handlers (demonstration)
                        if (audioSendHandler == null) {
                            audioSendHandler = new ExampleAudioSendHandler();
                            audioService.registerSendHandler(context.getGuild(), this, audioSendHandler, 50, 60);
                        }
                        
                        if (audioReceiveHandler == null) {
                            audioReceiveHandler = new ExampleAudioReceiveHandler();
                            audioService.registerReceiveHandler(context.getGuild(), this, audioReceiveHandler);
                        }
                        
                        context.reply("🎵 Audio demo started!");
                        return CommandResult.SUCCESS;
                    })
                .parent()
                .subcommand("stop", "Stop audio demo")
                    .execute(context -> {
                        if (context.getGuild() == null) {
                            context.reply("❌ This command requires a server!");
                            return CommandResult.ERROR;
                        }
                        
                        cleanupAudioHandlers();
                        context.reply("🔇 Audio demo stopped!");
                        return CommandResult.SUCCESS;
                    })
        );
    }

    // ===== EVENT HANDLING =====

    /**
     * Example event handler that demonstrates different priority levels.
     * This method will be called whenever a message is received.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onMessageHighPriority(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        
        // High priority processing (anti-spam, security checks, etc.)
        String content = event.getMessage().getContentRaw();
        if (content.toLowerCase().contains("spam")) {
            logger.debug("Potential spam detected from {}", event.getAuthor().getEffectiveName());
            // Could cancel event here if implementing Cancellable
        }
    }

    /**
     * Normal priority message handler for main plugin functionality.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMessage(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String userId = event.getAuthor().getId();
        String content = event.getMessage().getContentRaw().toLowerCase();
        
        // Update user statistics
        var userStorage = getPluginDataStorage().getUserStorage(userId);
        
        // Set first seen date
        if (!userStorage.has("first_seen")) {
            userStorage.set("first_seen", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        // Increment message count and points
        userStorage.increment("message_count", 1);
        userStorage.increment("points", 1);
        userStorage.set("last_activity", System.currentTimeMillis());
        
        // Auto-react to keywords
        if (content.contains("template") || content.contains(getName().toLowerCase())) {
            event.getMessage().addReaction(Emoji.fromUnicode("🤖")).queue();
        }
        
        // Update global statistics
        if (event.isFromGuild()) {
            var guildStorage = getPluginDataStorage().getGuildStorage(event.getGuild().getId());
            guildStorage.increment("total_messages", 1);
            guildStorage.set("last_activity", System.currentTimeMillis());
        }
        
        // Log activity (debug level)
        logger.debug("Message from {}: {}", event.getAuthor().getEffectiveName(), 
                    content.length() > 50 ? content.substring(0, 50) + "..." : content);
    }

    /**
     * Welcome message handler for new members.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMemberJoin(GuildMemberJoinEvent event) {
        if (!getConfiguration().getBoolean("features.welcome_messages", true)) return;
        
        String welcomeMessage = getConfiguration().getString("messages.welcome", "Welcome {user}!")
            .replace("{user}", event.getMember().getAsMention())
            .replace("{server}", event.getGuild().getName());
        
        // Send welcome message to system channel
        if (event.getGuild().getSystemChannel() != null) {
            event.getGuild().getSystemChannel().sendMessage(welcomeMessage).queue();
        }
        
        // Initialize user data
        String userId = event.getUser().getId();
        var userStorage = getPluginDataStorage().getUserStorage(userId);
        userStorage.set("join_date", LocalDateTime.now().toString());
        userStorage.set("points", 0);
        
        logger.info("New member joined: {} in {}", 
                   event.getMember().getEffectiveName(), event.getGuild().getName());
    }

    // ===== BACKGROUND TASKS =====

    /**
     * Initializes plugin features and services.
     */
    private void initializeFeatures() {
        // Initialize global statistics if not exist
        var globalStorage = getPluginDataStorage().getGlobalStorage();
        if (!globalStorage.has("plugin_start_time")) {
            globalStorage.set("plugin_start_time", System.currentTimeMillis());
        }
        if (!globalStorage.has("active_users")) {
            globalStorage.set("active_users", 0);
        }
        
        logger.debug("Plugin features initialized");
    }

    /**
     * Starts background tasks for maintenance and periodic operations.
     */
    private void startBackgroundTasks() {
        // Data cleanup task every 30 minutes
        scheduler.scheduleAtFixedRate(() -> {
            try {
                performDataCleanup();
            } catch (Exception e) {
                logger.error("Error in data cleanup task: {}", e.getMessage());
            }
        }, 30, 30, TimeUnit.MINUTES);
        
        // Statistics update task every 5 minutes
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateStatistics();
            } catch (Exception e) {
                logger.error("Error in statistics update task: {}", e.getMessage());
            }
        }, 5, 5, TimeUnit.MINUTES);
        
        logger.debug("Background tasks started");
    }

    /**
     * Stops all background tasks.
     */
    private void stopBackgroundTasks() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        logger.warn("Background tasks did not terminate gracefully");
                    }
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // ===== DATA MANAGEMENT =====

    /**
     * Performs periodic data cleanup to remove old or inactive data.
     */
    private void performDataCleanup() {
        long now = System.currentTimeMillis();
        long inactiveThreshold = TimeUnit.DAYS.toMillis(30); // 30 days
        
        // This is a simplified example - in practice you'd iterate through storage keys
        logger.debug("Performing data cleanup...");
        
        // Update global statistics
        var globalStorage = getPluginDataStorage().getGlobalStorage();
        globalStorage.set("last_cleanup", now);
        
        logger.debug("Data cleanup completed");
    }

    /**
     * Updates plugin statistics.
     */
    private void updateStatistics() {
        var globalStorage = getPluginDataStorage().getGlobalStorage();
        
        // Calculate uptime
        long startTime = globalStorage.get("plugin_start_time", Long.class).orElse(System.currentTimeMillis());
        long uptime = System.currentTimeMillis() - startTime;
        globalStorage.set("uptime_milliseconds", uptime);
        
        // Save statistics
        getPluginDataStorage().saveAll();
        
        logger.debug("Statistics updated - Uptime: {} hours", uptime / (1000 * 60 * 60));
    }

    /**
     * Saves plugin data to persistent storage.
     * This demonstrates how to use the storage API for data persistence.
     */
    private void savePluginData() {
        try {
            // Save shutdown timestamp
            getPluginDataStorage().getGlobalStorage().set("last_shutdown", System.currentTimeMillis());
            
            // Save all pending data
            getPluginDataStorage().saveAll();
            
            logger.debug("Plugin data saved successfully");
        } catch (Exception e) {
            logger.error("Failed to save plugin data: {}", e.getMessage());
        }
    }

    // ===== AUDIO SYSTEM (DEMONSTRATION) =====

    /**
     * Cleanup audio handlers.
     */
    private void cleanupAudioHandlers() {
        // Audio cleanup would happen here
        audioSendHandler = null;
        audioReceiveHandler = null;
        logger.debug("Audio handlers cleaned up");
    }

    // ===== UTILITY METHODS =====

    /**
     * Gets a localized message with fallback.
     */
    private String getLocalizedMessage(String key, String defaultValue) {
        return getPluginLanguageManager().getString(key, defaultValue);
    }

    /**
     * Creates a permission name with plugin prefix.
     */
    private String pluginPrefix(String node) {
        return getName().toLowerCase() + "." + node;
    }

    // ===== INNER CLASSES =====

    /**
     * Simple internal Permission implementation for template usage.
     */
    private record SimplePermission(String name, String description,
                                    PermissionDefault getDefault) implements Permission {
        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    /**
     * Example audio send handler (demonstration only).
     */
    private static class ExampleAudioSendHandler implements AudioSendHandler {
        @Override
        public boolean canProvide() {
            return false; // No actual audio for demo
        }

        @Override
        public ByteBuffer provide20MsAudio() {
            return null;
        }

        @Override
        public boolean isOpus() {
            return false;
        }
    }

    /**
     * Example audio receive handler (demonstration only).
     */
    private static class ExampleAudioReceiveHandler implements AudioReceiveHandler {
        @Override
        public boolean canReceiveCombined() {
            return true;
        }

        @Override
        public boolean canReceiveUser() {
            return false;
        }

        @Override
        public void handleCombinedAudio(CombinedAudio combinedAudio) {
            // Audio processing would happen here
        }
    }
}
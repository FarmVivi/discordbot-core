package fr.farmvivi.discordbot.examples.storage;

import fr.farmvivi.discordbot.api.command.CommandResult;
import fr.farmvivi.discordbot.api.event.EventHandler;
import fr.farmvivi.discordbot.api.event.EventPriority;
import fr.farmvivi.discordbot.api.plugin.AbstractPlugin;
import fr.farmvivi.discordbot.api.storage.binary.BinaryStorageKey;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Example plugin demonstrating comprehensive storage system usage.
 * Shows data storage, binary storage, user/guild/global storage patterns.
 */
public class StorageExamplePlugin extends AbstractPlugin {
    
    private ScheduledExecutorService scheduler;
    private final Map<String, Integer> temporaryCache = new HashMap<>();

    @Override
    public void onEnable() {
        registerCommands();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // Start data backup task
        startDataBackupTask();
        
        // Initialize plugin statistics
        initializeStatistics();
        
        logger.info("Storage Example Plugin enabled with comprehensive storage demonstrations!");
    }

    @Override
    public void onDisable() {
        // Save all data before shutdown
        saveAllData();
        
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("Storage Example Plugin disabled!");
    }

    private void registerCommands() {
        // User profile command
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("profile")
                .description("Manage your user profile")
                .subcommand("view", "View your profile")
                    .execute(context -> {
                        String userId = context.getUser().getId();
                        var userStorage = getPluginDataStorage().getUserStorage(userId);
                        
                        String name = userStorage.get("display_name", String.class).orElse("Unknown");
                        int level = userStorage.get("level", Integer.class).orElse(1);
                        int xp = userStorage.get("experience", Integer.class).orElse(0);
                        String joinDate = userStorage.get("first_seen", String.class).orElse("Unknown");
                        int messageCount = userStorage.get("message_count", Integer.class).orElse(0);
                        
                        String profile = String.format(
                            "👤 **%s's Profile**\n" +
                            "🏆 Level: %d\n" +
                            "⭐ Experience: %d XP\n" +
                            "📅 First seen: %s\n" +
                            "💬 Messages: %d",
                            name, level, xp, joinDate, messageCount
                        );
                        
                        context.reply(profile);
                        return CommandResult.SUCCESS;
                    })
                .parent()
                .subcommand("set", "Set profile information")
                    .stringOption("name", "Your display name", true)
                    .execute(context -> {
                        String userId = context.getUser().getId();
                        String newName = context.getStringOption("name");
                        
                        var userStorage = getPluginDataStorage().getUserStorage(userId);
                        userStorage.set("display_name", newName);
                        userStorage.set("last_updated", LocalDateTime.now().toString());
                        
                        context.reply("✅ Profile updated! Display name set to: **" + newName + "**");
                        return CommandResult.SUCCESS;
                    })
        );

        // Server statistics command
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("serverstats")
                .description("View server statistics")
                .guildOnly(true)
                .execute(context -> {
                    String guildId = context.getGuild().getId();
                    var guildStorage = getPluginDataStorage().getGuildStorage(guildId);
                    
                    int totalMessages = guildStorage.get("total_messages", Integer.class).orElse(0);
                    int activeUsers = guildStorage.get("active_users", Integer.class).orElse(0);
                    String lastActive = guildStorage.get("last_activity", String.class).orElse("Never");
                    int eventsLogged = guildStorage.get("events_logged", Integer.class).orElse(0);
                    
                    String stats = String.format(
                        "📊 **%s Statistics**\n" +
                        "💬 Total Messages: %d\n" +
                        "👥 Active Users: %d\n" +
                        "🕐 Last Activity: %s\n" +
                        "📋 Events Logged: %d",
                        context.getGuild().getName(), totalMessages, activeUsers, lastActive, eventsLogged
                    );
                    
                    context.reply(stats);
                    return CommandResult.SUCCESS;
                })
        );

        // Global statistics command
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("globalstats")
                .description("View global bot statistics")
                .execute(context -> {
                    var globalStorage = getPluginDataStorage().getGlobalStorage();
                    
                    int totalServers = globalStorage.get("total_servers", Integer.class).orElse(0);
                    int totalUsers = globalStorage.get("total_users", Integer.class).orElse(0);
                    long uptime = globalStorage.get("uptime_seconds", Long.class).orElse(0L);
                    String version = globalStorage.get("plugin_version", String.class).orElse("Unknown");
                    
                    String stats = String.format(
                        "🌐 **Global Statistics**\n" +
                        "🏰 Total Servers: %d\n" +
                        "👥 Total Users: %d\n" +
                        "⏱️ Uptime: %d hours\n" +
                        "🔖 Version: %s",
                        totalServers, totalUsers, uptime / 3600, version
                    );
                    
                    context.reply(stats);
                    return CommandResult.SUCCESS;
                })
        );

        // Binary storage demo command
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("filesave")
                .description("Save text to binary storage")
                .stringOption("filename", "Name of the file", true)
                .stringOption("content", "Content to save", true)
                .execute(context -> {
                    String filename = context.getStringOption("filename");
                    String content = context.getStringOption("content");
                    
                    try {
                        BinaryStorageKey key = new BinaryStorageKey("user_files", filename);
                        byte[] data = content.getBytes();
                        
                        getPluginBinaryStorage().storeFile(key, new ByteArrayInputStream(data));
                        
                        // Store metadata
                        var userStorage = getPluginDataStorage().getUserStorage(context.getUser().getId());
                        userStorage.set("last_file_saved", filename);
                        userStorage.set("last_save_time", LocalDateTime.now().toString());
                        
                        context.reply("💾 File **" + filename + "** saved successfully!");
                        return CommandResult.SUCCESS;
                        
                    } catch (IOException e) {
                        logger.error("Failed to save file: {}", e.getMessage());
                        context.reply("❌ Failed to save file: " + e.getMessage());
                        return CommandResult.ERROR;
                    }
                })
        );

        // List files command
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("filelist")
                .description("List saved files")
                .execute(context -> {
                    try {
                        BinaryStorageKey key = new BinaryStorageKey("user_files", "");
                        List<String> files = getPluginBinaryStorage().listFiles(key);
                        
                        if (files.isEmpty()) {
                            context.reply("📁 No files found in storage.");
                            return CommandResult.SUCCESS;
                        }
                        
                        StringBuilder fileList = new StringBuilder("📁 **Saved Files:**\n");
                        for (String file : files) {
                            long size = getPluginBinaryStorage().getFileSize(new BinaryStorageKey("user_files", file));
                            fileList.append("📄 ").append(file).append(" (").append(size).append(" bytes)\n");
                        }
                        
                        context.reply(fileList.toString());
                        return CommandResult.SUCCESS;
                        
                    } catch (Exception e) {
                        logger.error("Failed to list files: {}", e.getMessage());
                        context.reply("❌ Failed to list files: " + e.getMessage());
                        return CommandResult.ERROR;
                    }
                })
        );

        // Leaderboard command
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("leaderboard")
                .description("View message leaderboard")
                .guildOnly(true)
                .execute(context -> {
                    // This is a simplified example - in reality you'd want proper pagination
                    String guildId = context.getGuild().getId();
                    var guildStorage = getPluginDataStorage().getGuildStorage(guildId);
                    
                    // Get top users (simplified - would need proper data structure)
                    Map<String, Integer> topUsers = guildStorage.get("top_users", Map.class)
                        .orElse(new HashMap<>());
                    
                    if (topUsers.isEmpty()) {
                        context.reply("📈 No leaderboard data available yet!");
                        return CommandResult.SUCCESS;
                    }
                    
                    StringBuilder leaderboard = new StringBuilder("🏆 **Message Leaderboard**\n");
                    topUsers.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .limit(10)
                        .forEach(entry -> {
                            String userId = entry.getKey();
                            int count = entry.getValue();
                            leaderboard.append(String.format("👤 <@%s>: %d messages\n", userId, count));
                        });
                    
                    context.reply(leaderboard.toString());
                    return CommandResult.SUCCESS;
                })
        );
    }

    /**
     * Handle messages for activity tracking
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMessage(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        
        String userId = event.getAuthor().getId();
        String guildId = event.isFromGuild() ? event.getGuild().getId() : null;
        
        // Update user statistics
        updateUserActivity(userId);
        
        // Update guild statistics
        if (guildId != null) {
            updateGuildActivity(guildId, userId);
        }
        
        // Update global statistics
        updateGlobalActivity();
        
        // Track user experience
        updateUserExperience(userId);
    }

    private void updateUserActivity(String userId) {
        var userStorage = getPluginDataStorage().getUserStorage(userId);
        
        // Set first seen if not set
        if (!userStorage.has("first_seen")) {
            userStorage.set("first_seen", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        // Increment message count
        userStorage.increment("message_count", 1);
        
        // Update last activity
        userStorage.set("last_activity", LocalDateTime.now().toString());
        
        // Update activity streak
        String today = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String lastActiveDate = userStorage.get("last_active_date", String.class).orElse("");
        
        if (!today.equals(lastActiveDate)) {
            userStorage.set("last_active_date", today);
            if (isConsecutiveDay(lastActiveDate, today)) {
                userStorage.increment("activity_streak", 1);
            } else {
                userStorage.set("activity_streak", 1);
            }
        }
    }

    private void updateGuildActivity(String guildId, String userId) {
        var guildStorage = getPluginDataStorage().getGuildStorage(guildId);
        
        // Increment total messages
        guildStorage.increment("total_messages", 1);
        
        // Update last activity
        guildStorage.set("last_activity", LocalDateTime.now().toString());
        
        // Track active users (simplified)
        Map<String, Integer> topUsers = guildStorage.get("top_users", Map.class)
            .orElse(new HashMap<>());
        topUsers.merge(userId, 1, Integer::sum);
        guildStorage.set("top_users", topUsers);
        
        // Update active user count
        guildStorage.set("active_users", topUsers.size());
    }

    private void updateGlobalActivity() {
        var globalStorage = getPluginDataStorage().getGlobalStorage();
        
        // Increment total message count
        globalStorage.increment("total_messages_global", 1);
        
        // Update last global activity
        globalStorage.set("last_global_activity", LocalDateTime.now().toString());
    }

    private void updateUserExperience(String userId) {
        var userStorage = getPluginDataStorage().getUserStorage(userId);
        
        // Add experience points
        int currentXP = userStorage.get("experience", Integer.class).orElse(0);
        int newXP = currentXP + 1; // 1 XP per message
        userStorage.set("experience", newXP);
        
        // Calculate level (100 XP per level)
        int newLevel = (newXP / 100) + 1;
        int currentLevel = userStorage.get("level", Integer.class).orElse(1);
        
        if (newLevel > currentLevel) {
            userStorage.set("level", newLevel);
            // Could trigger level up event here
        }
    }

    private void initializeStatistics() {
        var globalStorage = getPluginDataStorage().getGlobalStorage();
        
        // Initialize plugin version
        globalStorage.set("plugin_version", getVersion());
        
        // Initialize start time for uptime calculation
        if (!globalStorage.has("start_time")) {
            globalStorage.set("start_time", System.currentTimeMillis());
        }
        
        // Initialize counters if not exist
        if (!globalStorage.has("total_servers")) {
            globalStorage.set("total_servers", 0);
        }
        if (!globalStorage.has("total_users")) {
            globalStorage.set("total_users", 0);
        }
    }

    private void startDataBackupTask() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Save all data
                getPluginDataStorage().saveAll();
                
                // Update uptime
                var globalStorage = getPluginDataStorage().getGlobalStorage();
                long startTime = globalStorage.get("start_time", Long.class).orElse(System.currentTimeMillis());
                long uptime = (System.currentTimeMillis() - startTime) / 1000;
                globalStorage.set("uptime_seconds", uptime);
                
                logger.debug("Data backup completed");
                
            } catch (Exception e) {
                logger.error("Failed to backup data: {}", e.getMessage());
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    private void saveAllData() {
        try {
            getPluginDataStorage().saveAll();
            logger.info("All data saved successfully");
        } catch (Exception e) {
            logger.error("Failed to save data during shutdown: {}", e.getMessage());
        }
    }

    private boolean isConsecutiveDay(String lastDate, String currentDate) {
        // Simplified - should use proper date parsing
        try {
            if (lastDate.isEmpty()) return false;
            // Would implement proper consecutive day checking here
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
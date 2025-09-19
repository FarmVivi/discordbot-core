package fr.farmvivi.discordbot.examples.events;

import fr.farmvivi.discordbot.api.event.EventHandler;
import fr.farmvivi.discordbot.api.event.EventPriority;
import fr.farmvivi.discordbot.api.plugin.AbstractPlugin;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Example plugin demonstrating comprehensive event system usage.
 * Shows Discord events, custom events, priorities, and event handling patterns.
 */
public class EventExamplePlugin extends AbstractPlugin {
    
    private final Map<String, Integer> messageCount = new HashMap<>();
    private final Map<String, Long> lastActivity = new HashMap<>();
    private ScheduledExecutorService scheduler;
    
    private boolean welcomeEnabled;
    private boolean logEvents;
    private boolean autoRoles;
    private String welcomeMessage;
    private String leaveMessage;

    @Override
    public void onEnable() {
        loadConfiguration();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // Start activity monitoring task
        startActivityMonitoring();
        
        logger.info("Event Example Plugin enabled with comprehensive event handling!");
    }

    @Override
    public void onDisable() {
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
        logger.info("Event Example Plugin disabled!");
    }

    private void loadConfiguration() {
        welcomeEnabled = getConfiguration().getBoolean("features.welcome_messages", true);
        logEvents = getConfiguration().getBoolean("features.event_logging", true);
        autoRoles = getConfiguration().getBoolean("features.auto_roles", false);
        welcomeMessage = getConfiguration().getString("messages.welcome", "Welcome {user} to {server}!");
        leaveMessage = getConfiguration().getString("messages.leave", "{user} has left {server}.");
        
        logger.info("Configuration loaded - Welcome: {}, Logging: {}, AutoRoles: {}", 
                   welcomeEnabled, logEvents, autoRoles);
    }

    /**
     * HIGH PRIORITY: Member join event (runs before other plugins)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onMemberJoinHigh(GuildMemberJoinEvent event) {
        if (!welcomeEnabled) return;
        
        Member member = event.getMember();
        String guildName = event.getGuild().getName();
        
        // Send welcome message to system channel
        TextChannel systemChannel = event.getGuild().getSystemChannel();
        if (systemChannel != null) {
            String message = welcomeMessage
                .replace("{user}", member.getAsMention())
                .replace("{server}", guildName);
            systemChannel.sendMessage("🎉 " + message).queue();
        }
        
        logger.info("HIGH PRIORITY: Member {} joined guild {}", 
                   member.getEffectiveName(), guildName);
    }

    /**
     * NORMAL PRIORITY: Member join event (default priority)
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMemberJoin(GuildMemberJoinEvent event) {
        if (!autoRoles) return;
        
        // Auto-assign default role
        String defaultRoleId = getConfiguration().getString("auto_role.default_role_id");
        if (defaultRoleId != null) {
            event.getGuild().getRoleById(defaultRoleId)
                .ifPresent(role -> event.getGuild().addRoleToMember(event.getMember(), role).queue());
        }
        
        // Store join timestamp for analytics
        getPluginDataStorage().getUserStorage(event.getUser().getId())
            .set("join_date", System.currentTimeMillis());
        
        logger.info("NORMAL PRIORITY: Processing member join for {}", 
                   event.getMember().getEffectiveName());
    }

    /**
     * LOW PRIORITY: Member join event (runs after other plugins)
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onMemberJoinLow(GuildMemberJoinEvent event) {
        if (!logEvents) return;
        
        // Log detailed member information
        Member member = event.getMember();
        String logMessage = String.format(
            "Member Join Event:\n" +
            "- User: %s (%s)\n" +
            "- Guild: %s\n" +
            "- Account Created: %s\n" +
            "- Avatar: %s",
            member.getEffectiveName(),
            member.getId(),
            event.getGuild().getName(),
            member.getTimeCreated().format(DateTimeFormatter.ISO_LOCAL_DATE),
            member.getEffectiveAvatarUrl()
        );
        
        logger.info("LOW PRIORITY: {}", logMessage);
    }

    /**
     * Member leave event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMemberLeave(GuildMemberRemoveEvent event) {
        if (!welcomeEnabled) return;
        
        String userName = event.getUser().getEffectiveName();
        String guildName = event.getGuild().getName();
        
        TextChannel systemChannel = event.getGuild().getSystemChannel();
        if (systemChannel != null) {
            String message = leaveMessage
                .replace("{user}", userName)
                .replace("{server}", guildName);
            systemChannel.sendMessage("👋 " + message).queue();
        }
        
        // Clean up user data
        getPluginDataStorage().getUserStorage(event.getUser().getId()).clear();
        messageCount.remove(event.getUser().getId());
        lastActivity.remove(event.getUser().getId());
        
        logger.info("Member {} left guild {}", userName, guildName);
    }

    /**
     * Message received event with filtering
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMessage(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        
        String userId = event.getAuthor().getId();
        
        // Update message count
        messageCount.merge(userId, 1, Integer::sum);
        lastActivity.put(userId, System.currentTimeMillis());
        
        // Auto-react to messages containing certain keywords
        String content = event.getMessage().getContentRaw().toLowerCase();
        if (content.contains("hello") || content.contains("hi")) {
            event.getMessage().addReaction(Emoji.fromUnicode("👋")).queue();
        } else if (content.contains("thanks") || content.contains("thank you")) {
            event.getMessage().addReaction(Emoji.fromUnicode("❤️")).queue();
        } else if (content.contains("bot") && content.contains("good")) {
            event.getMessage().addReaction(Emoji.fromUnicode("🤖")).queue();
        }
        
        // Log spam detection (example)
        int count = messageCount.get(userId);
        if (count > 10) { // 10 messages threshold
            logger.warn("Potential spam detected from user {} ({} messages)", 
                       event.getAuthor().getEffectiveName(), count);
        }
        
        // Store message data for analytics
        if (logEvents) {
            getPluginDataStorage().getGuildStorage(event.getGuild().getId())
                .increment("total_messages", 1);
        }
    }

    /**
     * Reaction events for role management
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) return;
        
        // Reaction role system example
        String messageId = getConfiguration().getString("reaction_roles.message_id");
        if (messageId != null && event.getMessageId().equals(messageId)) {
            String emoji = event.getReaction().getEmoji().getAsReactionCode();
            String roleId = getConfiguration().getString("reaction_roles.roles." + emoji);
            
            if (roleId != null && event.getGuild() != null) {
                event.getGuild().getRoleById(roleId)
                    .ifPresent(role -> {
                        event.getGuild().addRoleToMember(event.getUserIdLong(), role).queue(
                            success -> logger.debug("Added role {} to user {}", 
                                                   role.getName(), event.getUser().getEffectiveName()),
                            error -> logger.error("Failed to add role: {}", error.getMessage())
                        );
                    });
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onReactionRemove(MessageReactionRemoveEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) return;
        
        // Remove reaction roles
        String messageId = getConfiguration().getString("reaction_roles.message_id");
        if (messageId != null && event.getMessageId().equals(messageId)) {
            String emoji = event.getReaction().getEmoji().getAsReactionCode();
            String roleId = getConfiguration().getString("reaction_roles.roles." + emoji);
            
            if (roleId != null && event.getGuild() != null) {
                event.getGuild().getRoleById(roleId)
                    .ifPresent(role -> {
                        event.getGuild().removeRoleFromMember(event.getUserIdLong(), role).queue();
                    });
            }
        }
    }

    /**
     * Voice channel events
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (!logEvents) return;
        
        if (event.getChannelJoined() != null) {
            logger.info("User {} joined voice channel: {}", 
                       event.getMember().getEffectiveName(), 
                       event.getChannelJoined().getName());
        }
        
        if (event.getChannelLeft() != null) {
            logger.info("User {} left voice channel: {}", 
                       event.getMember().getEffectiveName(), 
                       event.getChannelLeft().getName());
        }
    }

    /**
     * User status change events
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onStatusChange(UserUpdateOnlineStatusEvent event) {
        if (!logEvents) return;
        
        logger.debug("User {} status changed: {} -> {}", 
                    event.getUser().getEffectiveName(),
                    event.getOldOnlineStatus(),
                    event.getNewOnlineStatus());
    }

    /**
     * Server management events
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onRoleCreate(RoleCreateEvent event) {
        if (!logEvents) return;
        
        logger.info("Role created in {}: {} ({})", 
                   event.getGuild().getName(),
                   event.getRole().getName(),
                   event.getRole().getId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRoleDelete(RoleDeleteEvent event) {
        if (!logEvents) return;
        
        logger.info("Role deleted in {}: {} ({})", 
                   event.getGuild().getName(),
                   event.getRole().getName(),
                   event.getRole().getId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChannelCreate(ChannelCreateEvent event) {
        if (!logEvents) return;
        
        logger.info("Channel created in {}: {} ({})", 
                   event.getGuild().getName(),
                   event.getChannel().getName(),
                   event.getChannel().getId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChannelDelete(ChannelDeleteEvent event) {
        if (!logEvents) return;
        
        logger.info("Channel deleted in {}: {} ({})", 
                   event.getGuild().getName(),
                   event.getChannel().getName(),
                   event.getChannel().getId());
    }

    /**
     * Start background task for activity monitoring
     */
    private void startActivityMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            long inactiveThreshold = TimeUnit.MINUTES.toMillis(30); // 30 minutes
            
            // Clean up inactive users from memory
            lastActivity.entrySet().removeIf(entry -> 
                now - entry.getValue() > inactiveThreshold);
            
            // Reset message counts periodically
            if (messageCount.size() > 1000) {
                messageCount.clear();
                logger.debug("Reset message count cache");
            }
            
        }, 5, 5, TimeUnit.MINUTES);
    }
}
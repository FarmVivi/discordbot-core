package fr.farmvivi.discordbot.core.command.impl;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandBuilder;
import fr.farmvivi.discordbot.core.api.command.CommandRegistry;
import fr.farmvivi.discordbot.core.api.command.CommandResult;
import fr.farmvivi.discordbot.core.api.command.CommandService;
import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import fr.farmvivi.discordbot.core.api.storage.DataStorageManager;
import fr.farmvivi.discordbot.core.api.storage.GlobalStorage;
import fr.farmvivi.discordbot.core.api.storage.GuildStorage;
import fr.farmvivi.discordbot.core.api.storage.UserGuildStorage;
import fr.farmvivi.discordbot.core.command.listener.SlashCommandListener;
import fr.farmvivi.discordbot.core.command.listener.TextCommandListener;
import fr.farmvivi.discordbot.core.command.util.CommandSynchronizer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Implementation of CommandService.
 * Provides methods for registering, executing, and synchronizing commands.
 */
public class CommandServiceImpl implements CommandService {

    private static final Logger logger = LoggerFactory.getLogger(CommandServiceImpl.class);
    
    // Default command prefix
    private static final String DEFAULT_PREFIX = "!";
    
    // Keys for storage
    private static final String GLOBAL_PREFIX_KEY = "command.prefix";
    private static final String GUILD_PREFIX_KEY = "command.prefix";
    private static final String COMMAND_DISABLED_KEY_PREFIX = "command.disabled.";
    private static final String COMMAND_COOLDOWN_KEY_PREFIX = "command.cooldowns.";
    
    // Command registry
    private final CommandRegistry registry;
    
    // JDA instance
    private JDA jda;
    
    // Event listeners
    private final SlashCommandListener slashCommandListener;
    private final TextCommandListener textCommandListener;
    
    // Command synchronizer
    private final CommandSynchronizer commandSynchronizer;
    
    // Storage
    private DataStorageManager storageManager;
    
    // Cache for guild prefixes
    private final Map<String, String> guildPrefixCache = new ConcurrentHashMap<>();
    
    // Cooldowns for commands (userId-commandName -> timestamp)
    private final Map<String, Long> commandCooldowns = new ConcurrentHashMap<>();
    
    // Execution statistics
    private final AtomicLong executionCount = new AtomicLong(0);
    private final AtomicLong successfulExecutionCount = new AtomicLong(0);
    private final AtomicLong failedExecutionCount = new AtomicLong(0);
    private double totalExecutionTimeMs = 0;
    
    // Enabled flag
    private boolean enabled = false;
    
    /**
     * Creates a new command service.
     */
    public CommandServiceImpl() {
        this.registry = new CommandRegistryImpl();
        this.slashCommandListener = new SlashCommandListener(this);
        this.textCommandListener = new TextCommandListener(this);
        this.commandSynchronizer = new CommandSynchronizer(this);
    }
    
    /**
     * Creates a new command service with a data storage manager.
     *
     * @param storageManager the data storage manager
     */
    public CommandServiceImpl(DataStorageManager storageManager) {
        this();
        this.storageManager = storageManager;
    }
    
    @Override
    public CommandRegistry getRegistry() {
        return registry;
    }
    
    @Override
    public CommandBuilder newCommand() {
        return CommandBuilderImpl.create();
    }
    
    @Override
    public String getPrefix() {
        if (storageManager != null) {
            GlobalStorage globalStorage = storageManager.getGlobalStorage();
            return globalStorage.get(GLOBAL_PREFIX_KEY, String.class).orElse(DEFAULT_PREFIX);
        }
        return DEFAULT_PREFIX;
    }
    
    @Override
    public String getPrefix(String guildId) {
        // Check cache first
        if (guildPrefixCache.containsKey(guildId)) {
            return guildPrefixCache.get(guildId);
        }
        
        // If we have storage, get from there
        if (storageManager != null) {
            GuildStorage guildStorage = storageManager.getGuildStorage(guildId);
            String prefix = guildStorage.get(GUILD_PREFIX_KEY, String.class).orElse(getPrefix());
            guildPrefixCache.put(guildId, prefix);
            return prefix;
        }
        
        // Default to global prefix
        return getPrefix();
    }
    
    @Override
    public void setPrefix(String prefix) {
        if (storageManager != null) {
            GlobalStorage globalStorage = storageManager.getGlobalStorage();
            globalStorage.set(GLOBAL_PREFIX_KEY, prefix);
        }
    }
    
    @Override
    public void setPrefix(String guildId, String prefix) {
        if (storageManager != null) {
            GuildStorage guildStorage = storageManager.getGuildStorage(guildId);
            guildStorage.set(GUILD_PREFIX_KEY, prefix);
        }
        guildPrefixCache.put(guildId, prefix);
    }
    
    @Override
    public boolean registerCommand(Command command, Plugin plugin) {
        return registry.register(command, plugin);
    }
    
    @Override
    public boolean registerCommand(Plugin plugin, Consumer<CommandBuilder> builderConsumer) {
        CommandBuilder builder = newCommand();
        builderConsumer.accept(builder);
        Command command = builder.build();
        return registerCommand(command, plugin);
    }
    
    @Override
    public CompletableFuture<Void> synchronizeCommands() {
        // Synchronize both global and guild commands
        CompletableFuture<Void> globalFuture = synchronizeGlobalCommands();
        
        // Get all guilds and synchronize for each
        if (jda != null) {
            CompletableFuture<Void>[] guildFutures = jda.getGuilds().stream()
                    .map(this::synchronizeGuildCommands)
                    .toArray(CompletableFuture[]::new);
            
            return CompletableFuture.allOf(
                    CompletableFuture.allOf(guildFutures),
                    globalFuture
            );
        }
        
        return globalFuture;
    }
    
    @Override
    public CompletableFuture<Void> synchronizeGuildCommands(Guild guild) {
        return commandSynchronizer.synchronizeGuild(guild);
    }
    
    @Override
    public CompletableFuture<Void> synchronizeGlobalCommands() {
        return commandSynchronizer.synchronizeGlobal();
    }
    
    @Override
    public void enable() {
        if (enabled) {
            return;
        }
        
        logger.info("Enabling command service...");
        
        // Register event listeners
        if (jda != null) {
            jda.addEventListener(slashCommandListener, textCommandListener);
        }
        
        // Load command states
        loadCommandStates();
        
        enabled = true;
    }
    
    @Override
    public void disable() {
        if (!enabled) {
            return;
        }
        
        logger.info("Disabling command service...");
        
        // Unregister event listeners
        if (jda != null) {
            jda.removeEventListener(slashCommandListener, textCommandListener);
        }
        
        // Save command states
        saveCommandStates();
        
        enabled = false;
    }
    
    @Override
    public void setJDA(JDA jda) {
        if (this.jda != null && enabled) {
            // Unregister event listeners from old JDA
            this.jda.removeEventListener(slashCommandListener, textCommandListener);
        }
        
        this.jda = jda;
        
        if (enabled && jda != null) {
            // Register event listeners to new JDA
            jda.addEventListener(slashCommandListener, textCommandListener);
        }
    }
    
    @Override
    public JDA getJDA() {
        return jda;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public long getCommandExecutionCount() {
        return executionCount.get();
    }
    
    @Override
    public long getSuccessfulCommandExecutionCount() {
        return successfulExecutionCount.get();
    }
    
    @Override
    public long getFailedCommandExecutionCount() {
        return failedExecutionCount.get();
    }
    
    @Override
    public double getAverageExecutionTimeMs() {
        long count = executionCount.get();
        return count > 0 ? totalExecutionTimeMs / count : 0;
    }
    
    @Override
    public boolean isOnCooldown(String userId, String commandName) {
        String key = userId + "-" + commandName;
        
        // Check if command is on cooldown in memory
        if (commandCooldowns.containsKey(key)) {
            long expiry = commandCooldowns.get(key);
            if (System.currentTimeMillis() < expiry) {
                return true;
            } else {
                // Cooldown expired
                commandCooldowns.remove(key);
                return false;
            }
        }
        
        // If we have storage, check for persistent cooldowns
        if (storageManager != null) {
            try {
                Command command = registry.getCommand(commandName).orElse(null);
                if (command != null && command.getCooldown() > 0) {
                    // Get guild from user context if command is guild-only
                    if (command.isGuildOnly() && !command.getGuildIds().isEmpty()) {
                        for (String guildId : command.getGuildIds()) {
                            UserGuildStorage storage = storageManager.getUserGuildStorage(userId, guildId);
                            Optional<Long> lastUsedOpt = storage.get(COMMAND_COOLDOWN_KEY_PREFIX + commandName, Long.class);
                            
                            if (lastUsedOpt.isPresent()) {
                                long lastUsed = lastUsedOpt.get();
                                long cooldownMs = command.getCooldown() * 1000L;
                                long expiryTime = lastUsed + cooldownMs;
                                
                                if (System.currentTimeMillis() < expiryTime) {
                                    // Load into memory for faster checks
                                    commandCooldowns.put(key, expiryTime);
                                    return true;
                                }
                            }
                        }
                    } else {
                        // Check global cooldown
                        GlobalStorage storage = storageManager.getGlobalStorage();
                        Optional<Long> lastUsedOpt = storage.get(COMMAND_COOLDOWN_KEY_PREFIX + commandName + "." + userId, Long.class);
                        
                        if (lastUsedOpt.isPresent()) {
                            long lastUsed = lastUsedOpt.get();
                            long cooldownMs = command.getCooldown() * 1000L;
                            long expiryTime = lastUsed + cooldownMs;
                            
                            if (System.currentTimeMillis() < expiryTime) {
                                // Load into memory for faster checks
                                commandCooldowns.put(key, expiryTime);
                                return true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error checking cooldown for command {} for user {}", commandName, userId, e);
            }
        }
        
        return false;
    }
    
    @Override
    public int getRemainingCooldown(String userId, String commandName) {
        String key = userId + "-" + commandName;
        
        // Check if command is on cooldown in memory
        if (commandCooldowns.containsKey(key)) {
            long expiry = commandCooldowns.get(key);
            long remaining = expiry - System.currentTimeMillis();
            
            if (remaining > 0) {
                // Convert to seconds
                return (int) (remaining / 1000);
            } else {
                // Cooldown expired
                commandCooldowns.remove(key);
                return 0;
            }
        }
        
        // If we have storage, check for persistent cooldowns
        if (storageManager != null) {
            try {
                Command command = registry.getCommand(commandName).orElse(null);
                if (command != null && command.getCooldown() > 0) {
                    // Get guild from user context if command is guild-only
                    if (command.isGuildOnly() && !command.getGuildIds().isEmpty()) {
                        for (String guildId : command.getGuildIds()) {
                            UserGuildStorage storage = storageManager.getUserGuildStorage(userId, guildId);
                            Optional<Long> lastUsedOpt = storage.get(COMMAND_COOLDOWN_KEY_PREFIX + commandName, Long.class);
                            
                            if (lastUsedOpt.isPresent()) {
                                long lastUsed = lastUsedOpt.get();
                                long cooldownMs = command.getCooldown() * 1000L;
                                long remaining = (lastUsed + cooldownMs) - System.currentTimeMillis();
                                
                                if (remaining > 0) {
                                    // Convert to seconds
                                    return (int) (remaining / 1000);
                                }
                            }
                        }
                    } else {
                        // Check global cooldown
                        GlobalStorage storage = storageManager.getGlobalStorage();
                        Optional<Long> lastUsedOpt = storage.get(COMMAND_COOLDOWN_KEY_PREFIX + commandName + "." + userId, Long.class);
                        
                        if (lastUsedOpt.isPresent()) {
                            long lastUsed = lastUsedOpt.get();
                            long cooldownMs = command.getCooldown() * 1000L;
                            long remaining = (lastUsed + cooldownMs) - System.currentTimeMillis();
                            
                            if (remaining > 0) {
                                // Convert to seconds
                                return (int) (remaining / 1000);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error checking cooldown for command {} for user {}", commandName, userId, e);
            }
        }
        
        return 0;
    }
    
    /**
     * Sets a cooldown for a user and command.
     *
     * @param userId      the user ID
     * @param commandName the command name
     * @param cooldownSec the cooldown in seconds
     */
    public void setCooldown(String userId, String commandName, int cooldownSec) {
        if (cooldownSec <= 0) {
            return;
        }
        
        String key = userId + "-" + commandName;
        long expiryTime = System.currentTimeMillis() + (cooldownSec * 1000L);
        
        // Set in memory
        commandCooldowns.put(key, expiryTime);
        
        // Set in storage
        if (storageManager != null) {
            try {
                Command command = registry.getCommand(commandName).orElse(null);
                if (command != null) {
                    long timestamp = System.currentTimeMillis();
                    
                    // Save in the appropriate storage based on command type
                    if (command.isGuildOnly() && !command.getGuildIds().isEmpty()) {
                        for (String guildId : command.getGuildIds()) {
                            UserGuildStorage storage = storageManager.getUserGuildStorage(userId, guildId);
                            storage.set(COMMAND_COOLDOWN_KEY_PREFIX + commandName, timestamp);
                        }
                    } else {
                        // Global cooldown
                        GlobalStorage storage = storageManager.getGlobalStorage();
                        storage.set(COMMAND_COOLDOWN_KEY_PREFIX + commandName + "." + userId, timestamp);
                    }
                }
            } catch (Exception e) {
                logger.error("Error setting cooldown for command {} for user {}", commandName, userId, e);
            }
        }
    }
    
    /**
     * Records command execution statistics.
     *
     * @param result        the command result
     * @param executionTimeMs the execution time in milliseconds
     */
    public void recordCommandExecution(CommandResult result, long executionTimeMs) {
        executionCount.incrementAndGet();
        totalExecutionTimeMs += executionTimeMs;
        
        if (result != null && result.isSuccess()) {
            successfulExecutionCount.incrementAndGet();
        } else {
            failedExecutionCount.incrementAndGet();
        }
    }
    
    /**
     * Loads command states from storage.
     * This includes command enabled/disabled state.
     */
    private void loadCommandStates() {
        if (storageManager == null) {
            return;
        }
        
        try {
            GlobalStorage storage = storageManager.getGlobalStorage();
            Map<String, Object> commandStates = storage.getAll();
            
            for (Map.Entry<String, Object> entry : commandStates.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(COMMAND_DISABLED_KEY_PREFIX)) {
                    String commandName = key.substring(COMMAND_DISABLED_KEY_PREFIX.length());
                    Boolean disabled = (Boolean) entry.getValue();
                    
                    if (disabled != null && disabled) {
                        registry.disableCommand(commandName);
                    } else {
                        registry.enableCommand(commandName);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error loading command states", e);
        }
    }
    
    /**
     * Saves command states to storage.
     * This includes command enabled/disabled state.
     */
    private void saveCommandStates() {
        if (storageManager == null) {
            return;
        }
        
        try {
            GlobalStorage storage = storageManager.getGlobalStorage();
            
            // Save enabled/disabled state
            for (String commandName : registry.getCommandNames()) {
                Command command = registry.getCommand(commandName).orElse(null);
                if (command != null) {
                    storage.set(COMMAND_DISABLED_KEY_PREFIX + commandName, !command.isEnabled());
                }
            }
        } catch (Exception e) {
            logger.error("Error saving command states", e);
        }
    }
    
    /**
     * Sets the data storage manager.
     *
     * @param storageManager the data storage manager
     */
    public void setStorageManager(DataStorageManager storageManager) {
        this.storageManager = storageManager;
    }
}

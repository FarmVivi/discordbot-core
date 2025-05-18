package fr.farmvivi.discordbot.core.command;

import fr.farmvivi.discordbot.core.api.command.*;
import fr.farmvivi.discordbot.core.api.command.event.CommandExecuteEvent;
import fr.farmvivi.discordbot.core.api.command.event.CommandExecutedEvent;
import fr.farmvivi.discordbot.core.api.command.exception.CommandParseException;
import fr.farmvivi.discordbot.core.api.command.exception.CommandPermissionException;
import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.permissions.PermissionManager;
import fr.farmvivi.discordbot.core.api.plugin.Plugin;
import fr.farmvivi.discordbot.core.api.storage.DataStorageManager;
import fr.farmvivi.discordbot.core.api.storage.GuildStorage;
import fr.farmvivi.discordbot.core.command.listener.CommandListener;
import fr.farmvivi.discordbot.core.command.parser.CommandParser;
import fr.farmvivi.discordbot.core.command.parser.SlashCommandParser;
import fr.farmvivi.discordbot.core.command.parser.TextCommandParser;
import fr.farmvivi.discordbot.core.command.system.HelpCommand;
import fr.farmvivi.discordbot.core.command.system.ShutdownCommand;
import fr.farmvivi.discordbot.core.command.system.VersionCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Implementation of the CommandService interface.
 * This service manages the lifecycle of commands, including registration,
 * execution, and synchronization with Discord.
 */
@SuppressWarnings("deprecation")
public class SimpleCommandService implements CommandService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCommandService.class);

    private final CommandRegistry registry;
    private final List<CommandParser> parsers = new ArrayList<>();
    private final EventManager eventManager;
    private final PermissionManager permissionManager;
    private final Configuration configuration;
    private final DataStorageManager storageManager;

    private JDA jda;
    private boolean enabled;
    private String defaultPrefix;
    private CommandListener commandListener;

    // Statistics
    private final AtomicLong commandExecutionCount = new AtomicLong();
    private final AtomicLong successfulCommandExecutionCount = new AtomicLong();
    private final AtomicLong failedCommandExecutionCount = new AtomicLong();
    private final AtomicLong totalExecutionTimeNs = new AtomicLong();

    // Cooldowns: userId -> (commandName -> expirationTime)
    private final Map<String, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    /**
     * Creates a new SimpleCommandService.
     *
     * @param eventManager      the event manager
     * @param permissionManager the permission manager
     * @param configuration     the configuration
     * @param storageManager    the storage manager
     */
    public SimpleCommandService(
            EventManager eventManager,
            PermissionManager permissionManager,
            Configuration configuration,
            DataStorageManager storageManager
    ) {
        this.eventManager = eventManager;
        this.permissionManager = permissionManager;
        this.configuration = configuration;
        this.storageManager = storageManager;

        this.registry = new SimpleCommandRegistry();

        // Load configuration
        try {
            this.enabled = configuration.getBoolean("commands.enabled", true);
            this.defaultPrefix = configuration.getString("commands.default-prefix", "!");

            logger.info("Command system " + (enabled ? "enabled" : "disabled") + " with default prefix: " + defaultPrefix);
        } catch (Exception e) {
            logger.warn("Failed to load command configuration, using defaults", e);
            this.enabled = true;
            this.defaultPrefix = "!";
        }

        // Register parsers
        parsers.add(new SlashCommandParser());
        parsers.add(new TextCommandParser(defaultPrefix));
    }

    @Override
    public CommandRegistry getRegistry() {
        return registry;
    }

    @Override
    public CommandBuilder newCommand() {
        return new SimpleCommandBuilder();
    }

    @Override
    public String getPrefix() {
        return defaultPrefix;
    }

    @Override
    public String getPrefix(String guildId) {
        if (guildId == null) {
            return defaultPrefix;
        }

        GuildStorage guildStorage = storageManager.getGuildStorage(guildId);
        return guildStorage.get("commands.prefix", String.class).orElse(defaultPrefix);
    }

    @Override
    public void setPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("Prefix cannot be null or empty");
        }

        this.defaultPrefix = prefix;

        // Update configuration
        configuration.set("commands.default-prefix", prefix);
        try {
            configuration.save();
        } catch (ConfigurationException e) {
            logger.error("Failed to save command prefix to configuration", e);
        }

        // Update text command parser
        for (CommandParser parser : parsers) {
            if (parser instanceof TextCommandParser textParser) {
                parsers.remove(textParser);
                parsers.add(new TextCommandParser(prefix));
                break;
            }
        }
    }

    @Override
    public void setPrefix(String guildId, String prefix) {
        if (guildId == null) {
            setPrefix(prefix);
            return;
        }

        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("Prefix cannot be null or empty");
        }

        GuildStorage guildStorage = storageManager.getGuildStorage(guildId);
        guildStorage.set("commands.prefix", prefix);
    }

    @Override
    public boolean registerCommand(Command command, Plugin plugin) {
        boolean result = registry.register(command, plugin);

        // If the command was registered and the service is enabled, synchronize commands
        if (result && isEnabled() && jda != null && jda.getStatus() == JDA.Status.CONNECTED) {
            if (command.getGuildIds().isEmpty()) {
                synchronizeGlobalCommands();
            } else {
                for (String guildId : command.getGuildIds()) {
                    Guild guild = jda.getGuildById(guildId);
                    if (guild != null) {
                        synchronizeGuildCommands(guild);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Registers a command without associating it with a plugin.
     * This is used for system commands.
     *
     * @param command the command to register
     * @return true if the command was registered
     */
    public boolean registerCommand(Command command) {
        return registerCommand(command, null);
    }

    @Override
    public boolean registerCommand(Plugin plugin, Consumer<CommandBuilder> builderConsumer) {
        SimpleCommandBuilder builder = new SimpleCommandBuilder();
        builderConsumer.accept(builder);
        Command command = builder.build();
        return registerCommand(command, plugin);
    }

    /**
     * Registers a command without associating it with a plugin.
     * This is used for system commands.
     *
     * @param builderConsumer consumer to configure the command builder
     * @return true if the command was registered
     */
    public boolean registerCommand(Consumer<CommandBuilder> builderConsumer) {
        SimpleCommandBuilder builder = new SimpleCommandBuilder();
        builderConsumer.accept(builder);
        Command command = builder.build();
        return registerCommand(command);
    }

    @Override
    public CompletableFuture<Void> synchronizeCommands() {
        if (!isEnabled() || jda == null || jda.getStatus() != JDA.Status.CONNECTED) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> globalFuture = synchronizeGlobalCommands();

        // Synchronize guild-specific commands
        Set<String> guildIds = new HashSet<>();
        for (Command command : registry.getCommands()) {
            guildIds.addAll(command.getGuildIds());
        }

        List<CompletableFuture<Void>> guildFutures = new ArrayList<>();
        for (String guildId : guildIds) {
            Guild guild = jda.getGuildById(guildId);
            if (guild != null) {
                guildFutures.add(synchronizeGuildCommands(guild));
            }
        }

        // Return a future that completes when all futures complete
        CompletableFuture<Void> allGuildsFuture = CompletableFuture.allOf(
                guildFutures.toArray(new CompletableFuture[0]));

        return CompletableFuture.allOf(globalFuture, allGuildsFuture);
    }

    @Override
    public CompletableFuture<Void> synchronizeGuildCommands(Guild guild) {
        if (!isEnabled() || jda == null || jda.getStatus() != JDA.Status.CONNECTED) {
            return CompletableFuture.completedFuture(null);
        }

        List<CommandData> commandData = new ArrayList<>();

        // Collect commands for this guild
        for (Command command : registry.getCommands()) {
            if (!command.isEnabled()) {
                continue;
            }

            if (command.getGuildIds().contains(guild.getId())) {
                commandData.add(createCommandData(command));
            }
        }

        logger.info("Synchronizing {} guild commands for guild {}", commandData.size(), guild.getName());

        // Update the commands
        return guild.updateCommands().addCommands(commandData).submit()
                .thenRun(() -> logger.info("Guild commands synchronized for guild {}", guild.getName()));
    }

    @Override
    public CompletableFuture<Void> synchronizeGlobalCommands() {
        if (!isEnabled() || jda == null || jda.getStatus() != JDA.Status.CONNECTED) {
            return CompletableFuture.completedFuture(null);
        }

        List<CommandData> commandData = new ArrayList<>();

        // Collect global commands
        for (Command command : registry.getCommands()) {
            if (!command.isEnabled()) {
                continue;
            }

            if (command.getGuildIds().isEmpty() && !command.isSubcommand()) {
                commandData.add(createCommandData(command));
            }
        }

        logger.info("Synchronizing {} global commands", commandData.size());

        // Update the commands
        return jda.updateCommands().addCommands(commandData).submit()
                .thenRun(() -> logger.info("Global commands synchronized"));
    }

    @Override
    public void enable() {
        if (isEnabled()) {
            return;
        }

        enabled = true;
        configuration.set("commands.enabled", true);
        try {
            configuration.save();
        } catch (ConfigurationException e) {
            logger.error("Failed to save command enabled state to configuration", e);
        }

        // Register the command listener
        if (jda != null) {
            commandListener = new CommandListener(this);
            jda.addEventListener(commandListener);

            // Register system commands
            registerSystemCommands();

            // Synchronize commands
            synchronizeCommands();
        }

        logger.info("Command service enabled");
    }

    @Override
    public void disable() {
        if (!isEnabled()) {
            return;
        }

        enabled = false;
        configuration.set("commands.enabled", false);
        try {
            configuration.save();
        } catch (ConfigurationException e) {
            logger.error("Failed to save command enabled state to configuration", e);
        }

        // Unregister the command listener
        if (jda != null && commandListener != null) {
            jda.removeEventListener(commandListener);
            commandListener = null;
        }

        logger.info("Command service disabled");
    }

    @Override
    public void setJDA(JDA jda) {
        this.jda = jda;

        if (isEnabled() && jda != null) {
            // Register the command listener
            commandListener = new CommandListener(this);
            jda.addEventListener(commandListener);

            // Register system commands
            registerSystemCommands();

            // Synchronize commands
            jda.getGuildCache().forEach(this::synchronizeGuildCommands);
            synchronizeGlobalCommands();
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
        return commandExecutionCount.get();
    }

    @Override
    public long getSuccessfulCommandExecutionCount() {
        return successfulCommandExecutionCount.get();
    }

    @Override
    public long getFailedCommandExecutionCount() {
        return failedCommandExecutionCount.get();
    }

    @Override
    public double getAverageExecutionTimeMs() {
        long count = commandExecutionCount.get();
        if (count == 0) {
            return 0;
        }

        return (double) totalExecutionTimeNs.get() / (count * 1_000_000);
    }

    @Override
    public boolean isOnCooldown(String userId, String commandName) {
        Map<String, Long> userCooldowns = cooldowns.get(userId);
        if (userCooldowns == null) {
            return false;
        }

        Long expirationTime = userCooldowns.get(commandName);
        if (expirationTime == null) {
            return false;
        }

        return expirationTime > System.currentTimeMillis();
    }

    @Override
    public int getRemainingCooldown(String userId, String commandName) {
        Map<String, Long> userCooldowns = cooldowns.get(userId);
        if (userCooldowns == null) {
            return 0;
        }

        Long expirationTime = userCooldowns.get(commandName);
        if (expirationTime == null) {
            return 0;
        }

        long remaining = expirationTime - System.currentTimeMillis();
        return remaining > 0 ? (int) (remaining / 1000) : 0;
    }

    /**
     * Executes a command with the given context.
     * This method is called by the command listener.
     *
     * @param command the command to execute
     * @param context the command context
     * @return the command result
     */
    public CommandResult executeCommand(Command command, CommandContext context) {
        if (!isEnabled()) {
            return CommandResult.error("Command system is disabled");
        }

        // Check if the command is enabled
        if (!command.isEnabled()) {
            return CommandResult.error("This command is disabled");
        }

        // Check guild-only
        if (command.isGuildOnly() && !context.isFromGuild()) {
            return CommandResult.error("This command can only be used in a server");
        }

        // Check admin permission
        if (command.getPermission() != null) {
            String userId = context.getUser().getId();
            String guildId = context.getGuild().map(Guild::getId).orElse(null);

            try {
                if (!permissionManager.hasPermission(userId, guildId, command.getPermission())) {
                    throw new CommandPermissionException(
                            "You don't have permission to use this command", command.getPermission());
                }
            } catch (Exception e) {
                return CommandResult.error("Permission error: " + e.getMessage());
            }
        }

        // Check cooldown
        String userId = context.getUser().getId();
        if (isOnCooldown(userId, command.getName())) {
            int seconds = getRemainingCooldown(userId, command.getName());
            return CommandResult.error("This command is on cooldown. Please wait " + seconds +
                    " second" + (seconds == 1 ? "" : "s") + " before using it again.");
        }

        // Fire command execute event
        CommandExecuteEvent executeEvent = new CommandExecuteEvent(command, context);
        eventManager.fireEvent(executeEvent);

        // Check if the event was cancelled
        if (executeEvent.isCancelled()) {
            return CommandResult.error("Command execution was cancelled");
        }

        // Execute the command
        CommandResult result;
        long startTime = System.nanoTime();

        try {
            result = command.execute(context);

            // Apply cooldown if specified
            if (command.getCooldown() > 0) {
                applyCooldown(userId, command.getName(), command.getCooldown());
            }
        } catch (Exception e) {
            logger.error("Error executing command {}: {}", command.getName(), e.getMessage(), e);
            result = CommandResult.error("An error occurred while executing the command: " + e.getMessage());
        }

        long endTime = System.nanoTime();
        long executionTimeNs = endTime - startTime;

        // Update statistics
        commandExecutionCount.incrementAndGet();
        totalExecutionTimeNs.addAndGet(executionTimeNs);

        if (result.isSuccess()) {
            successfulCommandExecutionCount.incrementAndGet();
        } else {
            failedCommandExecutionCount.incrementAndGet();
        }

        // Fire command executed event
        CommandExecutedEvent executedEvent = new CommandExecutedEvent(
                command, context, result, executionTimeNs / 1_000_000);
        eventManager.fireEvent(executedEvent);

        return result;
    }

    /**
     * Applies a cooldown to a command for a user.
     *
     * @param userId          the user ID
     * @param commandName     the command name
     * @param cooldownSeconds the cooldown in seconds
     */
    private void applyCooldown(String userId, String commandName, int cooldownSeconds) {
        long expirationTime = System.currentTimeMillis() + (cooldownSeconds * 1000L);
        cooldowns.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(commandName, expirationTime);
    }

    /**
     * Creates JDA command data from a command.
     *
     * @param command the command
     * @return the command data
     */
    private CommandData createCommandData(Command command) {
        SlashCommandData data = Commands.slash(command.getName().toLowerCase(), command.getDescription());

        // Add options
        for (fr.farmvivi.discordbot.core.api.command.option.CommandOption<?> option : command.getOptions()) {
            OptionData optionData = new OptionData(
                    convertOptionType(option.getType()),
                    option.getName(),
                    option.getDescription(),
                    option.isRequired()
            );

            // Add min/max values for number options
            if (option.getMinValue() != null) {
                if (option.getType() == fr.farmvivi.discordbot.core.api.command.option.OptionType2.INTEGER) {
                    optionData.setMinValue(option.getMinValue().longValue());
                } else if (option.getType() == fr.farmvivi.discordbot.core.api.command.option.OptionType2.NUMBER) {
                    optionData.setMinValue(option.getMinValue().doubleValue());
                }
            }

            if (option.getMaxValue() != null) {
                if (option.getType() == fr.farmvivi.discordbot.core.api.command.option.OptionType2.INTEGER) {
                    optionData.setMaxValue(option.getMaxValue().longValue());
                } else if (option.getType() == fr.farmvivi.discordbot.core.api.command.option.OptionType2.NUMBER) {
                    optionData.setMaxValue(option.getMaxValue().doubleValue());
                }
            }

            // Add min/max length for string options
            if (option.getMinLength() != null) {
                optionData.setMinLength(option.getMinLength());
            }

            if (option.getMaxLength() != null) {
                optionData.setMaxLength(option.getMaxLength());
            }

            // Add choices
            if (!option.getChoices().isEmpty()) {
                for (fr.farmvivi.discordbot.core.api.command.option.OptionChoice<?> choice : option.getChoices()) {
                    if (choice.value() instanceof String string) {
                        optionData.addChoice(choice.name(), string);
                    } else if (choice.value() instanceof Integer integer) {
                        optionData.addChoice(choice.name(), integer);
                    } else if (choice.value() instanceof Double doubleValue) {
                        optionData.addChoice(choice.name(), doubleValue);
                    }
                }
            }

            // Enable autocomplete
            if (option.getAutocompleteProvider() != null) {
                optionData.setAutoComplete(true);
            }

            data.addOptions(optionData);
        }

        // Add subcommands
        if (!command.getSubcommands().isEmpty()) {
            // Group subcommands if a group is specified
            if (command.getGroup() != null) {
                SubcommandGroupData groupData = new SubcommandGroupData(
                        command.getGroup().toLowerCase(),
                        command.getDescription()
                );

                for (Command subcommand : command.getSubcommands()) {
                    SubcommandData subcommandData = new SubcommandData(
                            subcommand.getName().toLowerCase(),
                            subcommand.getDescription()
                    );

                    // Add options to subcommand
                    for (fr.farmvivi.discordbot.core.api.command.option.CommandOption<?> option : subcommand.getOptions()) {
                        OptionData optionData = new OptionData(
                                convertOptionType(option.getType()),
                                option.getName(),
                                option.getDescription(),
                                option.isRequired()
                        );

                        subcommandData.addOptions(optionData);
                    }

                    groupData.addSubcommands(subcommandData);
                }

                data.addSubcommandGroups(groupData);
            } else {
                // Add subcommands directly
                for (Command subcommand : command.getSubcommands()) {
                    SubcommandData subcommandData = new SubcommandData(
                            subcommand.getName().toLowerCase(),
                            subcommand.getDescription()
                    );

                    // Add options to subcommand
                    for (fr.farmvivi.discordbot.core.api.command.option.CommandOption<?> option : subcommand.getOptions()) {
                        OptionData optionData = new OptionData(
                                convertOptionType(option.getType()),
                                option.getName(),
                                option.getDescription(),
                                option.isRequired()
                        );

                        subcommandData.addOptions(optionData);
                    }

                    data.addSubcommands(subcommandData);
                }
            }
        }

        // Set default permissions
        if (command.getPermission() != null) {
            data.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
        } else {
            data.setDefaultPermissions(DefaultMemberPermissions.ENABLED);
        }

        // Set guild-only
        data.setGuildOnly(command.isGuildOnly());

        return data;
    }

    /**
     * Converts an option type to a JDA option type.
     *
     * @param type the option type
     * @return the JDA option type
     */
    private OptionType convertOptionType(fr.farmvivi.discordbot.core.api.command.option.OptionType2 type) {
        return type.getJdaType();
    }

    /**
     * Registers system commands.
     */
    private void registerSystemCommands() {
        // Register help command
        registerCommand(new HelpCommand(this).getCommand());

        // Register version command
        registerCommand(new VersionCommand().getCommand());

        // Register shutdown command
        registerCommand(new ShutdownCommand().getCommand());
    }

    /**
     * Processes a command from a JDA event.
     * This method is called by the command listener.
     *
     * @param event the JDA event
     */
    public void processCommand(net.dv8tion.jda.api.events.Event event) {
        if (!isEnabled()) {
            return;
        }

        // Find a parser that can handle this event
        for (CommandParser parser : parsers) {
            if (parser.canParse(event) && parser.isCommandInvocation(event)) {
                try {
                    // Extract the command name
                    String commandName = parser.extractCommandName(event);

                    // Find the command
                    Command command = registry.getCommand(commandName)
                            .orElseGet(() -> registry.getCommandByAlias(commandName).orElse(null));

                    if (command == null) {
                        // Unknown command
                        continue;
                    }

                    // Parse the command
                    CommandContext context = parser.parse(event, command);

                    // Execute the command
                    CommandResult result = executeCommand(command, context);

                    // If the execution failed and the result contains an error message,
                    // reply with the error message
                    if (!result.isSuccess() && result.getErrorMessage() != null) {
                        context.replyError(result.getErrorMessage());
                    }

                    // We found and executed a command, so we're done
                    return;
                } catch (CommandParseException e) {
                    // Failed to parse the command - try the next parser
                    logger.debug("Failed to parse command: {}", e.getMessage());
                } catch (Exception e) {
                    // Something went wrong - log and continue
                    logger.error("Error processing command: {}", e.getMessage(), e);
                }
            }
        }
    }
}

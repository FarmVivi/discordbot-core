package fr.farmvivi.discordbot.core.command.util;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandService;
import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionChoice;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Utility class for synchronizing commands with Discord.
 */
public class CommandSynchronizer {

    private static final Logger logger = LoggerFactory.getLogger(CommandSynchronizer.class);
    
    private final CommandService commandService;
    
    /**
     * Creates a new CommandSynchronizer.
     *
     * @param commandService the command service
     */
    public CommandSynchronizer(CommandService commandService) {
        this.commandService = commandService;
    }
    
    /**
     * Synchronizes all commands with Discord.
     *
     * @return a future that completes when the synchronization is done
     */
    public CompletableFuture<Void> synchronizeAll() {
        JDA jda = commandService.getJDA();
        if (jda == null) {
            logger.warn("Cannot synchronize commands: JDA is null");
            return CompletableFuture.completedFuture(null);
        }
        
        // Synchronize global commands
        CompletableFuture<Void> globalFuture = synchronizeGlobal();
        
        // Synchronize guild commands
        List<CompletableFuture<Void>> guildFutures = new ArrayList<>();
        
        // Get all unique guild IDs from guild-only commands
        Set<String> guildIds = new HashSet<>();
        for (Command command : commandService.getRegistry().getCommands()) {
            if (command.isGuildOnly()) {
                guildIds.addAll(command.getGuildIds());
            }
        }
        
        // If no guild IDs specified, synchronize with all connected guilds
        if (guildIds.isEmpty()) {
            for (Guild guild : jda.getGuilds()) {
                guildFutures.add(synchronizeGuild(guild));
            }
        } else {
            // Synchronize with specific guilds
            for (String guildId : guildIds) {
                Guild guild = jda.getGuildById(guildId);
                if (guild != null) {
                    guildFutures.add(synchronizeGuild(guild));
                } else {
                    logger.warn("Cannot synchronize commands for guild {}: guild not found", guildId);
                }
            }
        }
        
        // Wait for all guild synchronizations to complete
        CompletableFuture<Void> allGuildsFuture = CompletableFuture.allOf(
                guildFutures.toArray(new CompletableFuture[0]));
        
        // Return a future that completes when both global and guild synchronizations are done
        return CompletableFuture.allOf(globalFuture, allGuildsFuture);
    }
    
    /**
     * Synchronizes global commands with Discord.
     *
     * @return a future that completes when the synchronization is done
     */
    public CompletableFuture<Void> synchronizeGlobal() {
        JDA jda = commandService.getJDA();
        if (jda == null) {
            logger.warn("Cannot synchronize global commands: JDA is null");
            return CompletableFuture.completedFuture(null);
        }
        
        // Filter global commands
        List<Command> globalCommands = commandService.getRegistry().getCommands().stream()
                .filter(cmd -> !cmd.isGuildOnly() || cmd.getGuildIds().isEmpty())
                .collect(Collectors.toList());
        
        // Convert to JDA command data
        List<SlashCommandData> commandData = globalCommands.stream()
                .map(this::convertToCommandData)
                .collect(Collectors.toList());
        
        // Update commands on Discord
        return jda.updateCommands()
                .addCommands(commandData)
                .submit()
                .thenApply(commands -> {
                    logger.info("Synchronized {} global commands", commands.size());
                    return null;
                })
                .exceptionally(ex -> {
                    logger.error("Failed to synchronize global commands", ex);
                    return null;
                });
    }
    
    /**
     * Synchronizes guild commands with Discord.
     *
     * @param guild the guild
     * @return a future that completes when the synchronization is done
     */
    public CompletableFuture<Void> synchronizeGuild(Guild guild) {
        if (guild == null) {
            logger.warn("Cannot synchronize guild commands: guild is null");
            return CompletableFuture.completedFuture(null);
        }
        
        // Filter commands for this guild
        List<Command> guildCommands = commandService.getRegistry().getCommands().stream()
                .filter(cmd -> cmd.isGuildOnly())
                .filter(cmd -> cmd.getGuildIds().isEmpty() || cmd.getGuildIds().contains(guild.getId()))
                .collect(Collectors.toList());
        
        // Convert to JDA command data
        List<SlashCommandData> commandData = guildCommands.stream()
                .map(this::convertToCommandData)
                .collect(Collectors.toList());
        
        // Update commands on Discord
        return guild.updateCommands()
                .addCommands(commandData)
                .submit()
                .thenApply(commands -> {
                    logger.info("Synchronized {} commands for guild {}", commands.size(), guild.getName());
                    return null;
                })
                .exceptionally(ex -> {
                    logger.error("Failed to synchronize commands for guild {}", guild.getName(), ex);
                    return null;
                });
    }
    
    /**
     * Converts a command to JDA command data.
     *
     * @param command the command
     * @return the command data
     */
    private SlashCommandData convertToCommandData(Command command) {
        // Create the command data
        SlashCommandData commandData = Commands.slash(command.getName(), command.getDescription());
        
        // Handle subcommands
        if (!command.getSubcommands().isEmpty()) {
            for (Command subcommand : command.getSubcommands()) {
                SubcommandData subcommandData = new SubcommandData(
                        subcommand.getName(), subcommand.getDescription());
                
                // Add options to the subcommand
                for (CommandOption<?> option : subcommand.getOptions()) {
                    addOptionToSubcommand(subcommandData, option);
                }
                
                commandData.addSubcommands(subcommandData);
            }
        } else {
            // Add options to the command
            for (CommandOption<?> option : command.getOptions()) {
                addOptionToCommand(commandData, option);
            }
        }
        
        return commandData;
    }
    
    /**
     * Adds an option to a command.
     *
     * @param commandData the command data
     * @param option     the option
     */
    private void addOptionToCommand(SlashCommandData commandData, CommandOption<?> option) {
        OptionData optionData = new OptionData(
                convertOptionType(option.getType()),
                option.getName(),
                option.getDescription(),
                option.isRequired(),
                option.isAutoComplete());
        
        // Add choices if available
        for (OptionChoice<?> choice : option.getChoices()) {
            addChoiceToOption(optionData, choice);
        }
        
        // Add min and max values for number options
        if (option.getType() == OptionType2.INTEGER || option.getType() == OptionType2.NUMBER) {
            optionData.setMinValue(option.getMinValue());
            optionData.setMaxValue(option.getMaxValue());
        }
        
        commandData.addOptions(optionData);
    }
    
    /**
     * Adds an option to a subcommand.
     *
     * @param subcommandData the subcommand data
     * @param option        the option
     */
    private void addOptionToSubcommand(SubcommandData subcommandData, CommandOption<?> option) {
        OptionData optionData = new OptionData(
                convertOptionType(option.getType()),
                option.getName(),
                option.getDescription(),
                option.isRequired(),
                option.isAutoComplete());
        
        // Add choices if available
        for (OptionChoice<?> choice : option.getChoices()) {
            addChoiceToOption(optionData, choice);
        }
        
        // Add min and max values for number options
        if (option.getType() == OptionType2.INTEGER || option.getType() == OptionType2.NUMBER) {
            optionData.setMinValue(option.getMinValue());
            optionData.setMaxValue(option.getMaxValue());
        }
        
        subcommandData.addOptions(optionData);
    }
    
    /**
     * Adds a choice to an option.
     *
     * @param optionData the option data
     * @param choice    the choice
     */
    @SuppressWarnings("unchecked")
    private void addChoiceToOption(OptionData optionData, OptionChoice<?> choice) {
        Object value = choice.getValue();
        
        if (value instanceof String) {
            optionData.addChoice(choice.getName(), (String) value);
        } else if (value instanceof Integer || value instanceof Long) {
            optionData.addChoice(choice.getName(), ((Number) value).longValue());
        } else if (value instanceof Float || value instanceof Double) {
            optionData.addChoice(choice.getName(), ((Number) value).doubleValue());
        }
    }
    
    /**
     * Converts an option type to a JDA option type.
     *
     * @param type the option type
     * @return the JDA option type
     */
    private OptionType2 convertOptionType(OptionType2 type) {
        return switch (type) {
            case STRING -> OptionType2.STRING;
            case INTEGER -> OptionType2.INTEGER;
            case BOOLEAN -> OptionType2.BOOLEAN;
            case USER -> OptionType2.USER;
            case CHANNEL -> OptionType2.CHANNEL;
            case ROLE -> OptionType2.ROLE;
            case MENTIONABLE -> OptionType2.MENTIONABLE;
            case NUMBER -> OptionType2.NUMBER;
            case ATTACHMENT -> OptionType2.ATTACHMENT;
            default -> OptionType2.STRING;
        };
    }
}

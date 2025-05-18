package fr.farmvivi.discordbot.core.command.system;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandBuilder;
import fr.farmvivi.discordbot.core.api.command.CommandContext;
import fr.farmvivi.discordbot.core.api.command.CommandResult;
import fr.farmvivi.discordbot.core.api.command.CommandService;
import fr.farmvivi.discordbot.core.api.command.option.OptionChoice;
import fr.farmvivi.discordbot.core.command.SimpleCommandBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * System command that displays help information about commands.
 */
public class HelpCommand {
    
    private final CommandService commandService;
    private final Command command;
    
    /**
     * Creates a new help command.
     *
     * @param commandService the command service
     */
    public HelpCommand(CommandService commandService) {
        this.commandService = commandService;
        
        CommandBuilder builder = new SimpleCommandBuilder()
                .name("help")
                .description("Shows information about available commands")
                .category("System")
                .aliases("?")
                .stringOption("command", "The command to get help for", false)
                .stringOption("category", "The category to get help for", false, this::provideCategories)
                .executor(this::execute);
        
        this.command = builder.build();
    }
    
    /**
     * Gets the command instance.
     *
     * @return the command
     */
    public Command getCommand() {
        return command;
    }
    
    /**
     * Provides a list of available categories for autocompletion.
     *
     * @param input the input string
     * @return a list of category choices
     */
    private List<OptionChoice<String>> provideCategories(String input) {
        String lowercaseInput = input.toLowerCase();
        
        return commandService.getRegistry().getCategories().stream()
                .filter(category -> category.toLowerCase().contains(lowercaseInput))
                .map(category -> OptionChoice.of(category, category))
                .limit(25)
                .collect(Collectors.toList());
    }
    
    /**
     * Executes the help command.
     *
     * @param context the command context
     * @param command the command
     * @return the command result
     */
    private CommandResult execute(CommandContext context, Command command) {
        // Check if we're getting help for a specific command
        if (context.hasOption("command")) {
            String commandName = context.getOption("command", "");
            return showCommandHelp(context, commandName);
        }
        
        // Check if we're getting help for a specific category
        if (context.hasOption("category")) {
            String category = context.getOption("category", "");
            return showCategoryHelp(context, category);
        }
        
        // Otherwise, show general help
        return showGeneralHelp(context);
    }
    
    /**
     * Shows help for a specific command.
     *
     * @param context the command context
     * @param commandName the command name
     * @return the command result
     */
    private CommandResult showCommandHelp(CommandContext context, String commandName) {
        // Find the command
        Command targetCommand = commandService.getRegistry().getCommand(commandName)
                .orElseGet(() -> commandService.getRegistry().getCommandByAlias(commandName).orElse(null));
        
        if (targetCommand == null) {
            context.replyError("Command not found: " + commandName);
            return CommandResult.error("Command not found");
        }
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Command: " + targetCommand.getName())
                .setDescription(targetCommand.getDescription());
        
        // Add usage
        StringBuilder usage = new StringBuilder();
        usage.append("**Usage:** ");
        
        if (context.getOriginalEvent() instanceof net.dv8tion.jda.api.events.message.MessageReceivedEvent) {
            usage.append("`").append(commandService.getPrefix());
            if (context.isFromGuild()) {
                usage.append(commandService.getPrefix(context.getGuild().get().getId()));
            }
            usage.append(targetCommand.getName());
        } else {
            usage.append("`/").append(targetCommand.getName());
        }
        
        if (!targetCommand.getOptions().isEmpty()) {
            for (var option : targetCommand.getOptions()) {
                if (option.isRequired()) {
                    usage.append(" <").append(option.getName()).append(">");
                } else {
                    usage.append(" [").append(option.getName()).append("]");
                }
            }
        }
        
        usage.append("`");
        embed.addField("Usage", usage.toString(), false);
        
        // Add category
        embed.addField("Category", targetCommand.getCategory(), true);
        
        // Add aliases if any
        if (!targetCommand.getAliases().isEmpty()) {
            embed.addField("Aliases", String.join(", ", targetCommand.getAliases()), true);
        }
        
        // Add permissions if any
        if (targetCommand.getPermission() != null) {
            embed.addField("Permission", targetCommand.getPermission(), true);
        }
        
        // Add options if any
        if (!targetCommand.getOptions().isEmpty()) {
            StringBuilder optionsStr = new StringBuilder();
            for (var option : targetCommand.getOptions()) {
                optionsStr.append("**").append(option.getName()).append("**");
                if (option.isRequired()) {
                    optionsStr.append(" (required)");
                }
                optionsStr.append(": ").append(option.getDescription()).append("\n");
            }
            embed.addField("Options", optionsStr.toString(), false);
        }
        
        // Add subcommands if any
        if (!targetCommand.getSubcommands().isEmpty()) {
            StringBuilder subcommandsStr = new StringBuilder();
            for (var subcommand : targetCommand.getSubcommands()) {
                subcommandsStr.append("**").append(subcommand.getName()).append("**")
                        .append(": ").append(subcommand.getDescription()).append("\n");
            }
            embed.addField("Subcommands", subcommandsStr.toString(), false);
        }
        
        context.replyEmbed(embed);
        return CommandResult.success();
    }
    
    /**
     * Shows help for a specific category.
     *
     * @param context the command context
     * @param category the category
     * @return the command result
     */
    private CommandResult showCategoryHelp(CommandContext context, String category) {
        List<Command> commands = commandService.getRegistry().getCommandsByCategory(category);
        
        if (commands.isEmpty()) {
            context.replyError("Category not found: " + category);
            return CommandResult.error("Category not found");
        }
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Category: " + category)
                .setDescription("Commands in this category:");
        
        commands.stream()
                .sorted(Comparator.comparing(Command::getName))
                .forEach(cmd -> embed.addField(cmd.getName(), cmd.getDescription(), false));
        
        context.replyEmbed(embed);
        return CommandResult.success();
    }
    
    /**
     * Shows general help with all categories and commands.
     *
     * @param context the command context
     * @return the command result
     */
    private CommandResult showGeneralHelp(CommandContext context) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Available Commands")
                .setDescription("Type `/help <command>` or `/help <category>` for more details.");
        
        // Group commands by category
        Map<String, List<Command>> commandsByCategory = commandService.getRegistry().getCommands().stream()
                .filter(Command::isEnabled)
                .collect(Collectors.groupingBy(Command::getCategory));
        
        // Sort categories and add them to the embed
        List<String> categories = new ArrayList<>(commandsByCategory.keySet());
        categories.sort(String::compareToIgnoreCase);
        
        for (String category : categories) {
            List<Command> commands = commandsByCategory.get(category);
            
            StringBuilder value = new StringBuilder();
            for (Command cmd : commands.stream().sorted(Comparator.comparing(Command::getName)).toList()) {
                value.append("`").append(cmd.getName()).append("` - ").append(cmd.getDescription()).append("\n");
            }
            
            embed.addField(category, value.toString(), false);
            
            // Check if we need to split the embed due to Discord's limits
            if (embed.getFields().size() >= MessageEmbed.MAX_FIELD_AMOUNT ||
                    embed.length() >= MessageEmbed.EMBED_MAX_LENGTH_BOT - 100) {
                
                // Send the current embed
                context.replyEmbed(embed);
                
                // Create a new embed for the next categories
                embed = new EmbedBuilder()
                        .setTitle("Available Commands (Continued)");
            }
        }
        
        // Send the final embed
        if (!embed.getFields().isEmpty()) {
            context.replyEmbed(embed);
        }
        
        return CommandResult.success();
    }
}

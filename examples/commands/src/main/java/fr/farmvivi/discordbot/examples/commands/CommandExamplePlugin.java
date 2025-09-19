package fr.farmvivi.discordbot.examples.commands;

import fr.farmvivi.discordbot.api.command.*;
import fr.farmvivi.discordbot.api.command.option.OptionChoice;
import fr.farmvivi.discordbot.api.command.option.OptionType;
import fr.farmvivi.discordbot.api.event.EventHandler;
import fr.farmvivi.discordbot.api.event.EventPriority;
import fr.farmvivi.discordbot.api.plugin.AbstractPlugin;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Example plugin demonstrating comprehensive command system usage.
 * Shows slash commands, text commands, options, permissions, cooldowns, and more.
 */
public class CommandExamplePlugin extends AbstractPlugin {

    @Override
    public void onEnable() {
        registerCommands();
        logger.info("Command Example Plugin enabled with comprehensive command examples!");
    }

    @Override
    public void onDisable() {
        logger.info("Command Example Plugin disabled!");
    }

    /**
     * Register all example commands demonstrating different features
     */
    private void registerCommands() {
        // Basic command
        registerBasicCommand();
        
        // Command with options
        registerCommandWithOptions();
        
        // Command with permissions
        registerPermissionCommand();
        
        // Command with cooldown
        registerCooldownCommand();
        
        // Subcommands
        registerSubcommands();
        
        // Command with autocomplete
        registerAutocompleteCommand();
        
        // Guild-only command
        registerGuildOnlyCommand();
        
        // Ephemeral response command
        registerEphemeralCommand();
    }

    /**
     * Basic command example
     */
    private void registerBasicCommand() {
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("hello")
                .description("Say hello!")
                .execute(context -> {
                    context.reply("Hello, " + context.getUser().getAsMention() + "!");
                    return CommandResult.SUCCESS;
                })
        );
    }

    /**
     * Command with various option types
     */
    private void registerCommandWithOptions() {
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("options")
                .description("Demo command with various option types")
                .stringOption("text", "Enter some text", true)
                .integerOption("number", "Enter a number", false)
                .booleanOption("flag", "True or false", false)
                .userOption("user", "Select a user", false)
                .channelOption("channel", "Select a channel", false)
                .roleOption("role", "Select a role", false)
                .attachmentOption("file", "Upload a file", false)
                .execute(context -> {
                    StringBuilder response = new StringBuilder("Options received:\n");
                    
                    String text = context.getStringOption("text");
                    response.append("📝 Text: ").append(text).append("\n");
                    
                    context.getIntegerOption("number").ifPresent(num -> 
                        response.append("🔢 Number: ").append(num).append("\n")
                    );
                    
                    context.getBooleanOption("flag").ifPresent(flag -> 
                        response.append("🚩 Flag: ").append(flag).append("\n")
                    );
                    
                    context.getUserOption("user").ifPresent(user -> 
                        response.append("👤 User: ").append(user.getAsMention()).append("\n")
                    );
                    
                    context.getChannelOption("channel").ifPresent(channel -> 
                        response.append("📺 Channel: ").append(channel.getAsMention()).append("\n")
                    );
                    
                    context.getRoleOption("role").ifPresent(role -> 
                        response.append("🎭 Role: ").append(role.getAsMention()).append("\n")
                    );
                    
                    context.getAttachmentOption("file").ifPresent(attachment -> 
                        response.append("📎 File: ").append(attachment.getFileName()).append("\n")
                    );
                    
                    context.reply(response.toString());
                    return CommandResult.SUCCESS;
                })
        );
    }

    /**
     * Command with permissions
     */
    private void registerPermissionCommand() {
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("admin")
                .description("Admin-only command")
                .permission("commandexample.admin")
                .execute(context -> {
                    context.reply("🛡️ You have admin permissions! Welcome to the admin panel.");
                    return CommandResult.SUCCESS;
                })
        );
    }

    /**
     * Command with cooldown
     */
    private void registerCooldownCommand() {
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("cooldown")
                .description("Command with 10 second cooldown")
                .cooldown(10, TimeUnit.SECONDS)
                .execute(context -> {
                    context.reply("⏰ Command executed! You must wait 10 seconds before using it again.");
                    return CommandResult.SUCCESS;
                })
        );
    }

    /**
     * Command with subcommands
     */
    private void registerSubcommands() {
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("manage")
                .description("Management commands")
                .subcommand("user", "User management")
                    .userOption("target", "Target user", true)
                    .stringOption("action", "Action to perform", true)
                        .choices(
                            OptionChoice.of("kick", "kick"),
                            OptionChoice.of("ban", "ban"),
                            OptionChoice.of("mute", "mute")
                        )
                    .execute(context -> {
                        User target = context.getUserOption("target").orElse(null);
                        String action = context.getStringOption("action");
                        
                        if (target == null) {
                            context.reply("❌ Target user not found!");
                            return CommandResult.ERROR;
                        }
                        
                        context.reply("⚡ Would perform action **" + action + "** on " + target.getAsMention() + 
                                     " (this is just a demo!)");
                        return CommandResult.SUCCESS;
                    })
                .parent()
                .subcommand("server", "Server management")
                    .stringOption("setting", "Setting to change", true)
                        .choices(
                            OptionChoice.of("Welcome Messages", "welcome"),
                            OptionChoice.of("Auto Moderation", "automod"),
                            OptionChoice.of("Logging", "logging")
                        )
                    .booleanOption("enabled", "Enable or disable", true)
                    .execute(context -> {
                        String setting = context.getStringOption("setting");
                        boolean enabled = context.getBooleanOption("enabled").orElse(false);
                        
                        context.reply("🛠️ Would " + (enabled ? "enable" : "disable") + 
                                     " **" + setting + "** (this is just a demo!)");
                        return CommandResult.SUCCESS;
                    })
        );
    }

    /**
     * Command with autocomplete
     */
    private void registerAutocompleteCommand() {
        List<String> programmingLanguages = Arrays.asList(
            "Java", "JavaScript", "Python", "C#", "C++", "Go", "Rust", "TypeScript", "PHP", "Ruby"
        );

        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("code")
                .description("Get information about a programming language")
                .stringOption("language", "Programming language", true, 
                    input -> programmingLanguages.stream()
                        .filter(lang -> lang.toLowerCase().contains(input.toLowerCase()))
                        .limit(25)
                        .map(lang -> OptionChoice.of(lang, lang.toLowerCase()))
                        .collect(Collectors.toList())
                )
                .execute(context -> {
                    String language = context.getStringOption("language");
                    context.reply("💻 You selected: **" + language + "**\n" +
                                 "Here would be information about " + language + " programming language!");
                    return CommandResult.SUCCESS;
                })
        );
    }

    /**
     * Guild-only command
     */
    private void registerGuildOnlyCommand() {
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("guildinfo")
                .description("Get information about this server")
                .guildOnly(true)
                .execute(context -> {
                    if (context.getGuild() == null) {
                        context.reply("❌ This command can only be used in a server!");
                        return CommandResult.ERROR;
                    }
                    
                    var guild = context.getGuild();
                    context.reply(String.format(
                        "🏰 **%s**\n" +
                        "👥 Members: %d\n" +
                        "📅 Created: %s\n" +
                        "👑 Owner: %s",
                        guild.getName(),
                        guild.getMemberCount(),
                        guild.getTimeCreated().toLocalDate(),
                        guild.getOwner() != null ? guild.getOwner().getAsMention() : "Unknown"
                    ));
                    return CommandResult.SUCCESS;
                })
        );
    }

    /**
     * Command with ephemeral response
     */
    private void registerEphemeralCommand() {
        getPluginCommandAdapter().registerCommand(
            commandService.newCommand()
                .name("secret")
                .description("Send a secret message only you can see")
                .stringOption("message", "Your secret message", true)
                .execute(context -> {
                    String message = context.getStringOption("message");
                    context.replyEphemeral("🤫 Your secret message: **" + message + "**\n" +
                                          "Only you can see this message!");
                    return CommandResult.SUCCESS;
                })
        );
    }

    /**
     * Example text command handler for legacy support
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMessage(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        
        String content = event.getMessage().getContentRaw();
        String prefix = commandService.getPrefix();
        
        if (content.equals(prefix + "textcmd")) {
            event.getChannel().sendMessage("📜 Text command still works! " +
                                          "But slash commands are recommended.").queue();
        }
    }
}
package fr.farmvivi.discordbot.core.command;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandContext;
import fr.farmvivi.discordbot.core.api.command.CommandResult;
import fr.farmvivi.discordbot.core.api.command.CommandService;
import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.api.permissions.PermissionManager;
import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.storage.DataStorageManager;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the command system.
 */
public class CommandSystemTest {

    @Mock
    private EventManager eventManager;
    
    @Mock
    private LanguageManager languageManager;
    
    @Mock
    private PermissionManager permissionManager;
    
    @Mock
    private Configuration configuration;
    
    @Mock
    private DataStorageManager storageManager;

    private CommandService commandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        commandService = new SimpleCommandService(
                eventManager,
                languageManager,
                permissionManager,
                configuration,
                storageManager,
                "!"
        );
    }

    @Test
    void testCommandRegistration() {
        // Create a simple test command
        Command testCommand = commandService.newCommand()
                .name("test")
                .description("A test command")
                .category("Test")
                .executor((context, cmd) -> {
                    context.reply("Test successful!");
                    return CommandResult.success();
                })
                .build();

        // Register the command
        boolean registered = commandService.registerCommand(testCommand);
        assertTrue(registered, "Command should be registered successfully");

        // Verify the command is in the registry
        assertTrue(commandService.getRegistry().getCommand("test").isPresent(), 
                "Command should be found in registry");
    }

    @Test
    void testCommandBuilder() {
        // Test the command builder
        Command command = commandService.newCommand()
                .name("ping")
                .description("Ping the bot")
                .category("Utility")
                .aliases("p", "pong")
                .guildOnly(false)
                .cooldown(5)
                .stringOption("message", "A message to echo", false)
                .executor((context, cmd) -> {
                    String message = context.getOption("message", "Pong!");
                    context.reply(message);
                    return CommandResult.success();
                })
                .build();

        // Verify command properties
        assertEquals("ping", command.getName());
        assertEquals("Ping the bot", command.getDescription());
        assertEquals("Utility", command.getCategory());
        assertTrue(command.getAliases().contains("p"));
        assertTrue(command.getAliases().contains("pong"));
        assertFalse(command.isGuildOnly());
        assertEquals(5, command.getCooldown());
        assertEquals(1, command.getOptions().size());
        assertEquals("message", command.getOptions().get(0).getName());
    }

    @Test
    void testSubcommandRegistration() {
        // Create a command with subcommands
        Command command = commandService.newCommand()
                .name("admin")
                .description("Administrative commands")
                .category("Admin")
                .subcommand(sub -> {
                    sub.name("kick")
                       .description("Kick a user")
                       .userOption("user", "The user to kick", true)
                       .stringOption("reason", "Reason for kick", false)
                       .executor((context, cmd) -> {
                           context.replySuccess("User kicked successfully!");
                           return CommandResult.success();
                       });
                })
                .subcommand(sub -> {
                    sub.name("ban")
                       .description("Ban a user")
                       .userOption("user", "The user to ban", true)
                       .stringOption("reason", "Reason for ban", false)
                       .executor((context, cmd) -> {
                           context.replySuccess("User banned successfully!");
                           return CommandResult.success();
                       });
                })
                .build();

        // Verify command properties
        assertEquals("admin", command.getName());
        assertEquals(2, command.getSubcommands().size());
        
        // Verify subcommands
        Command kickCommand = command.getSubcommands().stream()
                .filter(cmd -> cmd.getName().equals("kick"))
                .findFirst()
                .orElse(null);
        assertNotNull(kickCommand, "Kick subcommand should exist");
        assertEquals(2, kickCommand.getOptions().size());

        Command banCommand = command.getSubcommands().stream()
                .filter(cmd -> cmd.getName().equals("ban"))
                .findFirst()
                .orElse(null);
        assertNotNull(banCommand, "Ban subcommand should exist");
        assertEquals(2, banCommand.getOptions().size());
    }

    @Test
    void testCommandRegistry() {
        // Create and register multiple commands
        Command command1 = commandService.newCommand()
                .name("test1")
                .description("Test command 1")
                .category("Test")
                .executor((context, cmd) -> CommandResult.success())
                .build();

        Command command2 = commandService.newCommand()
                .name("test2")
                .description("Test command 2")
                .category("Test")
                .executor((context, cmd) -> CommandResult.success())
                .build();

        commandService.registerCommand(command1);
        commandService.registerCommand(command2);

        // Test registry methods
        assertEquals(2, commandService.getRegistry().getCommands().size());
        assertTrue(commandService.getRegistry().getCommand("test1").isPresent());
        assertTrue(commandService.getRegistry().getCommand("test2").isPresent());
        
        // Test categories
        assertTrue(commandService.getRegistry().getCategories().contains("test"));
        assertEquals(2, commandService.getRegistry().getCommandsByCategory("Test").size());
    }
}
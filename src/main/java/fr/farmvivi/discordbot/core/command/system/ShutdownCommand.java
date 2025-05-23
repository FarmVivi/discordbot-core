package fr.farmvivi.discordbot.core.command.system;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandContext;
import fr.farmvivi.discordbot.core.api.command.CommandResult;
import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.command.SimpleCommandBuilder;

/**
 * System command that shuts down the bot.
 * This command is only available to administrators.
 */
public class ShutdownCommand {

    private final Command command;
    private final LanguageManager languageManager;

    /**
     * Creates a new shutdown command with a language manager.
     *
     * @param languageManager the language manager for translations
     */
    public ShutdownCommand(LanguageManager languageManager) {
        this.languageManager = languageManager;

        command = new SimpleCommandBuilder()
                .name("shutdown")
                .description("Shuts down the bot")
                .category("System")
                .aliases("stop", "exit", "quit")
                .permission("discobocor.admin.shutdown")
                .booleanOption("restart", "Whether to restart the bot after shutdown", false)
                .executor(this::execute)
                .build();
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
     * Executes the shutdown command.
     *
     * @param context the command context
     * @param command the command
     * @return the command result
     */
    private CommandResult execute(CommandContext context, Command command) {
        boolean restart = context.getOption("restart", false);

        if (restart) {
            // Get the restart message using the language manager if available
            String restartMessage = languageManager.getString(context.getLocale(), "commands.shutdown.restarting");

            context.replyInfo(restartMessage);

            // Schedule a delayed task to restart the bot
            Thread restartThread = new Thread(() -> {
                try {
                    Thread.sleep(2000); // Give time for the message to be sent
                    System.exit(3); // Exit code 3 can be used by the wrapper script to restart
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            restartThread.setDaemon(true);
            restartThread.start();
        } else {
            // Get the shutdown message using the language manager if available
            String shutdownMessage = languageManager.getString(context.getLocale(), "commands.shutdown.shutting_down");

            context.replyInfo(shutdownMessage);

            // Schedule a delayed task to shut down the bot
            Thread shutdownThread = new Thread(() -> {
                try {
                    Thread.sleep(2000); // Give time for the message to be sent
                    System.exit(0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            shutdownThread.setDaemon(true);
            shutdownThread.start();
        }

        return CommandResult.success();
    }
}

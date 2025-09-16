package fr.farmvivi.discordbot.core.command.system;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandContext;
import fr.farmvivi.discordbot.core.api.command.CommandResult;
import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.command.SimpleCommandBuilder;
import fr.farmvivi.discordbot.core.util.DiscordColor;
import net.dv8tion.jda.api.EmbedBuilder;

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
        // For slash commands, defer reply as ephemeral immediately to avoid timeout and ensure ephemeral responses
        if (context.getOriginalEvent() instanceof net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent slashEvent && !slashEvent.isAcknowledged()) {
            context.deferReply(true);
        } else {
            // For non-slash commands, set ephemeral for direct replies
            context.setEphemeral(true);
        }
        
        boolean restart = context.getOption("restart", false);

        if (restart) {
            // Get the restart message using the language manager if available
            String restartMessage = languageManager.getString(context.getLocale(), "commands.shutdown.restarting");

            // Create embed for restart message
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(DiscordColor.DISCORD_BLURPLE.getColor())
                    .setTitle(languageManager.getString(context.getLocale(), "commands.titles.info"))
                    .setDescription(restartMessage);
            
            context.replyEmbed(embed);

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

            // Create embed for shutdown message
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(DiscordColor.DISCORD_BLURPLE.getColor())
                    .setTitle(languageManager.getString(context.getLocale(), "commands.titles.info"))
                    .setDescription(shutdownMessage);
            
            context.replyEmbed(embed);

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

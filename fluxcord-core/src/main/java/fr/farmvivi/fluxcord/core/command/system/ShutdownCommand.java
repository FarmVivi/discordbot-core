package fr.farmvivi.fluxcord.core.command.system;

import fr.farmvivi.fluxcord.api.command.Command;
import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.api.language.LanguageManager;
import fr.farmvivi.fluxcord.core.command.SimpleCommandBuilder;
import fr.farmvivi.fluxcord.core.util.DiscordColor;
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
        // Réponses administratives en éphémère
        context.setEphemeral(true);

        String shutdownMessage = languageManager.getString(context.getLocale(), "commands.shutdown.shutting_down");

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(DiscordColor.DISCORD_BLURPLE.getColor())
                .setTitle(languageManager.getString(context.getLocale(), "commands.titles.info"))
                .setDescription(shutdownMessage);

        context.replyEmbed(embed);

        Thread shutdownThread = new Thread(() -> System.exit(0));

        shutdownThread.setDaemon(true);
        shutdownThread.start();

        return CommandResult.success();
    }
}

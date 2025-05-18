package fr.farmvivi.discordbot.core.command.system;

import fr.farmvivi.discordbot.core.Discobocor;
import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandContext;
import fr.farmvivi.discordbot.core.api.command.CommandResult;
import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.command.SimpleCommandBuilder;
import fr.farmvivi.discordbot.core.util.DiscordColor;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * System command that displays version information about the bot.
 */
public class VersionCommand {

    private final Command command;
    private final LanguageManager languageManager;

    /**
     * Creates a new version command with a language manager.
     *
     * @param languageManager the language manager for translations
     */
    public VersionCommand(LanguageManager languageManager) {
        this.languageManager = languageManager;

        command = new SimpleCommandBuilder()
                .name("version")
                .description("Shows the bot version")
                .category("System")
                .aliases("ver", "v", "about")
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
     * Executes the version command.
     *
     * @param context the command context
     * @param command the command
     * @return the command result
     */
    private CommandResult execute(CommandContext context, Command command) {
        EmbedBuilder embed = new EmbedBuilder().setColor(DiscordColor.DISCORD_BLURPLE.getColor());

        embed.setTitle(languageManager.getString(context.getLocale(), "commands.version.title"))
                .addField(languageManager.getString(context.getLocale(), "commands.version.name"), Discobocor.NAME, true)
                .addField(languageManager.getString(context.getLocale(), "commands.version.version"), Discobocor.VERSION, true)
                .addField(languageManager.getString(context.getLocale(), "commands.version.mode"),
                        Discobocor.PRODUCTION
                                ? languageManager.getString(context.getLocale(), "commands.version.production")
                                : languageManager.getString(context.getLocale(), "commands.version.development"),
                        true)
                .addField(languageManager.getString(context.getLocale(), "commands.version.java_version"), System.getProperty("java.version"), true)
                .addField(languageManager.getString(context.getLocale(), "commands.version.jvm"),
                        System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version"), true)
                .addField(languageManager.getString(context.getLocale(), "commands.version.os"),
                        System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")", true);

        if (context.getJDA() != null) {
            embed.addField(languageManager.getString(context.getLocale(), "commands.version.gateway_ping"),
                    context.getJDA().getGatewayPing() + "ms", true);

            if (context.getJDA().getShardInfo() != null) {
                embed.addField(languageManager.getString(context.getLocale(), "commands.version.shard"),
                        context.getJDA().getShardInfo().getShardId() + "/" + context.getJDA().getShardInfo().getShardTotal(), true);
            }
        }

        context.replyEmbed(embed);
        return CommandResult.success();
    }
}

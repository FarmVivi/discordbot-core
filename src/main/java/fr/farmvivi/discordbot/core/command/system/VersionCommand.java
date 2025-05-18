package fr.farmvivi.discordbot.core.command.system;

import fr.farmvivi.discordbot.core.Discobocor;
import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandContext;
import fr.farmvivi.discordbot.core.api.command.CommandResult;
import fr.farmvivi.discordbot.core.command.SimpleCommandBuilder;
import fr.farmvivi.discordbot.core.util.DiscordColor;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * System command that displays version information about the bot.
 */
public class VersionCommand {

    private final Command command;

    /**
     * Creates a new version command.
     */
    public VersionCommand() {
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
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Bot Information")
                .setColor(DiscordColor.DISCORD_BLURPLE.getColor()) // Discord blurple
                .addField("Name", Discobocor.NAME, true)
                .addField("Version", Discobocor.VERSION, true)
                .addField("Mode", Discobocor.PRODUCTION ? "Production" : "Development", true)
                .addField("Java Version", System.getProperty("java.version"), true)
                .addField("JVM", System.getProperty("java.vm.name") + " " +
                        System.getProperty("java.vm.version"), true)
                .addField("OS", System.getProperty("os.name") + " " +
                        System.getProperty("os.version") + " (" +
                        System.getProperty("os.arch") + ")", true);

        if (context.getJDA() != null) {
            embed.addField("Gateway Ping", context.getJDA().getGatewayPing() + "ms", true);

            if (context.getJDA().getShardInfo() != null) {
                embed.addField("Shard", context.getJDA().getShardInfo().getShardId() + "/" +
                        context.getJDA().getShardInfo().getShardTotal(), true);
            }
        }

        context.replyEmbed(embed);
        return CommandResult.success();
    }
}

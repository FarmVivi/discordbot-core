package fr.farmvivi.discordbot.module.commands.command;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class VersionCommand extends Command {
    public VersionCommand() {
        super("version", CommandCategory.OTHER, "Affiche la version du bot");

        this.setGuildOnly(false);
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        // Create embed
        EmbedBuilder embed = reply.createInfoEmbed();

        // Set title
        embed.setTitle("Version");

        // Bot version
        embed.addField(Bot.name, Bot.version, false);

        // JDA version
        embed.addField("JDA", JDAInfo.VERSION, false);

        // LavaPlayer version
        embed.addField("LavaPlayer", PlayerLibrary.VERSION, false);

        // Send embed
        reply.addEmbeds(embed.build());

        reply.setEphemeral(true);

        return true;
    }
}

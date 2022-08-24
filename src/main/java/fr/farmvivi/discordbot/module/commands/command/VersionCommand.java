package fr.farmvivi.discordbot.module.commands.command;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import net.dv8tion.jda.api.JDAInfo;

public class VersionCommand extends Command {
    public VersionCommand() {
        super("version", CommandCategory.OTHER, "Affiche la version du bot");

        this.guildOnly = false;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content, CommandMessageBuilder reply) {
        if (!super.execute(event, content, reply))
            return false;

        reply.addContent(Bot.name + " **v" + Bot.version + "** :"
                + "\n-> JDA **v" + JDAInfo.VERSION + "**"
                + "\n-> LavaPlayer **v" + PlayerLibrary.VERSION + "**");

        reply.setEphemeral(true);

        return true;
    }
}

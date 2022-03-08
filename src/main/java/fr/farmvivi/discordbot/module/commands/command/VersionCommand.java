package fr.farmvivi.discordbot.module.commands.command;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class VersionCommand extends Command {
    public VersionCommand() {
        this.name = "version";
        this.category = CommandCategory.OTHER;
        this.description = "Affiche la version du bot";
        this.guildOnly = false;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        event.getChannel().sendMessage(Bot.name + " **v" + Bot.version + "** :"
                + "\n-> JDA **v" + JDAInfo.VERSION + "**"
                + "\n-> LavaPlayer **v" + PlayerLibrary.VERSION + "**")
                .queue();

        return true;
    }
}

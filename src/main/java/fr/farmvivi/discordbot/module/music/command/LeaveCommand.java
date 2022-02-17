package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LeaveCommand extends Command {
    public LeaveCommand() {
        this.name = "leave";
        this.aliases = new String[] { "disconnect" };
        this.category = CommandCategory.MUSIC;
        this.description = "Déconnecte le bot du channel";
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        TextChannel textChannel = event.getTextChannel();
        Guild guild = textChannel.getGuild();

        if (!guild.getAudioManager().isConnected()) {
            textChannel.sendMessage("Le bot n'est pas connecté à un salon vocal.").queue();
            return false;
        }

        guild.getAudioManager().closeAudioConnection();

        return true;
    }
}

package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class LeaveCommand extends Command {
    public LeaveCommand() {
        super("leave", CommandCategory.MUSIC, "Déconnecte le bot du channel", new String[]{"disconnect", "quit"});
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        TextChannel textChannel = event.getChannel().asTextChannel();
        Guild guild = textChannel.getGuild();

        if (!guild.getAudioManager().isConnected()) {
            textChannel.sendMessage("Le bot n'est pas connecté à un salon vocal.").queue();
            return false;
        }

        guild.getAudioManager().closeAudioConnection();

        return true;
    }
}

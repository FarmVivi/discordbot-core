package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class LeaveCommand extends Command {
    public LeaveCommand() {
        super("leave", CommandCategory.MUSIC, "Déconnecte le bot du salon audio", new String[]{"disconnect", "quit"});
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        Guild guild = event.getGuild();

        if (guild.getAudioManager().isConnected()) {
            guild.getAudioManager().closeAudioConnection();
            reply.addContent("Déconnecté.");
            return true;
        } else {
            reply.addContent("Le bot n'est pas connecté à un salon vocal.");
            return false;
        }
    }
}

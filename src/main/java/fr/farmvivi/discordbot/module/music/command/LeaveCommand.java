package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class LeaveCommand extends MusicCommand {
    public LeaveCommand(MusicModule musicModule) {
        super(musicModule, "leave", CommandCategory.MUSIC, "Déconnecte le bot du salon audio");

        this.setAliases(new String[]{"disconnect", "quit"});
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        Guild guild = event.getGuild();

        if (!guild.getAudioManager().isConnected()) {
            reply.error("Le bot n'est pas connecté à un salon vocal.");
            return false;
        }

        guild.getAudioManager().closeAudioConnection();
        reply.success("Déconnecté.");
        reply.setEphemeral(true);

        return true;
    }
}

package fr.farmvivi.discordbot.module.music.command.equalizer;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class EqStopCommand extends Command {
    private final MusicModule musicModule;

    public EqStopCommand(MusicModule musicModule) {
        super("eqstop", CommandCategory.MUSIC, "Désactive le modificateur audio");

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        Guild guild = event.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            reply.addContent("Aucune musique en cours de lecture.");
            return false;
        }

        musicModule.getPlayer(guild).getAudioPlayer().setFilterFactory(null);
        reply.addContent("**Equalizer** désactivé.");

        return true;
    }
}

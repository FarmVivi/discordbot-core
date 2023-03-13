package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class ShuffleCommand extends MusicCommand {
    public ShuffleCommand(MusicModule musicModule) {
        super(musicModule, "shuffle", CommandCategory.MUSIC, "Joue une musique aléatoire de la file d'attente");
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        Guild guild = event.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            reply.error("Aucune musique en cours de lecture.");
            return false;
        }

        if (musicModule.getPlayer(guild).isShuffleMode()) {
            musicModule.getPlayer(guild).setShuffleMode(false);
            reply.success("**Shuffle** désactivé.");
        } else {
            musicModule.getPlayer(guild).setShuffleMode(true);
            reply.success("**Shuffle** activé.");
        }

        reply.setEphemeral(true);

        return true;
    }
}

package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class NextCommand extends MusicCommand {
    public NextCommand(MusicModule musicModule) {
        super(musicModule, "next", CommandCategory.MUSIC, "Passe à la musique suivante (reste dans la file d'attente si loopqueue est activé)");
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

        AudioTrack track = musicModule.getPlayer(guild).nextTrack();
        if (track == null) {
            reply.addContent("Plus aucune musique à jouer.");
        } else {
            reply.addContent("Musique suivante: **" + track.getInfo().title + "**");
        }

        return true;
    }
}

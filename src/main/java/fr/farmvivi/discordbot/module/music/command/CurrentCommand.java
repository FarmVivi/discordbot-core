package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class CurrentCommand extends MusicCommand {
    public CurrentCommand(MusicModule musicModule) {
        super(musicModule, "current", CommandCategory.MUSIC, "Affiche la musique en cours de lecture");

        this.setAliases(new String[]{"np", "info"});
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

        AudioTrack track = musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack();
        reply.addContent("Musique en cours de lecture: **" + track.getInfo().title + "** | " + track.getInfo().uri);

        return true;
    }
}

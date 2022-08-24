package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;

public class CurrentCommand extends Command {
    private final MusicModule musicModule;

    public CurrentCommand(MusicModule musicModule) {
        super("current", CommandCategory.MUSIC, "Affiche la musique en cours de lecture", new String[]{"np", "info"});

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content, CommandMessageBuilder reply) {
        if (!super.execute(event, content, reply))
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

package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;

public class ReplayCommand extends Command {
    private final MusicModule musicModule;

    public ReplayCommand(MusicModule musicModule) {
        super("replay", CommandCategory.MUSIC, "Ajoute la musique en cours de lecture en haut de la file d'attente");

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

        AudioTrack currentTrack = musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack();
        if (currentTrack == null)
            return false;

        musicModule.getPlayer(guild).getListener().addTrackFirst(currentTrack.makeClone());
        reply.addContent("La piste **" + currentTrack.getInfo().title + "** va être rejoué");

        return true;
    }
}

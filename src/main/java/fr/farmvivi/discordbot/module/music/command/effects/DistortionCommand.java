package fr.farmvivi.discordbot.module.music.command.effects;

import com.github.natanbc.lavadsp.distortion.DistortionPcmAudioFilter;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import fr.farmvivi.discordbot.module.music.MusicPlayer;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Collections;

public class DistortionCommand extends Command {
    private final MusicModule musicModule;

    public DistortionCommand(MusicModule musicModule) {
        super("distortion", CommandCategory.MUSIC, "Active l'effet de distortion");

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

        MusicPlayer musicPlayer = musicModule.getPlayer(guild);
        musicPlayer.getAudioPlayer().setFilterFactory((track, format, output) -> {
            DistortionPcmAudioFilter distortionPcmAudioFilter = new DistortionPcmAudioFilter(output, format.channelCount);
            return Collections.singletonList(distortionPcmAudioFilter);
        });
        reply.addContent("**Effet de distortion** activé.");

        return true;
    }
}

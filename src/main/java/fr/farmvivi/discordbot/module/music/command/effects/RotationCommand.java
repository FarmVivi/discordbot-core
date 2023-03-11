package fr.farmvivi.discordbot.module.music.command.effects;

import com.github.natanbc.lavadsp.rotation.RotationPcmAudioFilter;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import fr.farmvivi.discordbot.module.music.MusicPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Collections;
import java.util.Map;

public class RotationCommand extends Command {
    private final MusicModule musicModule;

    public RotationCommand(MusicModule musicModule) {
        super("rotation", CommandCategory.MUSIC, "Active l'effet de rotation");

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

        MusicPlayer musicPlayer = musicModule.getPlayer(guild);
        musicPlayer.getAudioPlayer().setFilterFactory((track, format, output) -> {
            RotationPcmAudioFilter rotationPcmAudioFilter = new RotationPcmAudioFilter(output, format.sampleRate);
            rotationPcmAudioFilter.setRotationSpeed(0.1d);
            return Collections.singletonList(rotationPcmAudioFilter);
        });
        reply.addContent("**Effet de rotation** activé.");

        return true;
    }
}

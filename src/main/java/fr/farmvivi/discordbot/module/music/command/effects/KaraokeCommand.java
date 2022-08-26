package fr.farmvivi.discordbot.module.music.command.effects;

import com.github.natanbc.lavadsp.karaoke.KaraokePcmAudioFilter;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import fr.farmvivi.discordbot.module.music.MusicPlayer;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Collections;

public class KaraokeCommand extends Command {
    private final MusicModule musicModule;

    public KaraokeCommand(MusicModule musicModule) {
        super("karaoke", CommandCategory.MUSIC, "Active l'effet karaoke");

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
            KaraokePcmAudioFilter karaokePcmAudioFilter = new KaraokePcmAudioFilter(output, format.channelCount, format.sampleRate);
            return Collections.singletonList(karaokePcmAudioFilter);
        });
        reply.addContent("**Effet karaoke** activé.");

        return true;
    }
}

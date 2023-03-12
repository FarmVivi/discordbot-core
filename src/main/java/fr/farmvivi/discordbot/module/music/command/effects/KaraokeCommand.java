package fr.farmvivi.discordbot.module.music.command.effects;

import com.github.natanbc.lavadsp.karaoke.KaraokePcmAudioFilter;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import fr.farmvivi.discordbot.module.music.MusicPlayer;
import fr.farmvivi.discordbot.module.music.command.MusicCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Collections;
import java.util.Map;

public class KaraokeCommand extends MusicCommand {
    public KaraokeCommand(MusicModule musicModule) {
        super(musicModule, "karaoke", CommandCategory.MUSIC, "Active l'effet karaoke");
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
            KaraokePcmAudioFilter karaokePcmAudioFilter = new KaraokePcmAudioFilter(output, format.channelCount, format.sampleRate);
            return Collections.singletonList(karaokePcmAudioFilter);
        });
        reply.addContent("**Effet karaoke** activé.");

        return true;
    }
}

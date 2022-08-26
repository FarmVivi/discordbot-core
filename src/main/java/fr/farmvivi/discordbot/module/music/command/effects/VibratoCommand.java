package fr.farmvivi.discordbot.module.music.command.effects;

import com.github.natanbc.lavadsp.vibrato.VibratoPcmAudioFilter;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import fr.farmvivi.discordbot.module.music.MusicPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

public class VibratoCommand extends Command {
    private final MusicModule musicModule;

    public VibratoCommand(MusicModule musicModule) {
        super("vibrato", CommandCategory.MUSIC, "Active l'effet vibrato", new OptionData[]{
                new OptionData(OptionType.NUMBER, "depth", "Profondeur de l'effet")});

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
            VibratoPcmAudioFilter vibratoPcmAudioFilter = new VibratoPcmAudioFilter(output, format.channelCount, format.sampleRate);
            vibratoPcmAudioFilter.setDepth(Float.parseFloat(content));
            return Collections.singletonList(vibratoPcmAudioFilter);
        });
        reply.addContent("**Effet vibrato** " + content + " activé.");

        return true;
    }
}

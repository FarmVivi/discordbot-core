package fr.farmvivi.discordbot.module.music.command.effects;

import com.github.natanbc.lavadsp.vibrato.VibratoPcmAudioFilter;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import fr.farmvivi.discordbot.module.music.MusicPlayer;
import fr.farmvivi.discordbot.module.music.command.MusicCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.Map;

public class VibratoCommand extends MusicCommand {
    public VibratoCommand(MusicModule musicModule) {
        super(musicModule, "vibrato", CommandCategory.MUSIC, "Active l'effet vibrato");

        OptionData depthOption = new OptionData(OptionType.NUMBER, "depth", "Profondeur de l'effet", true);

        this.setArgs(new OptionData[]{depthOption});
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

        float depth = Float.parseFloat(args.get("depth").getAsString());

        MusicPlayer musicPlayer = musicModule.getPlayer(guild);
        musicPlayer.getAudioPlayer().setFilterFactory((track, format, output) -> {
            VibratoPcmAudioFilter vibratoPcmAudioFilter = new VibratoPcmAudioFilter(output, format.channelCount, format.sampleRate);
            vibratoPcmAudioFilter.setDepth(depth);
            return Collections.singletonList(vibratoPcmAudioFilter);
        });
        reply.addContent("**Effet vibrato** " + depth + " activé.");

        return true;
    }
}

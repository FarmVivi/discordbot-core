package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import fr.farmvivi.discordbot.module.music.utils.TimeToIntCalculator;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Map;

public class SeekCommand extends Command {
    private final MusicModule musicModule;

    public SeekCommand(MusicModule musicModule) {
        super("seek", CommandCategory.MUSIC, "Joue la musique a partir du temps donné", new OptionData[]{
                new OptionData(OptionType.INTEGER, "temps", "Temps à partir duquel lire la musique", true)});

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

        if (!musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack().isSeekable()) {
            reply.addContent("Cette piste n'est pas seekable.");
            return false;
        }

        String time = args.get("temps").getAsString();

        if (!TimeToIntCalculator.isFormatted(time)) {
            reply.addContent("Format de temps à utiliser: **jours:heures:minutes:secondes**");
            return false;
        }

        int startTime = TimeToIntCalculator.format(time) * 1000;
        AudioTrack currentTrack = musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack();
        if ((long) startTime > currentTrack.getDuration()) {
            reply.addContent("**" + args + "** > TrackDuration");
            return false;
        }

        musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack().setPosition(startTime);
        reply.addContent("Seek to **" + args + "**");

        return true;
    }
}

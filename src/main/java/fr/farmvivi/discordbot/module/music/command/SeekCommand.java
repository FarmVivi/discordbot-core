package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import fr.farmvivi.discordbot.module.music.utils.TimeParser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Map;

public class SeekCommand extends MusicCommand {
    public SeekCommand(MusicModule musicModule) {
        super(musicModule, "seek", CommandCategory.MUSIC, "Joue la musique a partir du temps donné");

        OptionData timeOption = new OptionData(OptionType.STRING, "temps", "Temps à partir duquel lire la musique", true);

        this.setArgs(new OptionData[]{timeOption});
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        Guild guild = event.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            reply.error("Aucune musique en cours de lecture.");
            return false;
        }

        if (!musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack().isSeekable()) {
            reply.error("Cette piste n'est pas seekable.");
            return false;
        }

        String time = args.get("temps").getAsString();

        if (!TimeParser.isFormatted(time)) {
            reply.error("Format de temps à utiliser: **jours:heures:minutes:secondes**");
            return false;
        }

        int startTime = TimeParser.convertStringToInt(time) * 1000;
        time = TimeParser.convertIntToString(startTime / 1000);
        AudioTrack currentTrack = musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack();
        if ((long) startTime > currentTrack.getDuration()) {
            reply.error("**" + time + "** > durée de la piste (**" + TimeParser.convertIntToString((int) currentTrack.getDuration() / 1000) + "**)");
            return false;
        }

        musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack().setPosition(startTime);
        musicModule.getPlayer(guild).getMusicPlayerMessage().refreshMessage();
        reply.success("Seek to **" + time + "**");

        reply.setEphemeral(true);

        return true;
    }
}

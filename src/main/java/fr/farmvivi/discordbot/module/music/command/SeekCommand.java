package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.Configuration;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import fr.farmvivi.discordbot.module.music.utils.TimeToIntCalculator;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class SeekCommand extends Command {
    private final MusicModule musicModule;
    private final Configuration botConfig;

    public SeekCommand(MusicModule musicModule, Configuration botConfig) {
        super("seek", CommandCategory.MUSIC, "Joue la musique a partir du temps donné", new OptionData[]{
                new OptionData(OptionType.INTEGER, "temps", "Temps à partir duquel lire la musique")});

        this.musicModule = musicModule;
        this.botConfig = botConfig;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content, CommandMessageBuilder reply) {
        if (!super.execute(event, content, reply))
            return false;
        if (this.getArgs().length > 0 && content.length() == 0) {
            reply.append("Utilisation de la commande: **")
                    .append(botConfig.cmdPrefix).append(this.getName()).append(" ").append(this.getArgsAsString()).append("**");
            return false;
        }

        Guild guild = event.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            reply.append("Aucune musique en cours de lecture.");
            return false;
        }

        if (!musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack().isSeekable()) {
            reply.append("Cette piste n'est pas seekable.");
            return false;
        }

        if (!TimeToIntCalculator.isFormatted(content)) {
            reply.append("Format de temps à utiliser: **jours:heures:minutes:secondes**");
            return false;
        }

        int startTime = TimeToIntCalculator.format(content) * 1000;
        AudioTrack currentTrack = musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack();
        if ((long) startTime > currentTrack.getDuration()) {
            reply.append("**").append(content).append("** > TrackDuration");
            return false;
        }

        musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack().setPosition(startTime);
        reply.append("Seek to **").append(content).append("**");

        return true;
    }
}

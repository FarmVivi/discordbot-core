package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.farmvivi.discordbot.Configuration;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.music.MusicModule;
import fr.farmvivi.discordbot.module.music.utils.TimeToIntCalculator;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SeekCommand extends Command {
    private final MusicModule musicModule;
    private final Configuration botConfig;

    public SeekCommand(MusicModule musicModule, Configuration botConfig) {
        this.name = "seek";
        this.category = CommandCategory.MUSIC;
        this.description = "Joue la musique a partir du temps donné";
        this.args = "<time>";

        this.musicModule = musicModule;
        this.botConfig = botConfig;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;
        if (args != null && content.length() == 0) {
            event.getChannel().sendMessage("Utilisation de la commande: **"
                    + botConfig.cmdPrefix + name + " " + args + "**").queue();
            return false;
        }

        TextChannel textChannel = event.getTextChannel();
        Guild guild = textChannel.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return false;
        }

        if (!musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack().isSeekable()) {
            textChannel.sendMessage("Cette piste n'est pas seekable.").queue();
            return false;
        }

        if (!TimeToIntCalculator.isFormated(content)) {
            textChannel.sendMessage("Format de temps à utiliser: **jours:heures:minutes:secondes**").queue();
            return false;
        }

        int startTime = TimeToIntCalculator.format(content) * 1000;
        AudioTrack currentTrack = musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack();
        if (Long.valueOf(startTime) > currentTrack.getDuration()) {
            textChannel.sendMessage("**" + content + "** > TrackDuration").queue();
            return false;
        }

        musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack().setPosition(Long.valueOf(startTime));
        textChannel.sendMessage("Seek to **" + content + "**").queue();

        return true;
    }
}

package fr.farmvivi.discordbot.command.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.command.Command;
import fr.farmvivi.discordbot.command.CommandCategory;
import fr.farmvivi.discordbot.music.MusicManager;
import fr.farmvivi.discordbot.otherclass.TimeToIntCalculator;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SeekCommand extends Command {
    public SeekCommand() {
        this.name = "seek";
        this.category = CommandCategory.MUSIC;
        this.description = "Joue la musique a partir du temps donné";
        this.args = "<time>";
    }

    @Override
    protected boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;
        if (args != null && content.length() == 0) {
            event.getChannel().sendMessage("Utilisation de la commande: **"
                    + Bot.getInstance().getConfiguration().cmdPrefix + name + " " + args + "**").queue();
            return false;
        }

        final TextChannel textChannel = event.getTextChannel();
        final Guild guild = textChannel.getGuild();
        final MusicManager musicManager = Bot.getInstance().getMusicManager();

        if (musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return false;
        }

        if (!musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack().isSeekable()) {
            textChannel.sendMessage("Cette piste n'est pas seekable.").queue();
            return false;
        }

        if (!TimeToIntCalculator.isFormated(content)) {
            textChannel.sendMessage("Format de temps à utiliser: **jours:heures:minutes:secondes**").queue();
            return false;
        }

        final int startTime = TimeToIntCalculator.format(content) * 1000;
        final AudioTrack currentTrack = musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack();
        if (Long.valueOf(startTime) > currentTrack.getDuration()) {
            textChannel.sendMessage("**" + content + "** > TrackDuration").queue();
            return false;
        }

        musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack().setPosition(Long.valueOf(startTime));
        textChannel.sendMessage("Seek to **" + content + "**").queue();

        return true;
    }
}

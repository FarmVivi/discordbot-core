package fr.farmvivi.discordbot.command.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.command.Command;
import fr.farmvivi.discordbot.command.CommandCategory;
import fr.farmvivi.discordbot.music.MusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ReplayCommand extends Command {
    public ReplayCommand() {
        this.name = "replay";
        this.category = CommandCategory.MUSIC;
        this.description = "Rejoue la musique en cours de lecture juste après";
    }

    @Override
    protected boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        final TextChannel textChannel = event.getTextChannel();
        final Guild guild = textChannel.getGuild();
        final MusicManager musicManager = Bot.getInstance().getMusicManager();

        if (musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return false;
        }

        final AudioTrack currentTrack = musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack();
        if (currentTrack == null)
            return false;

        musicManager.getPlayer(guild).getListener().addTrackFirst(currentTrack.makeClone());
        textChannel.sendMessage("La piste **" + currentTrack.getInfo().title + "** va être rejoué").queue();

        return true;
    }
}

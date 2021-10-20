package fr.farmvivi.animecity.command.music.equalizer;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import fr.farmvivi.animecity.command.CommandCategory;
import fr.farmvivi.animecity.music.MusicManager;
import fr.farmvivi.animecity.music.MusicPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EqStartCommand extends Command {
    public EqStartCommand() {
        this.name = "eqstart";
        this.category = CommandCategory.MUSIC;
        this.description = "Lance le tunage de la musique";
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

        final MusicPlayer musicPlayer = musicManager.getPlayer(guild);
        musicPlayer.getAudioPlayer().setFrameBufferDuration(500);
        musicPlayer.getAudioPlayer().setFilterFactory(musicPlayer.getEqualizer());
        textChannel.sendMessage("**Equalizer** activé.").queue();

        return true;
    }
}

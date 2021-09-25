package fr.farmvivi.animecity.command.music;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import fr.farmvivi.animecity.music.MusicManager;
import fr.farmvivi.animecity.otherclass.DetermineIsNumber;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class VolumeCommand extends Command {
    public VolumeCommand() {
        this.name = "volume";
        this.aliases = new String[] { "v" };
        this.args = "<volume>";
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

        final int volume;

        if (content.length() > 0 && DetermineIsNumber.isInteger(content)) {
            volume = Integer.valueOf(content);
        } else {
            textChannel.sendMessage(
                    "Le volume actuel est à **" + musicManager.getPlayer(guild).getAudioPlayer().getVolume() + "%**.")
                    .queue();
            return true;
        }

        if (volume > 0 && volume <= 100) {
            musicManager.getPlayer(guild).getAudioPlayer().setVolume(volume);
            textChannel.sendMessage("Volume mis à **" + volume + "%**.").queue();
        } else {
            textChannel.sendMessage("Le volume doit être compris entre **1%** et **100%**.").queue();
            return false;
        }

        return true;
    }
}

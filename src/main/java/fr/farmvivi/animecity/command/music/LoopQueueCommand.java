package fr.farmvivi.animecity.command.music;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import fr.farmvivi.animecity.music.MusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LoopQueueCommand extends Command {
    public LoopQueueCommand() {
        this.name = "loopqueue";
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

        if (musicManager.getPlayer(guild).isLoopQueueMode()) {
            musicManager.getPlayer(guild).setLoopQueueMode(false);
            textChannel.sendMessage("**Loop queue** désactivé.").queue();
        } else {
            musicManager.getPlayer(guild).setLoopQueueMode(true);
            textChannel.sendMessage("**Loop queue** activé.").queue();
        }

        return true;
    }
}

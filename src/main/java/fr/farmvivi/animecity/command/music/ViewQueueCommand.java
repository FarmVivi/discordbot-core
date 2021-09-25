package fr.farmvivi.animecity.command.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import fr.farmvivi.animecity.music.MusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ViewQueueCommand extends Command {
    public ViewQueueCommand() {
        this.name = "viewqueue";
        this.aliases = new String[] { "queue" };
    }

    @Override
    protected boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        final TextChannel textChannel = event.getTextChannel();
        final Guild guild = textChannel.getGuild();
        final MusicManager musicManager = Bot.getInstance().getMusicManager();

        if (musicManager.getPlayer(guild).getListener().getTracks().isEmpty()) {
            textChannel.sendMessage("Il n'y a pas de musique dans la file d'attente.").queue();
            return true;
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("**Queue** :\n");
        for (final AudioTrack track : musicManager.getPlayer(guild).getListener().getTracks())
            builder.append("\n-> **").append(track.getInfo().title).append("**");
        textChannel.sendMessage(builder.toString()).queue();

        return true;
    }
}

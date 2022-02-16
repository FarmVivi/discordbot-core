package fr.farmvivi.discordbot.command.music;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.command.Command;
import fr.farmvivi.discordbot.command.CommandCategory;
import fr.farmvivi.discordbot.music.MusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LoopCommand extends Command {
    public LoopCommand() {
        this.name = "loop";
        this.category = CommandCategory.MUSIC;
        this.description = "Met en boucle la musique en cours de lecture";
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

        if (musicManager.getPlayer(guild).isLoopMode()) {
            musicManager.getPlayer(guild).setLoopMode(false);
            textChannel.sendMessage("**Loop** désactivé.").queue();
        } else {
            musicManager.getPlayer(guild).setLoopMode(true);
            textChannel.sendMessage("**Loop** activé.").queue();
        }

        return true;
    }
}

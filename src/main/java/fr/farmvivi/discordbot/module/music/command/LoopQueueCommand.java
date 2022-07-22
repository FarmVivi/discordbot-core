package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LoopQueueCommand extends Command {
    private final MusicModule musicModule;

    public LoopQueueCommand(MusicModule musicModule) {
        this.name = "loopqueue";
        this.category = CommandCategory.MUSIC;
        this.description = "Met en boucle la musique en cours de lecture ainsi que la file d'attente";

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        TextChannel textChannel = event.getChannel().asTextChannel();
        Guild guild = textChannel.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return false;
        }

        if (musicModule.getPlayer(guild).isLoopQueueMode()) {
            musicModule.getPlayer(guild).setLoopQueueMode(false);
            textChannel.sendMessage("**Loop queue** désactivé.").queue();
        } else {
            musicModule.getPlayer(guild).setLoopQueueMode(true);
            textChannel.sendMessage("**Loop queue** activé.").queue();
        }

        return true;
    }
}

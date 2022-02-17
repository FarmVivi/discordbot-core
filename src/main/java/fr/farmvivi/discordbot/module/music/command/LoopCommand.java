package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LoopCommand extends Command {
    private final MusicModule musicModule;

    public LoopCommand(MusicModule musicModule) {
        this.name = "loop";
        this.category = CommandCategory.MUSIC;
        this.description = "Met en boucle la musique en cours de lecture";

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        TextChannel textChannel = event.getTextChannel();
        Guild guild = textChannel.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return false;
        }

        if (musicModule.getPlayer(guild).isLoopMode()) {
            musicModule.getPlayer(guild).setLoopMode(false);
            textChannel.sendMessage("**Loop** désactivé.").queue();
        } else {
            musicModule.getPlayer(guild).setLoopMode(true);
            textChannel.sendMessage("**Loop** activé.").queue();
        }

        return true;
    }
}

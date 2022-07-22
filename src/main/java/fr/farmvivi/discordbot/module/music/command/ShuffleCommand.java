package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ShuffleCommand extends Command {
    private final MusicModule musicModule;

    public ShuffleCommand(MusicModule musicModule) {
        this.name = "shuffle";
        this.category = CommandCategory.MUSIC;
        this.description = "Joue la musique aléatoirement";

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

        if (musicModule.getPlayer(guild).isShuffleMode()) {
            musicModule.getPlayer(guild).setShuffleMode(false);
            textChannel.sendMessage("**Shuffle** désactivé.").queue();
        } else {
            musicModule.getPlayer(guild).setShuffleMode(true);
            textChannel.sendMessage("**Shuffle** activé.").queue();
        }

        return true;
    }
}

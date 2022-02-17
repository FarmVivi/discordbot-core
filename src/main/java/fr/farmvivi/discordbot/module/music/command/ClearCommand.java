package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ClearCommand extends Command {
    private final MusicModule musicModule;

    public ClearCommand(MusicModule musicModule) {
        this.name = "clear";
        this.category = CommandCategory.MUSIC;
        this.description = "Supprime la file d'attente";

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

        if (musicModule.getPlayer(guild).getListener().getTracks().isEmpty()) {
            textChannel.sendMessage("Il n'y a pas de musique dans la file d'attente.").queue();
            return false;
        }

        musicModule.getPlayer(guild).getListener().getTracks().clear();
        textChannel.sendMessage("La liste d'attente à été vidé.").queue();

        return true;
    }
}

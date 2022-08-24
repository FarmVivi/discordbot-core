package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;

public class ClearCommand extends Command {
    private final MusicModule musicModule;

    public ClearCommand(MusicModule musicModule) {
        super("clear", CommandCategory.MUSIC, "Vide la file d'attente");

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content, CommandMessageBuilder reply) {
        if (!super.execute(event, content, reply))
            return false;

        Guild guild = event.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            reply.addContent("Aucune musique en cours de lecture.");
            return false;
        }

        if (musicModule.getPlayer(guild).getListener().getTracks().isEmpty()) {
            reply.addContent("Il n'y a pas de musique dans la file d'attente.");
            return false;
        }

        musicModule.getPlayer(guild).getListener().getTracks().clear();
        reply.addContent("La liste d'attente à été vidé.");

        return true;
    }
}

package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class ClearQueueCommand extends Command {
    private final MusicModule musicModule;

    public ClearQueueCommand(MusicModule musicModule) {
        super("clear-queue", CommandCategory.MUSIC, "Vide la file d'attente");

        this.setAliases(new String[]{"clear", "clearqueue"});

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
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

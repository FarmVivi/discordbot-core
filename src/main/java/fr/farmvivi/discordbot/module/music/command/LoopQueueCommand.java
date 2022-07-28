package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;

public class LoopQueueCommand extends Command {
    private final MusicModule musicModule;

    public LoopQueueCommand(MusicModule musicModule) {
        super("loopqueue", CommandCategory.MUSIC, "Répète en boucle la file d'attente");

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content, CommandMessageBuilder reply) {
        if (!super.execute(event, content, reply))
            return false;

        Guild guild = event.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            reply.append("Aucune musique en cours de lecture.");
            return false;
        }

        if (musicModule.getPlayer(guild).isLoopQueueMode()) {
            musicModule.getPlayer(guild).setLoopQueueMode(false);
            reply.append("**Loop queue** désactivé.");
        } else {
            musicModule.getPlayer(guild).setLoopQueueMode(true);
            reply.append("**Loop queue** activé.");
        }

        return true;
    }
}

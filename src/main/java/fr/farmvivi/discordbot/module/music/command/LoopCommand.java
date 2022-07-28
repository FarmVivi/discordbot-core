package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;

public class LoopCommand extends Command {
    private final MusicModule musicModule;

    public LoopCommand(MusicModule musicModule) {
        super("loop", CommandCategory.MUSIC, "Répète en boucle la musique en cours de lecture");

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

        if (musicModule.getPlayer(guild).isLoopMode()) {
            musicModule.getPlayer(guild).setLoopMode(false);
            reply.append("**Loop** désactivé.");
        } else {
            musicModule.getPlayer(guild).setLoopMode(true);
            reply.append("**Loop** activé.");
        }

        return true;
    }
}

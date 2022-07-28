package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;

public class StopCommand extends Command {
    private final MusicModule musicModule;

    public StopCommand(MusicModule musicModule) {
        super("stop", CommandCategory.MUSIC, "Arrête la musique et vide la file d'attente");

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

        musicModule.getPlayer(guild).getListener().getTracks().clear();
        musicModule.getPlayer(guild).skipTrack();

        reply.append("La musique a été stoppée.");

        return true;
    }
}

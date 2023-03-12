package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class PauseCommand extends MusicCommand {
    public PauseCommand(MusicModule musicModule) {
        super(musicModule, "pause", CommandCategory.MUSIC, "Met en pause la musique");

        this.setAliases(new String[]{"resume"});
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

        if (musicModule.getPlayer(guild).getAudioPlayer().isPaused()) {
            musicModule.getPlayer(guild).getAudioPlayer().setPaused(false);
            reply.addContent("Lecture !");
        } else {
            musicModule.getPlayer(guild).getAudioPlayer().setPaused(true);
            reply.addContent("Pause !");
        }

        return true;
    }
}

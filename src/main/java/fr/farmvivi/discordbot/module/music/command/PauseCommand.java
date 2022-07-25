package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class PauseCommand extends Command {
    private final MusicModule musicModule;

    public PauseCommand(MusicModule musicModule) {
        super("pause", CommandCategory.MUSIC, "Met en pause la musique (et inversement)", new String[]{"resume"});

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        TextChannel textChannel = event.getChannel().asTextChannel();
        Guild guild = textChannel.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return false;
        }

        if (musicModule.getPlayer(guild).getAudioPlayer().isPaused()) {
            musicModule.getPlayer(guild).getAudioPlayer().setPaused(false);
            textChannel.sendMessage("Lecture !").queue();
        } else {
            musicModule.getPlayer(guild).getAudioPlayer().setPaused(true);
            textChannel.sendMessage("Pause !").queue();
        }

        return true;
    }
}

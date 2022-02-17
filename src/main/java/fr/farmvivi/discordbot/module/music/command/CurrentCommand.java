package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CurrentCommand extends Command {
    private final MusicModule musicModule;

    public CurrentCommand(MusicModule musicModule) {
        this.name = "current";
        this.aliases = new String[] { "np", "info" };
        this.category = CommandCategory.MUSIC;
        this.description = "Affiche la musique en cours de lecture";

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

        AudioTrack track = musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack();
        textChannel
                .sendMessage("Musique en cours de lecture: **" + track.getInfo().title + "** | " + track.getInfo().uri)
                .queue();

        return true;
    }
}

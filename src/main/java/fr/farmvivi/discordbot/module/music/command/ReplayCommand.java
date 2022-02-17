package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ReplayCommand extends Command {
    private final MusicModule musicModule;

    public ReplayCommand(MusicModule musicModule) {
        this.name = "replay";
        this.category = CommandCategory.MUSIC;
        this.description = "Rejoue la musique en cours de lecture juste après";

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

        AudioTrack currentTrack = musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack();
        if (currentTrack == null)
            return false;

        musicModule.getPlayer(guild).getListener().addTrackFirst(currentTrack.makeClone());
        textChannel.sendMessage("La piste **" + currentTrack.getInfo().title + "** va être rejoué").queue();

        return true;
    }
}

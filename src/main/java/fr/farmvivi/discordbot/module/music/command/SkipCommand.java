package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class SkipCommand extends Command {
    private final MusicModule musicModule;

    public SkipCommand(MusicModule musicModule) {
        super("skip", CommandCategory.MUSIC, "Passe à la musique suivante", new String[]{"fs"});

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

        AudioTrack track = musicModule.getPlayer(guild).skipTrack();
        if (track == null)
            textChannel.sendMessage("Plus aucune musique à jouer.").queue();
        else
            textChannel.sendMessage("Musique suivante: **" + track.getInfo().title + "**").queue();

        return true;
    }
}

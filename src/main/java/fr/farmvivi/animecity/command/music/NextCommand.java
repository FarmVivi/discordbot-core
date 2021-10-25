package fr.farmvivi.animecity.command.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import fr.farmvivi.animecity.command.CommandCategory;
import fr.farmvivi.animecity.music.MusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NextCommand extends Command {
    public NextCommand() {
        this.name = "next";
        this.category = CommandCategory.MUSIC;
        this.description = "Passe à la musique suivante (sans l'enlever de la file d'attente si loopqueue est activé)";
    }

    @Override
    protected boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        final TextChannel textChannel = event.getTextChannel();
        final Guild guild = textChannel.getGuild();
        final MusicManager musicManager = Bot.getInstance().getMusicManager();

        if (musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return false;
        }

        final AudioTrack track = musicManager.getPlayer(guild).skipTrack(false);
        if (track == null)
            textChannel.sendMessage("Plus aucune musique à jouer.").queue();
        else
            textChannel.sendMessage("Musique suivante: **" + track.getInfo().title + "**").queue();

        return true;
    }
}

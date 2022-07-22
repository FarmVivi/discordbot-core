package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NextCommand extends Command {
    private final MusicModule musicModule;

    public NextCommand(MusicModule musicModule) {
        this.name = "next";
        this.category = CommandCategory.MUSIC;
        this.description = "Passe à la musique suivante (sans l'enlever de la file d'attente si loopqueue est activé)";

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        TextChannel textChannel = event.getChannel().asTextChannel();
        Guild guild = textChannel.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return false;
        }

        AudioTrack track = musicModule.getPlayer(guild).nextTrack();
        if (track == null)
            textChannel.sendMessage("Plus aucune musique à jouer.").queue();
        else
            textChannel.sendMessage("Musique suivante: **" + track.getInfo().title + "**").queue();

        return true;
    }
}

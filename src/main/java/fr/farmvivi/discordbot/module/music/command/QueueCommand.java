package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import fr.farmvivi.discordbot.module.music.MusicPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class QueueCommand extends MusicCommand {
    public QueueCommand(MusicModule musicModule) {
        super(musicModule, "queue", CommandCategory.MUSIC, "Affiche la file d'attente");

        this.setAliases(new String[]{"viewqueue", "view-queue"});
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        Guild guild = event.getGuild();
        MusicPlayer musicPlayer = musicModule.getPlayer(guild);

        if (musicPlayer.getQueueSize() == 0) {
            reply.addContent("Il n'y a pas de musique dans la file d'attente.");
            return true;
        }

        reply.addContent("File d'attente (" + musicPlayer.getQueueSize() + ") :\n");

        AudioTrack track = musicPlayer.getAudioPlayer().getPlayingTrack();

        StringBuilder topQueue = new StringBuilder();
        int i = 0;
        long endingTimeMs = -1;
        if (track.getDuration() != Long.MAX_VALUE) {
            endingTimeMs = System.currentTimeMillis() + (track.getDuration() - track.getPosition());
        }
        for (AudioTrack queueTrack : musicPlayer.getQueue()) {
            if (musicPlayer.getAudioPlayer().isPaused() || endingTimeMs == -1) {
                topQueue.append(String.format("%s. [%s](%s)%n", i + 1, queueTrack.getInfo().title, queueTrack.getInfo().uri));
            } else {
                topQueue.append(String.format("%s. [%s](%s) - <t:%d:R>%n", i + 1, queueTrack.getInfo().title, queueTrack.getInfo().uri, endingTimeMs / 1000));
                if (queueTrack.getDuration() != Long.MAX_VALUE) {
                    endingTimeMs += queueTrack.getDuration();
                } else {
                    endingTimeMs = -1;
                }
            }
            i++;
            // Limit to 15 tracks
            if (i >= 15) {
                break;
            }
        }
        reply.addContent(topQueue.toString());

        return true;
    }
}

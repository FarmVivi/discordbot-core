package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;

public class QueueCommand extends Command {
    private final MusicModule musicModule;

    public QueueCommand(MusicModule musicModule) {
        super("queue", CommandCategory.MUSIC, "Affiche la file d'attente", new String[]{"viewqueue", "view-queue"});

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content, CommandMessageBuilder reply) {
        if (!super.execute(event, content, reply))
            return false;

        Guild guild = event.getGuild();

        if (musicModule.getPlayer(guild).getListener().getTracks().isEmpty()) {
            reply.addContent("Il n'y a pas de musique dans la file d'attente.");
            return true;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("**Queue** :");
        for (AudioTrack track : musicModule.getPlayer(guild).getListener().getTracks())
            builder.append("\n-> **").append(track.getInfo().title).append("**");
        reply.addContent(builder.toString());

        return true;
    }
}

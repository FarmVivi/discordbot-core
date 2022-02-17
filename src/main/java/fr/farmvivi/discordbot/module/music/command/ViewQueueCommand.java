package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ViewQueueCommand extends Command {
    private final MusicModule musicModule;

    public ViewQueueCommand(MusicModule musicModule) {
        this.name = "viewqueue";
        this.aliases = new String[] { "queue" };
        this.category = CommandCategory.MUSIC;
        this.description = "Affiche la file d'attente";

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        TextChannel textChannel = event.getTextChannel();
        Guild guild = textChannel.getGuild();

        if (musicModule.getPlayer(guild).getListener().getTracks().isEmpty()) {
            textChannel.sendMessage("Il n'y a pas de musique dans la file d'attente.").queue();
            return true;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("**Queue** :");
        for (AudioTrack track : musicModule.getPlayer(guild).getListener().getTracks())
            builder.append("\n-> **").append(track.getInfo().title).append("**");
        textChannel.sendMessage(builder.toString()).queue();

        return true;
    }
}

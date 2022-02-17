package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class StopCommand extends Command {
    private final MusicModule musicModule;

    public StopCommand(MusicModule musicModule) {
        this.name = "stop";
        this.category = CommandCategory.MUSIC;
        this.description = "Stoppe la musique";

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

        musicModule.getPlayer(guild).getListener().getTracks().clear();
        musicModule.getPlayer(guild).skipTrack();

        textChannel.sendMessage("La musique a été stoppée.").queue();

        return true;
    }
}

package fr.farmvivi.discordbot.module.music.command.equalizer;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EqStopCommand extends Command {
    private final MusicModule musicModule;

    public EqStopCommand(MusicModule musicModule) {
        this.name = "eqstop";
        this.category = CommandCategory.MUSIC;
        this.description = "Arrête le tunage de la musique";

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

        musicModule.getPlayer(guild).getAudioPlayer().setFilterFactory(null);
        textChannel.sendMessage("**Equalizer** désactivé.").queue();

        return true;
    }
}

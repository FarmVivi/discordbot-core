package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class VolumeCommand extends Command {
    private final MusicModule musicModule;

    public VolumeCommand(MusicModule musicModule) {
        this.name = "volume";
        this.aliases = new String[] { "v" };
        this.category = CommandCategory.MUSIC;
        this.description = "Affiche ou change le volume si une valeur est précisée";
        this.args = "<volume>";

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

        int volume;
        try {
            volume = Integer.parseInt(content);
        } catch (NumberFormatException e) {
            textChannel.sendMessage(
                    "Le volume actuel est à **" + musicModule.getPlayer(guild).getAudioPlayer().getVolume() + "%**.")
                    .queue();
            return true;
        }

        if (volume > 0 && volume <= 100) {
            musicModule.getPlayer(guild).getAudioPlayer().setVolume(volume);
            textChannel.sendMessage("Volume mis à **" + volume + "%**.").queue();
        } else {
            textChannel.sendMessage("Le volume doit être compris entre **1%** et **100%**.").queue();
            return false;
        }

        return true;
    }
}

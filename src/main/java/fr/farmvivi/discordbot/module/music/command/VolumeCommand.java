package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class VolumeCommand extends Command {
    private final MusicModule musicModule;

    public VolumeCommand(MusicModule musicModule) {
        super("volume", CommandCategory.MUSIC, "Change le volume de la musique", new OptionData[]{
                new OptionData(OptionType.INTEGER, "volume", "Nouveau volume")}, new String[]{"v"});

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content, CommandMessageBuilder reply) {
        if (!super.execute(event, content, reply))
            return false;

        Guild guild = event.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            reply.addContent("Aucune musique en cours de lecture.");
            return false;
        }

        int volume;
        try {
            volume = Integer.parseInt(content);
        } catch (NumberFormatException e) {
            reply.addContent("Le volume actuel est à **" + musicModule.getPlayer(guild).getAudioPlayer().getVolume() + "%**.");
            return true;
        }

        if (volume > 0 && volume <= 100) {
            musicModule.getPlayer(guild).getAudioPlayer().setVolume(volume);
            reply.addContent("Volume mis à **" + volume + "%**.");
        } else {
            reply.addContent("Le volume doit être compris entre **1%** et **100%**.");
            return false;
        }

        return true;
    }
}

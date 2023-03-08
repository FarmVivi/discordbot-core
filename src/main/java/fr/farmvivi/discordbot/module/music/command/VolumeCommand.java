package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Map;

public class VolumeCommand extends Command {
    private final MusicModule musicModule;

    public VolumeCommand(MusicModule musicModule) {
        super("volume", CommandCategory.MUSIC, "Voir/Changer le volume de la musique");

        OptionData volumeOption = new OptionData(OptionType.INTEGER, "volume", "Nouveau volume", false);
        volumeOption.setMinValue(0);
        volumeOption.setMaxValue(100);

        this.setArgs(new OptionData[]{volumeOption});
        this.setAliases(new String[]{"v"});

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        Guild guild = event.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            reply.addContent("Aucune musique en cours de lecture.");
            return false;
        }

        if (args.containsKey("volume")) {
            int volume = args.get("volume").getAsInt();
            musicModule.getPlayer(guild).getAudioPlayer().setVolume(volume);
            musicModule.getPlayer(guild).getMusicPlayerMessage().refreshMessage();
            reply.addContent("Volume changé à **" + volume + "%**.");
        } else {
            reply.addContent("Le volume actuel est de **" + musicModule.getPlayer(guild).getAudioPlayer().getVolume() + "%**.");
        }

        return true;
    }
}

package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Map;

public class VolumeCommand extends MusicCommand {
    public VolumeCommand(MusicModule musicModule) {
        super(musicModule, "volume", CommandCategory.MUSIC, "Voir/Changer le volume de la musique");

        OptionData volumeOption = new OptionData(OptionType.INTEGER, "volume", "Nouveau volume", false);
        volumeOption.setMinValue(0);
        volumeOption.setMaxValue(100);

        this.setArgs(new OptionData[]{volumeOption});
        this.setAliases(new String[]{"v"});
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        Guild guild = event.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            reply.error("Aucune musique en cours de lecture.");
            return false;
        }

        if (args.containsKey("volume")) {
            int volume = args.get("volume").getAsInt();
            musicModule.getPlayer(guild).setVolume(volume);
            reply.success("Volume changé à **" + volume + "%**.");
        } else {
            reply.info("Le volume actuel est de **" + musicModule.getPlayer(guild).getVolume() + "%**.");
        }

        reply.setEphemeral(true);

        return true;
    }
}

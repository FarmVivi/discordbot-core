package fr.farmvivi.discordbot.module.music.command.equalizer;

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

public class EqHighBassCommand extends Command {
    private static final float[] BASS_BOOST = {0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f};

    private final MusicModule musicModule;

    public EqHighBassCommand(MusicModule musicModule) {
        super("eqhighbass", CommandCategory.MUSIC, "Ajuste le niveau de basses du modificateur audio");

        OptionData levelOption = new OptionData(OptionType.NUMBER, "niveau", "Niveau de basses", true);

        this.setArgs(new OptionData[]{levelOption});

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

        float level = Float.parseFloat(args.get("niveau").getAsString());

        for (int i = 0; i < BASS_BOOST.length; i++)
            musicModule.getPlayer(guild).getEqualizer().setGain(i, BASS_BOOST[i] + level);

        reply.addContent("**Equalizer - High Bass** de " + level + " activé.");

        return true;
    }
}

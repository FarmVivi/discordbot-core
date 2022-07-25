package fr.farmvivi.discordbot.module.music.command.equalizer;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class EqHighBassCommand extends Command {
    private static final float[] BASS_BOOST = {0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f};

    private final MusicModule musicModule;

    public EqHighBassCommand(MusicModule musicModule) {
        super("eqhighbass", CommandCategory.MUSIC, "Arrête le tunage de la musique", new OptionData[]{
                new OptionData(OptionType.NUMBER, "level", "Niveau de basses")});

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        TextChannel textChannel = event.getChannel().asTextChannel();
        Guild guild = textChannel.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return false;
        }

        try {
            float diff = Float.parseFloat(content);
            for (int i = 0; i < BASS_BOOST.length; i++)
                musicModule.getPlayer(guild).getEqualizer().setGain(i, BASS_BOOST[i] + diff);

            textChannel.sendMessage("**Equalizer - High Bass** de " + diff + " activé.").queue();

            return true;
        } catch (NumberFormatException err) {
            textChannel.sendMessage("Valeur non valide.").queue();
            return false;
        }
    }
}

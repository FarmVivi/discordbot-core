package fr.farmvivi.animecity.command.music.equalizer;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import fr.farmvivi.animecity.command.CommandCategory;
import fr.farmvivi.animecity.music.MusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EqHighBassCommand extends Command {
    private static final float[] BASS_BOOST = { 0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f, -0.1f,
            -0.1f, -0.1f, -0.1f, -0.1f, -0.1f };

    public EqHighBassCommand() {
        this.name = "eqhighbass";
        this.category = CommandCategory.MUSIC;
        this.description = "Arrête le tunage de la musique";
        this.args = "<level>";
    }

    @Override
    protected boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        final TextChannel textChannel = event.getTextChannel();
        final Guild guild = textChannel.getGuild();
        final MusicManager musicManager = Bot.getInstance().getMusicManager();

        if (musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return false;
        }

        try {
            Float diff = Float.parseFloat(content);
            for (int i = 0; i < BASS_BOOST.length; i++)
                musicManager.getPlayer(guild).getEqualizer().setGain(i, BASS_BOOST[i] + diff);

            textChannel.sendMessage("**Equalizer - High Bass** de " + diff + " activé.").queue();

            return true;
        } catch (NumberFormatException err) {
            textChannel.sendMessage("Valeur non valide.").queue();
            return false;
        }
    }
}

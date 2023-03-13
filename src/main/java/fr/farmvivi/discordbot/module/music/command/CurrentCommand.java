package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import fr.farmvivi.discordbot.module.music.utils.TimeParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class CurrentCommand extends MusicCommand {
    public CurrentCommand(MusicModule musicModule) {
        super(musicModule, "current", CommandCategory.MUSIC, "Affiche la musique en cours de lecture");

        this.setAliases(new String[]{"np", "info"});
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        Guild guild = event.getGuild();

        if (musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            reply.warning("Aucune musique en cours de lecture.");
            return false;
        }

        AudioTrack track = musicModule.getPlayer(guild).getAudioPlayer().getPlayingTrack();
        EmbedBuilder embed = reply.createInfoEmbed();

        embed.setTitle("Musique en cours de lecture");

        embed.addField("Titre", String.format("[%s](%s)", track.getInfo().title, track.getInfo().uri), false);

        if (track.getInfo().author != null) {
            embed.addField("Auteur", track.getInfo().author, false);
        }

        if (track.getInfo().isStream) {
            embed.addField("Durée", "En direct", false);
        } else {
            embed.addField("Durée", TimeParser.convertIntToString((int) track.getDuration() / 1000), false);
        }

        reply.addEmbeds(embed.build());
        reply.setEphemeral(true);

        return true;
    }
}

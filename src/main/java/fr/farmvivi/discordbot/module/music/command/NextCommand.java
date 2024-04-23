package fr.farmvivi.discordbot.module.music.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class NextCommand extends MusicCommand {
    public NextCommand(MusicModule musicModule) {
        super(musicModule, "next", CommandCategory.MUSIC, "Passe à la musique suivante (reste dans la file d'attente si loopqueue est activé)");
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

        AudioTrack track = musicModule.getPlayer(guild).nextTrack();
        if (track == null) {
            reply.warning("Plus aucune musique à jouer.");
        } else {
            EmbedBuilder embed = reply.createSuccessEmbed();

            embed.setTitle("Musique suivante");

            if (track.getInfo().artworkUrl != null) {
                embed.setThumbnail(track.getInfo().artworkUrl);
            }

            embed.addField("Titre", String.format("[%s](%s)", track.getInfo().title, track.getInfo().uri), false);

            reply.addEmbeds(embed.build());
        }

        reply.setEphemeral(true);

        return true;
    }
}

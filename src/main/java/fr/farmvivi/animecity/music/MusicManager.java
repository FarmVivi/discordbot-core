package fr.farmvivi.animecity.music;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.spotify.LinkConverter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

public class MusicManager {
    private static final Logger logger = LoggerFactory.getLogger(MusicManager.class);

    public static final int QUIT_TIMEOUT = 900;
    public static final int DEFAULT_VOICE_VOLUME = 5;
    public static final int DEFAULT_RADIO_VOLUME = 25;

    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    private final Map<String, MusicPlayer> players = new HashMap<>();

    public MusicManager() {
        // Remote sources
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        // Local source
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
    }

    public synchronized MusicPlayer getPlayer(Guild guild) {
        if (!players.containsKey(guild.getId()))
            players.put(guild.getId(), new MusicPlayer(audioPlayerManager.createPlayer(), guild));
        return players.get(guild.getId());
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    public void loadTrack(final Guild guild, final String source) {
        this.loadTrack(guild, source, null);
    }

    public void loadTrack(final Guild guild, final String source, final boolean playNow) {
        this.loadTrack(guild, source, null, playNow);
    }

    public void loadTrack(final Guild guild, final String source, final TextChannel textChannel) {
        this.loadTrack(guild, source, textChannel, false);
    }

    public void loadTrack(final Guild guild, final String source, final TextChannel textChannel,
            final boolean playNow) {
        final MusicPlayer player = getPlayer(guild);

        guild.getAudioManager().setSendingHandler(player.getAudioPlayerSendHandler());

        if ((source.startsWith("http") && source.contains("://")) || source.startsWith("/")
                || source.startsWith("./")) {
            if (source.contains("open.spotify.com")) {
                final LinkConverter linkConverter = new LinkConverter();
                try {
                    final List<String> songs = linkConverter.convert(source);
                    for (final String songName : songs)
                        audioPlayerManager.loadItem("ytsearch: " + songName,
                                new FunctionalResultHandler(null, playlist -> {
                                    if (textChannel != null)
                                        textChannel.sendMessage("**" + playlist.getTracks().get(0).getInfo().title
                                                + "** ajouté à la file d'attente.").queue();
                                    player.playTrack(playlist.getTracks().get(0), playNow);
                                }, null, null));
                } catch (ParseException | SpotifyWebApiException | IOException e) {
                    Bot.logger.error("Unable to get tracks from " + source, e);
                }
            } else {
                audioPlayerManager.loadItemOrdered(player, source, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        if (textChannel != null)
                            textChannel.sendMessage("**" + track.getInfo().title + "** ajouté à la file d'attente.")
                                    .queue();
                        player.playTrack(track, playNow);
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        final StringBuilder builder = new StringBuilder();
                        builder.append("Ajout de la playlist **").append(playlist.getName()).append("** :");

                        for (final AudioTrack track : playlist.getTracks()) {
                            builder.append("\n-> **").append(track.getInfo().title).append("**");
                            player.playTrack(track, playNow);
                        }

                        if (textChannel != null)
                            textChannel.sendMessage(builder.toString()).queue();
                    }

                    @Override
                    public void noMatches() {
                        // Notify the user that we've got nothing
                        if (textChannel != null)
                            textChannel.sendMessage("La piste " + source + " n'a pas été trouvé.").queue();
                        else
                            logger.warn("La piste " + source + " n'a pas été trouvé.");
                    }

                    @Override
                    public void loadFailed(FriendlyException throwable) {
                        // Notify the user that everything exploded
                        if (textChannel != null)
                            textChannel
                                    .sendMessage(
                                            "Impossible de jouer la piste (raison: " + throwable.getMessage() + ").")
                                    .queue();
                        else
                            logger.warn("Impossible de jouer la piste.", throwable);
                    }
                });
            }
        } else {
            audioPlayerManager.loadItem("ytsearch: " + source, new FunctionalResultHandler(null, playlist -> {
                if (textChannel != null)
                    textChannel.sendMessage(
                            "**" + playlist.getTracks().get(0).getInfo().title + "** ajouté à la file d'attente.")
                            .queue();
                player.playTrack(playlist.getTracks().get(0), playNow);
            }, null, null));
        }
    }
}

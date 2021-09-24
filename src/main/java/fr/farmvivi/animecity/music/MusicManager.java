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
import com.wrapper.spotify.exceptions.SpotifyWebApiException;

import org.apache.hc.core5.http.ParseException;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.spotify.LinkConverter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class MusicManager {
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    private final Map<String, MusicPlayer> players = new HashMap<>();

    public MusicManager() {
        // Remote sources
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        // Local source
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    public synchronized MusicPlayer getPlayer(Guild guild) {
        if (!players.containsKey(guild.getId()))
            players.put(guild.getId(), new MusicPlayer(audioPlayerManager.createPlayer(), guild));
        return players.get(guild.getId());
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    public void loadTrack(final TextChannel channel, final String source) {
        MusicPlayer player = getPlayer(channel.getGuild());

        channel.getGuild().getAudioManager().setSendingHandler(player.getAudioPlayerSendHandler());

        if (source.startsWith("http") && source.contains("://")) {
            if (source.contains("open.spotify.com")) {
                LinkConverter linkConverter = new LinkConverter();
                try {
                    List<String> songs = linkConverter.convert(source);
                    for (String songName : songs)
                        audioPlayerManager.loadItem("ytsearch: " + songName,
                                new FunctionalResultHandler(null, playlist -> {
                                    channel.sendMessage("**" + playlist.getTracks().get(0).getInfo().title
                                            + "** ajouté à la file d'attente.").queue();
                                    player.playTrack(playlist.getTracks().get(0));
                                }, null, null));
                } catch (ParseException | SpotifyWebApiException | IOException e) {
                    Bot.logger.error("Unable to get tracks from " + source, e);
                }
            } else {
                audioPlayerManager.loadItemOrdered(player, source, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        channel.sendMessage("**" + track.getInfo().title + "** ajouté à la file d'attente.").queue();
                        player.playTrack(track);
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("Ajout de la playlist **").append(playlist.getName()).append("\n");

                        for (AudioTrack track : playlist.getTracks()) {
                            builder.append("\n **->** ").append(track.getInfo().title);
                            player.playTrack(track);
                        }
                        builder.append("**");

                        channel.sendMessage(builder.toString()).queue();
                    }

                    @Override
                    public void noMatches() {
                        // Notify the user that we've got nothing
                        channel.sendMessage("La piste " + source + " n'a pas été trouvé.").queue();
                    }

                    @Override
                    public void loadFailed(FriendlyException throwable) {
                        // Notify the user that everything exploded
                        channel.sendMessage("Impossible de jouer la piste (raison: " + throwable.getMessage() + ").")
                                .queue();
                    }
                });
            }
        } else {
            audioPlayerManager.loadItem("ytsearch: " + source, new FunctionalResultHandler(null, playlist -> {
                channel.sendMessage(
                        "**" + playlist.getTracks().get(0).getInfo().title + "** ajouté à la file d'attente.").queue();
                player.playTrack(playlist.getTracks().get(0));
            }, null, null));
        }
    }
}

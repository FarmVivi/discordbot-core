package fr.farmvivi.animecity.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.farmvivi.animecity.jda.MessageListener;
import fr.farmvivi.animecity.otherclass.DetermineIsNumber;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class MusicController {
    private final MusicManager musicManager = new MusicManager();

    public void playMusic(TextChannel textChannel, User user, String command) {
        Guild guild = textChannel.getGuild();

        if (guild == null)
            return;

        if (!guild.getAudioManager().isConnected()) {
            VoiceChannel voiceChannel = guild.getMember(user).getVoiceState().getChannel();
            if (voiceChannel == null) {
                textChannel.sendMessage("Vous devez être connecté à un salon vocal.").queue();
                return;
            }
            guild.getAudioManager().openAudioConnection(voiceChannel);
            guild.getAudioManager().setAutoReconnect(true);
            musicManager.getPlayer(guild).getAudioPlayer().setVolume(50);
        }

        if (musicManager.getPlayer(textChannel.getGuild()).getAudioPlayer().isPaused()) {
            musicManager.getPlayer(textChannel.getGuild()).getAudioPlayer().setPaused(false);
            textChannel.sendMessage("LECTURE !").queue();
        }

        if (!command.replaceFirst("\\" + MessageListener.PREFIX + "play", "").trim().equals("")) {
            musicManager.loadTrack(textChannel,
                    command.replaceFirst("\\" + MessageListener.PREFIX + "play", "").trim());
        }
    }

    public void skipMusic(TextChannel textChannel) {
        Guild guild = textChannel.getGuild();

        if (guild == null)
            return;

        if (!guild.getAudioManager().isConnected()) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return;
        }

        AudioTrack track = musicManager.getPlayer(guild).skipTrack();
        if (track == null) {
            textChannel.sendMessage("Plus aucune musique à jouer.").queue();
        } else {
            textChannel.sendMessage("Musique suivante: **" + track.getInfo().title + "** | " + track.getInfo().uri)
                    .queue();
        }
    }

    public void clearMusic(TextChannel textChannel) {
        Guild guild = textChannel.getGuild();

        if (guild == null)
            return;

        if (!guild.getAudioManager().isConnected()) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return;
        }

        MusicPlayer player = musicManager.getPlayer(textChannel.getGuild());

        if (player.getListener().getTracks().isEmpty()) {
            textChannel.sendMessage("Il n'y a pas de musique dans la file d'attente.").queue();
            return;
        }

        player.getListener().getTracks().clear();
        textChannel.sendMessage("La liste d'attente à été vidé.").queue();
    }

    public void currentMusic(TextChannel textChannel) {
        Guild guild = textChannel.getGuild();

        if (guild == null)
            return;

        if (!guild.getAudioManager().isConnected()) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return;
        }

        AudioTrack track = musicManager.getPlayer(textChannel.getGuild()).getAudioPlayer().getPlayingTrack();
        textChannel
                .sendMessage("Musique en cours de lecture: **" + track.getInfo().title + "** | " + track.getInfo().uri)
                .queue();
    }

    public void stopMusic(TextChannel textChannel) {
        Guild guild = textChannel.getGuild();

        if (guild == null)
            return;

        if (!guild.getAudioManager().isConnected()) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return;
        }

        clearMusic(textChannel);
        skipMusic(textChannel);
    }

    public void disconnectMusic(TextChannel textChannel) {
        stopMusic(textChannel);
    }

    public void pauseMusic(TextChannel textChannel) {
        Guild guild = textChannel.getGuild();

        if (guild == null)
            return;

        if (!guild.getAudioManager().isConnected()) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return;
        }

        if (musicManager.getPlayer(textChannel.getGuild()).getAudioPlayer().isPaused()) {
            musicManager.getPlayer(textChannel.getGuild()).getAudioPlayer().setPaused(false);
            textChannel.sendMessage("LECTURE !").queue();
        } else {
            musicManager.getPlayer(textChannel.getGuild()).getAudioPlayer().setPaused(true);
            textChannel.sendMessage("PAUSE !").queue();
        }
    }

    public void loopMusic(TextChannel textChannel) {
        Guild guild = textChannel.getGuild();

        if (guild == null)
            return;

        if (!guild.getAudioManager().isConnected()) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return;
        }

        if (musicManager.getPlayer(textChannel.getGuild()).isLoopMode()) {
            musicManager.getPlayer(textChannel.getGuild()).setLoopMode(false);
            textChannel.sendMessage("Loop Mode désactiver.").queue();
        } else {
            musicManager.getPlayer(textChannel.getGuild()).setLoopMode(true);
            textChannel.sendMessage("Loop Mode activé.").queue();
        }
    }

    public void volumeMusic(TextChannel textChannel, String command) {
        Guild guild = textChannel.getGuild();

        if (guild == null)
            return;

        if (!guild.getAudioManager().isConnected()) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return;
        }

        int volume = 0;

        if (DetermineIsNumber.isInteger(command.replaceFirst("\\" + MessageListener.PREFIX + "volume", "").trim())) {
            volume = Integer.valueOf(command.replaceFirst("\\" + MessageListener.PREFIX + "volume", "").trim());
        } else {
            textChannel
                    .sendMessage("Le volume actuel est à **"
                            + musicManager.getPlayer(textChannel.getGuild()).getAudioPlayer().getVolume() + "%**.")
                    .queue();
            return;
        }

        if (volume > 0 && volume <= 100) {
            musicManager.getPlayer(textChannel.getGuild()).getAudioPlayer().setVolume(volume);
            textChannel.sendMessage("Volume mis à **" + volume + "%**.").queue();
        } else {
            textChannel.sendMessage("Le volume doit être compris entre **1%** et **100%**.").queue();
        }
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }
}

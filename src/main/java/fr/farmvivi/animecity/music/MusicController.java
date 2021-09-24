package fr.farmvivi.animecity.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.otherclass.DetermineIsNumber;
import fr.farmvivi.animecity.otherclass.TimeToIntCalculator;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class MusicController {
    private final MusicManager musicManager = new MusicManager();

    public void playMusic(TextChannel textChannel, User user, String music) {
        Guild guild = textChannel.getGuild();

        if (!guild.getAudioManager().isConnected()) {
            VoiceChannel voiceChannel = guild.getMember(user).getVoiceState().getChannel();
            if (voiceChannel == null) {
                textChannel.sendMessage("Vous devez être connecté à un salon vocal.").queue();
                return;
            }
            Bot.logger.info("Join channel " + voiceChannel.getName() + "...");
            guild.getAudioManager().openAudioConnection(voiceChannel);
            guild.getAudioManager().setAutoReconnect(true);
            // Default volume
            musicManager.getPlayer(guild).getAudioPlayer().setVolume(10);
        }

        if (musicManager.getPlayer(textChannel.getGuild()).getAudioPlayer().isPaused()) {
            musicManager.getPlayer(textChannel.getGuild()).getAudioPlayer().setPaused(false);
            textChannel.sendMessage("LECTURE !").queue();
        }

        musicManager.loadTrack(textChannel, music);
    }

    public void skipMusic(TextChannel textChannel) {
        Guild guild = textChannel.getGuild();

        if (musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
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

        if (musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
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

        if (musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
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

        if (musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
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

        if (musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
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

    public void loopQueueMusic(TextChannel textChannel) {
        Guild guild = textChannel.getGuild();

        if (musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return;
        }

        if (musicManager.getPlayer(textChannel.getGuild()).isLoopQueueMode()) {
            musicManager.getPlayer(textChannel.getGuild()).setLoopQueueMode(false);
            textChannel.sendMessage("**Loop queue** désactivé.").queue();
        } else {
            musicManager.getPlayer(textChannel.getGuild()).setLoopQueueMode(true);
            textChannel.sendMessage("**Loop queue** activé.").queue();
        }
    }

    public void loopMusic(TextChannel textChannel) {
        Guild guild = textChannel.getGuild();

        if (musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return;
        }

        if (musicManager.getPlayer(textChannel.getGuild()).isLoopMode()) {
            musicManager.getPlayer(textChannel.getGuild()).setLoopMode(false);
            textChannel.sendMessage("**Loop** désactivé.").queue();
        } else {
            musicManager.getPlayer(textChannel.getGuild()).setLoopMode(true);
            textChannel.sendMessage("**Loop** activé.").queue();
        }
    }

    public void volumeMusic(TextChannel textChannel, String volume) {
        Guild guild = textChannel.getGuild();

        if (musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return;
        }

        int volumeInt = 0;

        if (volume.length() > 0 && DetermineIsNumber.isInteger(volume)) {
            volumeInt = Integer.valueOf(volume);
        } else {
            textChannel
                    .sendMessage("Le volume actuel est à **"
                            + musicManager.getPlayer(textChannel.getGuild()).getAudioPlayer().getVolume() + "%**.")
                    .queue();
            return;
        }

        if (volumeInt > 0 && volumeInt <= 100) {
            musicManager.getPlayer(textChannel.getGuild()).getAudioPlayer().setVolume(volumeInt);
            textChannel.sendMessage("Volume mis à **" + volumeInt + "%**.").queue();
        } else {
            textChannel.sendMessage("Le volume doit être compris entre **1%** et **100%**.").queue();
        }
    }

    public void replayMusic(TextChannel textChannel) {
        Guild guild = textChannel.getGuild();

        if (musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return;
        }

        AudioTrack currentTrack = musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack();
        if (currentTrack == null)
            return;
        musicManager.getPlayer(guild).getListener().addTrackFirst(currentTrack.makeClone());
        textChannel.sendMessage("La piste **" + currentTrack.getInfo().title + "** va être rejoué").queue();
    }

    public void seekMusic(TextChannel textChannel, String content) {
        Guild guild = textChannel.getGuild();

        if (musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack() == null) {
            textChannel.sendMessage("Aucune musique en cours de lecture.").queue();
            return;
        }

        MusicPlayer musicPlayer = musicManager.getPlayer(guild);
        if (!musicPlayer.getAudioPlayer().getPlayingTrack().isSeekable()) {
            textChannel.sendMessage("Cette piste n'est pas seekable.").queue();
            return;
        }

        if (!TimeToIntCalculator.isFormated(content)) {
            textChannel.sendMessage("Format de temps à utiliser: **jours:heures:minutes:secondes**").queue();
            return;
        }
        int startTime = TimeToIntCalculator.format(content);
        AudioTrack currentTrack = musicManager.getPlayer(guild).getAudioPlayer().getPlayingTrack();
        if (Long.valueOf(startTime) > currentTrack.getDuration()) {
            textChannel.sendMessage("**" + content + "** > TrackDuration").queue();
            return;
        }
        musicPlayer.getAudioPlayer().getPlayingTrack().setPosition(Long.valueOf(startTime));
        textChannel.sendMessage("Seek to **" + content + "**").queue();
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }
}

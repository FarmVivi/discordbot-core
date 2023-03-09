package fr.farmvivi.discordbot.module.music;

import fr.farmvivi.discordbot.Bot;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class MusicEventHandler extends ListenerAdapter {
    private final MusicModule musicModule;

    public MusicEventHandler(MusicModule musicModule) {
        this.musicModule = musicModule;
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        super.onGuildVoiceUpdate(event);

        if (event.getChannelLeft() != null && event.getChannelJoined() == null &&
                event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
            MusicPlayer musicPlayer = musicModule.getPlayer(event.getGuild());
            musicPlayer.getAudioPlayer().setPaused(false);
            musicPlayer.getListener().getTracks().clear();
            musicPlayer.skipTrack();
            musicPlayer.resetToDefaultSettings();
            Bot.setDefaultActivity();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith(MusicModule.PLAYER_ID_PREFIX)) {
            return;
        }

        String guildId = MusicModule.getGuildID(id);
        if (!guildId.equals(event.getGuild().getId())) {
            // Send error message
            event.reply("Une erreur est survenue, veuillez réessayer.").setEphemeral(true).queue();
            return;
        }

        MusicPlayer musicPlayer = musicModule.getPlayer(event.getGuild());

        String action = MusicModule.getAction(id);
        switch (action) {
            case "pause" -> musicPlayer.getAudioPlayer().setPaused(!musicPlayer.getAudioPlayer().isPaused());
            case "skip" -> musicPlayer.skipTrack();
            case "stop" -> {
                musicPlayer.getAudioPlayer().setPaused(false);
                musicPlayer.getListener().getTracks().clear();
                musicPlayer.skipTrack();
            }
            case "clearqueue" -> {
                musicPlayer.getListener().getTracks().clear();
                musicPlayer.getMusicPlayerMessage().refreshMessage();
            }
            case "loop" -> musicPlayer.setLoopMode(!musicPlayer.isLoopMode());
            case "loopqueue" -> musicPlayer.setLoopQueueMode(!musicPlayer.isLoopQueueMode());
            case "shuffle" -> musicPlayer.setShuffleMode(!musicPlayer.isShuffleMode());
            case "volumedown10" -> {
                int volume = musicPlayer.getAudioPlayer().getVolume() - 10;
                if (volume < 0) {
                    volume = 0;
                }
                musicPlayer.getAudioPlayer().setVolume(volume);
                musicPlayer.getMusicPlayerMessage().refreshMessage();
            }
            case "volumedown5" -> {
                int volume = musicPlayer.getAudioPlayer().getVolume() - 5;
                if (volume < 0) {
                    volume = 0;
                }
                musicPlayer.getAudioPlayer().setVolume(volume);
                musicPlayer.getMusicPlayerMessage().refreshMessage();
            }
            case "volumeup5" -> {
                int volume = musicPlayer.getAudioPlayer().getVolume() + 5;
                if (volume > 100) {
                    volume = 100;
                }
                musicPlayer.getAudioPlayer().setVolume(volume);
                musicPlayer.getMusicPlayerMessage().refreshMessage();
            }
            case "volumeup10" -> {
                int volume = musicPlayer.getAudioPlayer().getVolume() + 10;
                if (volume > 100) {
                    volume = 100;
                }
                musicPlayer.getAudioPlayer().setVolume(volume);
                musicPlayer.getMusicPlayerMessage().refreshMessage();
            }
            case "volumemute" -> {
                if (musicPlayer.getAudioPlayer().getVolume() == 0) {
                    musicPlayer.unmute();
                } else {
                    musicPlayer.mute();
                }
            }
            default -> event.reply("Une erreur est survenue, veuillez réessayer.").setEphemeral(true).queue();
        }

        // Delete interaction
        if (!event.isAcknowledged()) {
            event.deferEdit().queue();
        }
    }
}

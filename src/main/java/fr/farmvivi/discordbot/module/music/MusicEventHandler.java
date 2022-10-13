package fr.farmvivi.discordbot.module.music;

import fr.farmvivi.discordbot.Bot;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
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
}

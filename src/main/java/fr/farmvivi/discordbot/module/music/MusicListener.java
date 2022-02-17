package fr.farmvivi.discordbot.module.music;

import fr.farmvivi.discordbot.Bot;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MusicListener extends ListenerAdapter {
    private final MusicModule musicModule;

    public MusicListener(MusicModule musicModule) {
        this.musicModule = musicModule;
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getChannelJoined() == null &&
                event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
            musicModule.getLogger().info("Leaved channel " + event.getChannelLeft().getName());
            MusicPlayer musicPlayer = musicModule.getPlayer(event.getGuild());
            musicPlayer.getListener().getTracks().clear();
            musicPlayer.skipTrack();
            musicPlayer.resetToDefaultSettings();
            Bot.setDefaultActivity();
        }
    }
}

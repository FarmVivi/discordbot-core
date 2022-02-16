package fr.farmvivi.discordbot.listener;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.music.MusicPlayer;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GeneralListener extends ListenerAdapter {
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getChannelJoined() == null &&
                event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
            Bot.logger.info("Leaved channel " + event.getChannelLeft().getName());
            MusicPlayer musicPlayer = Bot.getInstance().getMusicManager().getPlayer(event.getGuild());
            musicPlayer.getListener().getTracks().clear();
            musicPlayer.skipTrack();
            musicPlayer.resetToDefaultSettings();
            Bot.getInstance().setDefaultActivity();
        }
    }
}

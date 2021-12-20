package fr.farmvivi.animecity.listener;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.music.MusicPlayer;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GeneralListener extends ListenerAdapter {
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getChannelJoined() == null &&
                event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
            Bot.logger.info("Leaved channel " + event.getChannelLeft().getName());
            Bot.getInstance().setDefaultActivity();
            MusicPlayer musicPlayer = Bot.getInstance().getMusicManager().getPlayer(event.getGuild());
            musicPlayer.resetToDefaultSettings();
        }
    }
}

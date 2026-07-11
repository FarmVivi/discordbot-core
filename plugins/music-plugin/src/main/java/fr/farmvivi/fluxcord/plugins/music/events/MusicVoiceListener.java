package fr.farmvivi.fluxcord.plugins.music.events;

import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Forwards JDA voice state updates to the {@link fr.farmvivi.fluxcord.plugins.music.MusicManager}.
 *
 * <p>This is registered directly as a JDA listener because JDA events are not bridged to the
 * Fluxcord event bus. It notably detects when the bot itself leaves a voice channel (kick, /stop,
 * auto-leave) so the player message can be removed and the persisted playback state cleared.
 */
public class MusicVoiceListener extends ListenerAdapter {
    private final MusicPlugin plugin;

    public MusicVoiceListener(MusicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (plugin.getMusicManager() != null) {
            plugin.getMusicManager().handleVoiceUpdate(event);
        }
    }
}

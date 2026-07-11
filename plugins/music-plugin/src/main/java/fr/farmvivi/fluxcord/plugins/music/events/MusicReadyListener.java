package fr.farmvivi.fluxcord.plugins.music.events;

import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Restores persisted music playback once the JDA session becomes ready.
 *
 * <p>On a fresh process start (e.g. a Kubernetes pod being rescheduled), the bot is no longer
 * connected to any voice channel. When JDA finishes loading the guilds, this listener asks the
 * {@link fr.farmvivi.fluxcord.plugins.music.MusicManager} to reconnect and resume playback for
 * every guild that had a saved state.
 */
public class MusicReadyListener extends ListenerAdapter {
    private final MusicPlugin plugin;

    public MusicReadyListener(MusicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        if (plugin.getMusicManager() != null) {
            plugin.getMusicManager().restoreAllStates(event.getJDA());
        }
    }
}

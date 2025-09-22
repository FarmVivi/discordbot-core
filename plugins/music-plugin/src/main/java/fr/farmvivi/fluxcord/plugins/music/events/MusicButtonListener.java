package fr.farmvivi.fluxcord.plugins.music.events;

import fr.farmvivi.fluxcord.plugins.music.ButtonHandler;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.ui.MusicPlayerMessage;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Listens to Discord button interactions (JDA) for the music player.
 */
public class MusicButtonListener extends ListenerAdapter {
    private final MusicPlugin plugin;

    public MusicButtonListener(MusicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        MusicPlayerMessage.ButtonInfo info = MusicPlayerMessage.parseButtonId(buttonId);
        if (info == null) {
            return; // Not a music button
        }
        new ButtonHandler(plugin).handleButton(event, info);
    }
}

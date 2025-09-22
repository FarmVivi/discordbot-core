package fr.farmvivi.fluxcord.plugins.music.events;

import fr.farmvivi.fluxcord.api.language.LanguageManager;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.ui.MusicPlayerMessage;
import fr.farmvivi.fluxcord.plugins.music.util.ModalCommandContext;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Handles modal submissions for the music plugin (provider + query input).
 */
public class MusicModalListener extends ListenerAdapter {
    private final MusicPlugin plugin;

    public MusicModalListener(MusicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        // Expecting modal id format: "music:<guildId>:add"
        MusicPlayerMessage.ButtonInfo info = MusicPlayerMessage.parseButtonId(modalId);
        if (info == null) {
            return;
        }
        if (!"add".equals(info.action)) {
            return;
        }

        // Retrieve provider and query values
        var providerValue = event.getValue("provider");
        var queryValue = event.getValue("query");
        if (providerValue == null || queryValue == null) {
            event.reply(plugin.getPluginLanguageManager().getString("music.error.modal.invalid"))
                    .setEphemeral(true)
                    .queue();
            return;
        }

    String provider = providerValue.getAsString();
        String query = queryValue.getAsString();

        // Transform query per provider rules
        String effectiveQuery = mapQueryForProvider(provider, query);

    // Acknowledge the modal and trigger play command logic
    event.deferReply(true).queue();

    LanguageManager lm = plugin.getContext().getLanguageManager();
    var locale = lm.getDefaultLocale();
    var ctx = new ModalCommandContext(event, locale, true, true);
    // Play now = false by default; could add a checkbox in modal later
    plugin.getMusicManager().loadTrack(ctx, effectiveQuery, false);
    }

    private String mapQueryForProvider(String provider, String query) {
        // If user pasted a full URL, keep as-is for providers accepting links
        if (query.contains("://")) {
            return query;
        }
        // For search provider, prepend ytsearch: which is configured
        switch (provider) {
            case "search":
                return "ytsearch:" + query;
            case "youtube":
                // Accept either raw search via ytsearch: or expect link; we default to ytsearch for convenience
                return "ytsearch:" + query;
            case "spotify":
            case "deezer":
            case "apple_music":
            case "soundcloud":
            case "bandcamp":
            case "vimeo":
            case "twitch":
            case "getyarn":
            case "http":
            case "local":
            case "flowery_tts":
                // These typically expect URLs or identifiers understood by their respective SourceManagers.
                // Without a URL, fallback to ytsearch (common UX) so the user can search by name.
                return "ytsearch:" + query;
            default:
                return query;
        }
    }
}

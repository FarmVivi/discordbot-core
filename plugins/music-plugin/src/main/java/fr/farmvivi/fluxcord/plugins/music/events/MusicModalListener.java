package fr.farmvivi.fluxcord.plugins.music.events;

import fr.farmvivi.fluxcord.api.language.LanguageManager;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import fr.farmvivi.fluxcord.plugins.music.ui.MusicPlayerMessage;
import fr.farmvivi.fluxcord.plugins.music.util.ModalCommandContext;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles modal submissions for the music plugin (provider + query input).
 */
public class MusicModalListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MusicModalListener.class);
    private static final String YTSEARCH_PREFIX = "ytsearch:";
    private final MusicPlugin plugin;

    public MusicModalListener(MusicPlugin plugin) {
        this.plugin = plugin;
    }

    private static String trim(String s, int max) {
        if (s == null) return "null";
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max)) + "…";
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        final String userId = event.getUser().getId();
        final String guildId = event.isFromGuild() ? event.getGuild().getId() : "DM";
        final String channelId = event.getChannel().getId();

        if (logger.isDebugEnabled()) {
            logger.debug("[MusicModal] Received modal interaction: modalId={}, user={}, guild={}, channel={}",
                    modalId, userId, guildId, channelId);
        }

        try {
            // Expecting modal id format: "music:<guildId>:add"
            MusicPlayerMessage.ButtonInfo info = MusicPlayerMessage.parseButtonId(modalId);
            if (info == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[MusicModal] Ignored modal: not a music modal (modalId={})", modalId);
                }
                return;
            }
            if (!"add".equals(info.action())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[MusicModal] Ignored modal: unsupported action '{}' (modalId={})", info.action(), modalId);
                }
                return;
            }

            // Retrieve provider and query values
            var providerValue = event.getValue("provider");
            var queryValue = event.getValue("query");
            if (providerValue == null || queryValue == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[MusicModal] Missing fields: providerPresent={}, queryPresent={} (modalId={})",
                            providerValue != null, queryValue != null, modalId);
                }
                event.reply(plugin.getPluginLanguageManager().getString("music.error.modal.invalid"))
                        .setEphemeral(true)
                        .queue();
                return;
            }

            String provider = providerValue.getAsString();
            String query = queryValue.getAsString();

            if (logger.isDebugEnabled()) {
                logger.debug("[MusicModal] Input values: provider='{}', query='{}' (len={})", provider, trim(query, 256),
                        query.length());
            }

            // Transform query per provider rules
            String effectiveQuery = mapQueryForProvider(provider, query);
            if (logger.isDebugEnabled()) {
                logger.debug("[MusicModal] Mapped query: '{}' -> '{}' (provider='{}')", trim(query, 256), trim(effectiveQuery, 256), provider);
            }

            // Acknowledge the modal and trigger play command logic
            if (logger.isDebugEnabled()) {
                logger.debug("[MusicModal] Deferring ephemeral reply and dispatching loadTrack (guild={}, user={})", guildId, userId);
            }
            event.deferReply(true).queue();

            LanguageManager lm = plugin.getContext().getLanguageManager();
            var locale = lm.getDefaultLocale();
            var ctx = new ModalCommandContext(event, locale, true, true);
            // Play now = false by default; could add a checkbox in modal later
            plugin.getMusicManager().loadTrack(ctx, effectiveQuery, false);

            if (logger.isDebugEnabled()) {
                logger.debug("[MusicModal] loadTrack dispatched successfully (guild={}, provider={}, now={})", guildId, provider, false);
            }
        } catch (Exception e) {
            logger.error("[MusicModal] Unexpected error handling modal interaction (modalId={}, guild={}, user={})",
                    modalId, guildId, userId, e);
            try {
                event.reply(plugin.getPluginLanguageManager().getString("music.error.modal.unexpected"))
                        .setEphemeral(true)
                        .queue();
            } catch (Exception ignored) {
                // Already acknowledged or failed to reply; nothing else we can do here
            }
        }
    }

    private String mapQueryForProvider(String provider, String query) {
        // If user pasted a full URL, keep as-is for providers accepting links
        if (query.contains("://")) {
            return query;
        }
        // For search provider, prepend ytsearch: which is configured
        switch (provider) {
            case "search":
                return YTSEARCH_PREFIX + query;
            case "youtube":
                // Accept either raw search via ytsearch: or expect link; we default to ytsearch for convenience
                return YTSEARCH_PREFIX + query;
          case "spotify", "deezer", "apple_music", "soundcloud", "bandcamp",
              "vimeo", "twitch", "getyarn", "http", "local", "flowery_tts":
                // These typically expect URLs or identifiers understood by their respective SourceManagers.
                // Without a URL, fallback to ytsearch (common UX) so the user can search by name.
                return YTSEARCH_PREFIX + query;
            default:
                return query;
        }
    }
}

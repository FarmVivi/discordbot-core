package fr.farmvivi.fluxcord.plugins.music;

import fr.farmvivi.fluxcord.api.command.CommandResult;
import fr.farmvivi.fluxcord.api.command.option.OptionChoice;
import fr.farmvivi.fluxcord.api.event.EventHandler;
import fr.farmvivi.fluxcord.api.event.EventPriority;
import fr.farmvivi.fluxcord.api.permissions.Permission;
import fr.farmvivi.fluxcord.api.permissions.PermissionDefault;
import fr.farmvivi.fluxcord.api.plugin.AbstractPlugin;
import fr.farmvivi.fluxcord.plugins.music.commands.*;
import fr.farmvivi.fluxcord.plugins.music.playlist.PlaylistManager;
import fr.farmvivi.fluxcord.plugins.music.ui.MusicPlayerMessage;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Advanced music bot plugin for Fluxcord.
 * <p>
 * Features:
 * - Play music from YouTube, Spotify, SoundCloud, Deezer, Apple Music
 * - Advanced queue management with shuffle, loop, and priority
 * - Persistent music player messages with interactive controls
 * - Playlist management (personal and server playlists)
 * - Audio effects and volume control
 * - Multi-language support via Fluxcord i18n API
 * - Automatic disconnection after inactivity
 */
public class MusicPlugin extends AbstractPlugin {

    private MusicManager musicManager;
    private PlaylistManager playlistManager;
    private ScheduledExecutorService scheduler;

    @Override
    public void onEnable() {
        logger.info("Music Plugin enabling...");

        // Initialize scheduler
        this.scheduler = Executors.newScheduledThreadPool(2);

        // Register permissions
        registerPermissions();

        // Initialize managers
        this.musicManager = new MusicManager(this);
        this.playlistManager = new PlaylistManager(this);

        // Register commands
        registerCommands();

        // Load configuration
        loadConfiguration();

        logger.info("Music Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        logger.info("Music Plugin disabling...");

        if (scheduler != null) {
            scheduler.shutdown();
        }

        if (musicManager != null) {
            musicManager.shutdown();
        }

        if (playlistManager != null) {
            playlistManager.saveAllPlaylists();
        }

        logger.info("Music Plugin disabled!");
    }

    private void registerPermissions() {
        registerPermissionNode("play", "Allows playing tracks", PermissionDefault.TRUE);
        registerPermissionNode("skip", "Allows skipping current track", PermissionDefault.TRUE);
        registerPermissionNode("queue", "Allows viewing the queue", PermissionDefault.TRUE);
        registerPermissionNode("volume", "Allows changing playback volume", PermissionDefault.OP);
        registerPermissionNode("playlist", "Allows managing playlists", PermissionDefault.TRUE);
        registerPermissionNode("admin", "Allows moderator music actions", PermissionDefault.OP);
    }

    private void registerPermissionNode(String node, String description, PermissionDefault def) {
        getPluginPermissionManager().registerPermission(new SimplePermission(permissionKey(node), description, def));
    }

    private String permissionKey(String node) {
        return getName().toLowerCase() + "." + node;
    }

    private void registerCommands() {
        // Main commands
        commandService.registerCommand(this, builder -> {
            builder.name("play")
                    .description(getPluginLanguageManager().getString("music.command.play.description"))
                    .category("Music")
                    .aliases("p")
                    .stringOption("query", getPluginLanguageManager().getString("music.command.play.option.query"), true)
                    .booleanOption("now", getPluginLanguageManager().getString("music.command.play.option.now"), false)
                    .executor((ctx, cmd) -> {
                        String query = ctx.getRequiredOption("query");
                        boolean playNow = ctx.getOption("now", false);
                        new PlayCommand(this).execute(ctx, query, playNow);
                        return CommandResult.success();
                    });
        });

        commandService.registerCommand(this, builder -> {
            builder.name("pause")
                    .description(getPluginLanguageManager().getString("music.command.pause.description"))
                    .category("Music")
                    .executor((ctx, cmd) -> {
                        new PauseCommand(this).execute(ctx);
                        return CommandResult.success();
                    });
        });

        commandService.registerCommand(this, builder -> {
            builder.name("skip")
                    .description(getPluginLanguageManager().getString("music.command.skip.description"))
                    .category("Music")
                    .aliases("s", "next")
                    .executor((ctx, cmd) -> {
                        new SkipCommand(this).execute(ctx);
                        return CommandResult.success();
                    });
        });

        commandService.registerCommand(this, builder -> {
            builder.name("stop")
                    .description(getPluginLanguageManager().getString("music.command.stop.description"))
                    .category("Music")
                    .executor((ctx, cmd) -> {
                        new StopCommand(this).execute(ctx);
                        return CommandResult.success();
                    });
        });

        commandService.registerCommand(this, builder -> {
            builder.name("queue")
                    .description(getPluginLanguageManager().getString("music.command.queue.description"))
                    .category("Music")
                    .aliases("q")
                    .integerOption("page", getPluginLanguageManager().getString("music.command.queue.option.page"), false, 1, 100)
                    .executor((ctx, cmd) -> {
                        int page = ctx.getOption("page", 1);
                        new QueueCommand(this).execute(ctx, page);
                        return CommandResult.success();
                    });
        });

        commandService.registerCommand(this, builder -> {
            builder.name("nowplaying")
                    .description(getPluginLanguageManager().getString("music.command.nowplaying.description"))
                    .category("Music")
                    .aliases("np", "current")
                    .executor((ctx, cmd) -> {
                        new NowPlayingCommand(this).execute(ctx);
                        return CommandResult.success();
                    });
        });

        commandService.registerCommand(this, builder -> {
            builder.name("volume")
                    .description(getPluginLanguageManager().getString("music.command.volume.description"))
                    .category("Music")
                    .aliases("vol")
                    .integerOption("level", getPluginLanguageManager().getString("music.command.volume.option.level"), false, 0, 100)
                    .executor((ctx, cmd) -> {
                        Integer level = ctx.<Integer>getOption("level").orElse(null);
                        new VolumeCommand(this).execute(ctx, level);
                        return CommandResult.success();
                    });
        });

        commandService.registerCommand(this, builder -> {
            builder.name("loop")
                    .description(getPluginLanguageManager().getString("music.command.loop.description"))
                    .category("Music")
                    .stringOption(
                            "mode",
                            getPluginLanguageManager().getString("music.command.loop.option.mode"),
                            false,
                            OptionChoice.of(getPluginLanguageManager().getString("music.command.loop.mode.off"), "off"),
                            OptionChoice.of(getPluginLanguageManager().getString("music.command.loop.mode.track"), "track"),
                            OptionChoice.of(getPluginLanguageManager().getString("music.command.loop.mode.queue"), "queue")
                    )
                    .executor((ctx, cmd) -> {
                        String mode = ctx.getOption("mode", "toggle");
                        new LoopCommand(this).execute(ctx, mode);
                        return CommandResult.success();
                    });
        });

        commandService.registerCommand(this, builder -> {
            builder.name("shuffle")
                    .description(getPluginLanguageManager().getString("music.command.shuffle.description"))
                    .category("Music")
                    .executor((ctx, cmd) -> {
                        new ShuffleCommand(this).execute(ctx);
                        return CommandResult.success();
                    });
        });

        commandService.registerCommand(this, builder -> {
            builder.name("clear")
                    .description(getPluginLanguageManager().getString("music.command.clear.description"))
                    .category("Music")
                    .permission(permissionKey("admin"))
                    .executor((ctx, cmd) -> {
                        new ClearCommand(this).execute(ctx);
                        return CommandResult.success();
                    });
        });

        commandService.registerCommand(this, builder -> {
            builder.name("remove")
                    .description(getPluginLanguageManager().getString("music.command.remove.description"))
                    .category("Music")
                    .integerOption("position", getPluginLanguageManager().getString("music.command.remove.option.position"), true, 1, 1000)
                    .executor((ctx, cmd) -> {
                        int position = ctx.getRequiredOption("position");
                        new RemoveCommand(this).execute(ctx, position);
                        return CommandResult.success();
                    });
        });

        commandService.registerCommand(this, builder -> {
            builder.name("seek")
                    .description(getPluginLanguageManager().getString("music.command.seek.description"))
                    .category("Music")
                    .stringOption("time", getPluginLanguageManager().getString("music.command.seek.option.time"), true)
                    .executor((ctx, cmd) -> {
                        String time = ctx.getRequiredOption("time");
                        new SeekCommand(this).execute(ctx, time);
                        return CommandResult.success();
                    });
        });

        // Register more commands as needed...

        logger.info("Registered {} music commands", 12);
    }

    private void loadConfiguration() {
        int defaultVolume = getConfiguration().getInt("music.default_volume", 50);
        int maxQueue = getConfiguration().getInt("music.max_queue_size", 100);
        int maxTrackDurationMs = getConfiguration().getInt("music.max_track_duration", 600_000);
        boolean enableSpotify = getConfiguration().getBoolean("providers.spotify.enabled", true);
        boolean enableSoundcloud = getConfiguration().getBoolean("providers.soundcloud.enabled", true);
        int autoLeaveTimeoutMs = getConfiguration().getInt("music.auto_leave_timeout", 300_000);
        logger.info("Music config loaded: vol={}, queue={}, maxTrackMs={}, spotify={}, soundcloud={}, autoLeaveMs={}",
                defaultVolume, maxQueue, maxTrackDurationMs, enableSpotify, enableSoundcloud, autoLeaveTimeoutMs);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (musicManager != null) {
            musicManager.handleVoiceUpdate(event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        MusicPlayerMessage.ButtonInfo info = MusicPlayerMessage.parseButtonId(buttonId);

        if (info == null) {
            return;
        }

        // Handle music player button interactions
        new ButtonHandler(this).handleButton(event, info);
    }

    // Getters
    public MusicManager getMusicManager() {
        return musicManager;
    }

    public PlaylistManager getPlaylistManager() {
        return playlistManager;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}

// Internal simple permission implementation
class SimplePermission implements Permission {
    private final String name;
    private final String description;
    private final PermissionDefault def;

    public SimplePermission(String name, String description, PermissionDefault def) {
        this.name = name;
        this.description = description;
        this.def = def;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public PermissionDefault getDefault() {
        return def;
    }
}
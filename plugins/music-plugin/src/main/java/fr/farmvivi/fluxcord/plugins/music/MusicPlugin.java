package fr.farmvivi.fluxcord.plugins.music;

import fr.farmvivi.fluxcord.api.event.EventHandler;
import fr.farmvivi.fluxcord.api.event.EventPriority;
import fr.farmvivi.fluxcord.api.permissions.Permission;
import fr.farmvivi.fluxcord.api.permissions.PermissionDefault;
import fr.farmvivi.fluxcord.api.plugin.AbstractPlugin;
import fr.farmvivi.fluxcord.plugins.music.command.PauseCommand;
import fr.farmvivi.fluxcord.plugins.music.command.PlayCommand;
import fr.farmvivi.fluxcord.plugins.music.command.QueueCommand;
import fr.farmvivi.fluxcord.plugins.music.command.SkipCommand;
import fr.farmvivi.fluxcord.plugins.music.command.VolumeCommand;
import fr.farmvivi.fluxcord.plugins.music.command.StopCommand;
import fr.farmvivi.fluxcord.plugins.music.command.LoopCommand;
import fr.farmvivi.fluxcord.plugins.music.command.ShuffleCommand;
import fr.farmvivi.fluxcord.plugins.music.command.NowPlayingCommand;
import fr.farmvivi.fluxcord.plugins.music.command.ClearCommand;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

/**
 * Advanced music bot plugin for Fluxcord.
 * <p>
 * Features:
 * - Play music from YouTube, Spotify, SoundCloud
 * - Playlist management and queue controls
 * - Audio effects and volume control
 * - Rich embeds with now playing information
 * - Search functionality and favorites
 */
public class MusicPlugin extends AbstractPlugin {

    private MusicManager musicManager;
    private PlaylistManager playlistManager;
    
    // Command instances
    private PlayCommand playCommand;
    private PauseCommand pauseCommand;
    private SkipCommand skipCommand;
    private QueueCommand queueCommand;
    private VolumeCommand volumeCommand;
    private StopCommand stopCommand;
    private LoopCommand loopCommand;
    private ShuffleCommand shuffleCommand;
    private NowPlayingCommand nowPlayingCommand;
    private ClearCommand clearCommand;

    @Override
    public void onEnable() {
        logger.info("Music Plugin enabling...");

        // Register permissions
        registerPermissions();

        // Initialize managers
        this.musicManager = new MusicManager(this);
        this.playlistManager = new PlaylistManager(this);

        // Initialize commands
        initializeCommands();

        // Load configuration
        loadConfiguration();

        logger.info("Music Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        logger.info("Music Plugin disabling...");

        if (musicManager != null) {
            musicManager.shutdown();
        }

        if (playlistManager != null) {
            playlistManager.saveAllPlaylists();
        }

        logger.info("Music Plugin disabled!");
    }
    
    private void initializeCommands() {
        logger.info("Initializing music commands...");
        
        // Create command instances
        this.playCommand = new PlayCommand(this, musicManager.getAudioPlayerService(), musicManager.getVoiceChannelService());
        this.pauseCommand = new PauseCommand(this, musicManager.getAudioPlayerService());
        this.skipCommand = new SkipCommand(this, musicManager.getAudioPlayerService());
        this.queueCommand = new QueueCommand(this, musicManager.getAudioPlayerService());
        this.volumeCommand = new VolumeCommand(this, musicManager.getAudioPlayerService());
        this.stopCommand = new StopCommand(this, musicManager.getAudioPlayerService(), musicManager.getVoiceChannelService());
        this.loopCommand = new LoopCommand(this, musicManager.getAudioPlayerService());
        this.shuffleCommand = new ShuffleCommand(this, musicManager.getAudioPlayerService());
        this.nowPlayingCommand = new NowPlayingCommand(this, musicManager.getAudioPlayerService());
        this.clearCommand = new ClearCommand(this, musicManager.getAudioPlayerService());
        
        // TODO: Register commands with command service when command registration API is available
        // This would typically be done through a CommandService or similar API
        /*
        getCommandService().registerCommand("play", playCommand::execute)
            .description("Play music from URL or search query")
            .addOption("query", "Search query or URL", true)
            .addOption("next", "Play next in queue", false)
            .permission("musicplugin.play");
            
        getCommandService().registerCommand("pause", pauseCommand::execute)
            .description("Pause or resume music playback")
            .permission("musicplugin.play");
            
        getCommandService().registerCommand("skip", skipCommand::execute)
            .description("Skip the current track")
            .permission("musicplugin.skip");
            
        getCommandService().registerCommand("queue", queueCommand::execute)
            .description("Display the current music queue")
            .permission("musicplugin.queue");
            
        getCommandService().registerCommand("volume", volumeCommand::execute)
            .description("Control playback volume")
            .addOption("volume", "Volume level (0-100)", false)
            .permission("musicplugin.volume");
            
        getCommandService().registerCommand("stop", stopCommand::execute)
            .description("Stop playback and clear queue")
            .permission("musicplugin.admin");
            
        getCommandService().registerCommand("loop", loopCommand::execute)
            .description("Control loop modes")
            .addOption("mode", "Loop mode (off/track/queue)", false)
            .permission("musicplugin.queue");
            
        getCommandService().registerCommand("shuffle", shuffleCommand::execute)
            .description("Shuffle the queue")
            .permission("musicplugin.queue");
            
        getCommandService().registerCommand("nowplaying", nowPlayingCommand::execute)
            .description("Show current track information")
            .aliases("np", "current")
            .permission("musicplugin.queue");
            
        getCommandService().registerCommand("clear", clearCommand::execute)
            .description("Clear the music queue")
            .permission("musicplugin.admin");
        */
        
        logger.info("Music commands initialized (registration pending command API availability)");
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

    private void loadConfiguration() {
        int defaultVolume = getConfiguration().getInt("music.default_volume", 50);
        int maxQueue = getConfiguration().getInt("music.max_queue_size", 100);
        int maxTrackDurationMs = getConfiguration().getInt("music.max_track_duration", 600_000);
        boolean enableSpotify = getConfiguration().getBoolean("platforms.enable_spotify", true);
        boolean enableSoundcloud = getConfiguration().getBoolean("platforms.enable_soundcloud", true);
        int autoLeaveTimeoutMs = getConfiguration().getInt("voice.auto_leave_timeout", 300);
        logger.info("Music config loaded: vol={}, queue={}, maxTrackMs={}, spotify={}, soundcloud={}, autoLeaveMs={}",
                defaultVolume, maxQueue, maxTrackDurationMs, enableSpotify, enableSoundcloud, autoLeaveTimeoutMs);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (musicManager != null) {
            musicManager.handleVoiceUpdate(event);
        }
    }

    // Getters for managers (used by other classes)
    public MusicManager getMusicManager() {
        return musicManager;
    }

    public PlaylistManager getPlaylistManager() {
        return playlistManager;
    }
    
    // Getters for commands (for potential external access)
    public PlayCommand getPlayCommand() {
        return playCommand;
    }
    
    public PauseCommand getPauseCommand() {
        return pauseCommand;
    }
    
    public SkipCommand getSkipCommand() {
        return skipCommand;
    }
    
    public QueueCommand getQueueCommand() {
        return queueCommand;
    }
    
    public VolumeCommand getVolumeCommand() {
        return volumeCommand;
    }
    
    public StopCommand getStopCommand() {
        return stopCommand;
    }
    
    public LoopCommand getLoopCommand() {
        return loopCommand;
    }
    
    public ShuffleCommand getShuffleCommand() {
        return shuffleCommand;
    }
    
    public NowPlayingCommand getNowPlayingCommand() {
        return nowPlayingCommand;
    }
    
    public ClearCommand getClearCommand() {
        return clearCommand;
    }
}

// Internal simple permission implementation (mirrors template approach)
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
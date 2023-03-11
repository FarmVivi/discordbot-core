package fr.farmvivi.discordbot.module.music;

import com.github.topisenpai.lavasrc.applemusic.AppleMusicSourceManager;
import com.github.topisenpai.lavasrc.mirror.DefaultMirroringAudioTrackResolver;
import com.github.topisenpai.lavasrc.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.*;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.Configuration;
import fr.farmvivi.discordbot.jda.JDAManager;
import fr.farmvivi.discordbot.module.Module;
import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandsModule;
import fr.farmvivi.discordbot.module.music.command.*;
import fr.farmvivi.discordbot.module.music.command.equalizer.EqHighBassCommand;
import fr.farmvivi.discordbot.module.music.command.equalizer.EqStartCommand;
import fr.farmvivi.discordbot.module.music.command.equalizer.EqStopCommand;
import fr.farmvivi.discordbot.module.music.sourcemanager.SearchSourceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicModule extends Module {
    public static final String PLAYER_ID_PREFIX = "discordbot-music";
    public static final int QUIT_TIMEOUT = 900;
    public static final int DEFAULT_VOICE_VOLUME = 5;
    public static final int DEFAULT_RADIO_VOLUME = 25;

    private final Bot bot;
    private final MusicEventHandler musicEventHandler;
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    private final Map<String, MusicPlayer> players = new HashMap<>();

    public MusicModule(Bot bot) {
        super(Modules.MUSIC);

        this.bot = bot;
        this.musicEventHandler = new MusicEventHandler(this);
    }

    @Override
    public void onPreEnable() {
        super.onPreEnable();

        logger.info("Registering audio sources...");

        // Source providers

        // Remote sources

        // YouTube source provider
        YoutubeAudioSourceManager youtubeAudioSourceManager;
        try {
            String ytEmail = bot.getConfiguration().getValue("YOUTUBE_EMAIL");
            String ytPassword = bot.getConfiguration().getValue("YOUTUBE_PASSWORD");
            youtubeAudioSourceManager = new YoutubeAudioSourceManager(true, 1, ytEmail, ytPassword);
        } catch (Configuration.ValueNotFoundException e) {
            logger.warn("Playing restricted youtube videos will throws exceptions because no credentials provided : " + e.getLocalizedMessage());
            youtubeAudioSourceManager = new YoutubeAudioSourceManager(true, 1, null, null);
        }
        audioPlayerManager.registerSourceManager(youtubeAudioSourceManager);

        // Spotify source provider
        // create a new config
        try {
            String spotifyId = bot.getConfiguration().getValue("SPOTIFY_ID");
            String spotifyToken = bot.getConfiguration().getValue("SPOTIFY_TOKEN");

            // create a new SpotifySourceManager with the default providers
            audioPlayerManager.registerSourceManager(new SpotifySourceManager(spotifyId, spotifyToken, bot.getConfiguration().countryCode, 1, audioPlayerManager, new DefaultMirroringAudioTrackResolver(null)));
        } catch (Configuration.ValueNotFoundException e) {
            logger.warn("Could not initialise spotify source provider because, " + e.getLocalizedMessage());
        }

        // Apple Music source provider
        // create a new AppleMusicSourceManager with the default providers
        audioPlayerManager.registerSourceManager(new AppleMusicSourceManager(null, bot.getConfiguration().countryCode, 1, audioPlayerManager, new DefaultMirroringAudioTrackResolver(null)));

        // SoundCloud source provider
        SoundCloudDataReader dataReader = new DefaultSoundCloudDataReader();
        SoundCloudDataLoader dataLoader = new DefaultSoundCloudDataLoader();
        SoundCloudFormatHandler formatHandler = new DefaultSoundCloudFormatHandler();
        SoundCloudPlaylistLoader playlistLoader = new DefaultSoundCloudPlaylistLoader(dataLoader, dataReader, formatHandler);

        audioPlayerManager.registerSourceManager(new SoundCloudAudioSourceManager(true, 1, dataReader, dataLoader, formatHandler, playlistLoader));

        // Bandcamp source provider
        audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());

        // Vimeo source provider
        audioPlayerManager.registerSourceManager(new VimeoAudioSourceManager());

        // Twitch source provider
        audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());

        // GetYarn source provider
        audioPlayerManager.registerSourceManager(new GetyarnAudioSourceManager());

        // HTTP source provider
        audioPlayerManager.registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));

        // Local sources

        // Local source provider
        audioPlayerManager.registerSourceManager(new LocalAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));

        // Search source provider
        audioPlayerManager.registerSourceManager(new SearchSourceManager(youtubeAudioSourceManager, "ytsearch:"));

        audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        logger.info("Registering commands...");

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);

        Configuration botConfig = bot.getConfiguration();

        commandsModule.registerCommand(module, new PlayCommand(this));
        commandsModule.registerCommand(module, new NowCommand(this));
        commandsModule.registerCommand(module, new SkipCommand(this));
        commandsModule.registerCommand(module, new NextCommand(this));
        commandsModule.registerCommand(module, new ClearQueueCommand(this));
        commandsModule.registerCommand(module, new CurrentCommand(this));
        commandsModule.registerCommand(module, new StopCommand(this));
        commandsModule.registerCommand(module, new LeaveCommand());
        commandsModule.registerCommand(module, new PauseCommand(this));
        commandsModule.registerCommand(module, new LoopQueueCommand(this));
        commandsModule.registerCommand(module, new LoopCommand(this));
        commandsModule.registerCommand(module, new ShuffleCommand(this));
        commandsModule.registerCommand(module, new VolumeCommand(this));
        commandsModule.registerCommand(module, new SeekCommand(this));
        commandsModule.registerCommand(module, new ReplayCommand(this));
        commandsModule.registerCommand(module, new QueueCommand(this));
        commandsModule.registerCommand(module, new EqStartCommand(this));
        commandsModule.registerCommand(module, new EqStopCommand(this));
        commandsModule.registerCommand(module, new EqHighBassCommand(this));

        if (!botConfig.radioPath.equalsIgnoreCase(""))
            commandsModule.registerCommand(module, new RadioCommand(this, botConfig));
    }

    @Override
    public void onPostEnable() {
        super.onPostEnable();

        logger.info("Registering event listener...");

        JDAManager.getJDA().addEventListener(musicEventHandler);
    }

    @Override
    public void onPreDisable() {
        super.onPreDisable();

        logger.info("Deleting all music players messages...");
        for (MusicPlayer player : players.values()) {
            Message message = player.getMusicPlayerMessage().getMessage();
            if (message != null) {
                message.delete().queue();
            }
        }

        logger.info("Unregistering event listener...");

        JDAManager.getJDA().removeEventListener(musicEventHandler);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);

        commandsModule.unregisterCommands(module);

        audioPlayerManager.shutdown();
    }

    public synchronized MusicPlayer getPlayer(Guild guild) {
        if (!players.containsKey(guild.getId()))
            players.put(guild.getId(), new MusicPlayer(this, audioPlayerManager.createPlayer(), guild));
        return players.get(guild.getId());
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    public void loadTrack(Guild guild, String source, MessageChannelUnion messageChannel, CommandMessageBuilder reply, boolean playNow) {
        MusicPlayer player = getPlayer(guild);

        if (messageChannel != null) {
            player.getMusicPlayerMessage().setMessageChannel(messageChannel);
        }

        guild.getAudioManager().setSendingHandler(player.getAudioPlayerSendHandler());

        if (reply != null) {
            reply.setDiffer(true);
        }

        audioPlayerManager.loadItemOrdered(player, source, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                // Log : [<Guild name> (Guild id)] Track loaded: <Track name> (Link: <Track link>)
                logger.info(String.format("[%s (%s)] Track loaded : %s (Link: %s)", guild.getName(), guild.getId(), track.getInfo().title, track.getInfo().uri));

                if (reply != null) {
                    reply.addContent("**" + track.getInfo().title + "** ajouté à la file d'attente.");
                    reply.replyNow();
                }

                if (playNow) {
                    player.playTrackNow(track);
                } else {
                    player.playTrack(track);
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                StringBuilder builder = new StringBuilder();

                // If the playlist is a search result, we only want to queue the first track
                if (playlist.isSearchResult()) {
                    AudioTrack track = playlist.getTracks().get(0);

                    // Log : [<Guild name> (Guild id)] Track loaded (search): <Track name> (Link: <Track link>)
                    logger.info(String.format("[%s (%s)] Track loaded (search): %s (Link: %s)", guild.getName(), guild.getId(), track.getInfo().title, track.getInfo().uri));

                    builder.append("**").append(track.getInfo().title).append("** ajouté à la file d'attente.");

                    if (playNow) {
                        player.playTrackNow(track);
                    } else {
                        player.playTrack(track);
                    }
                }
                // Else we queue the whole playlist
                else {
                    List<AudioTrack> tracks = playlist.getTracks();

                    // Log : [<Guild name> (Guild id)] Playlist loaded: <Playlist name> (<Playlist size> tracks)
                    logger.info(String.format("[%s (%s)] Playlist loaded: %s (%d tracks)", guild.getName(), guild.getId(), playlist.getName(), tracks.size()));

                    builder.append("Ajout de la playlist **").append(playlist.getName()).append("** à la file d'attente (").append(playlist.getTracks().size()).append(" piste(s))");

                    for (AudioTrack track : playlist.getTracks()) {
                        if (playNow) {
                            player.playTrackNow(track);
                        } else {
                            player.playTrack(track);
                        }
                    }
                }

                if (reply != null) {
                    reply.addContent(builder.toString());
                    reply.replyNow();
                }
            }

            @Override
            public void noMatches() {
                // Log : [<Guild name> (Guild id)] Track not found: <Track name>
                logger.warn(String.format("[%s (%s)] Track not found: %s", guild.getName(), guild.getId(), source));

                // Notify the user that we've got nothing
                if (reply != null) {
                    reply.addContent("Impossible de trouver la piste demandée.");
                    reply.setEphemeral(true);
                    reply.replyNow();
                }
            }

            @Override
            public void loadFailed(FriendlyException throwable) {
                // Log : [<Guild name> (Guild id)] Track load failed: <Track name> (Reason: <Reason>)
                logger.error(String.format("[%s (%s)] Track load failed: %s (Reason: %s)", guild.getName(), guild.getId(), source, throwable.getMessage()));

                // Notify the user that everything exploded
                if (reply != null) {
                    reply.addContent("Impossible de charger la piste demandée.");
                    reply.setEphemeral(true);
                    reply.replyNow();
                }
            }
        });
    }

    public static String getDiscordID(Guild guild, String action) {
        return MusicModule.PLAYER_ID_PREFIX + "-" + guild.getId() + "-" + action;
    }

    public static String getGuildID(String discordID) {
        // discordID = discordbot-music-<guildID>-<action>
        int index = MusicModule.PLAYER_ID_PREFIX.length() + 1;
        return discordID.substring(index, discordID.indexOf("-", index));
    }

    public static String getAction(String discordID) {
        // discordID = discordbot-music-<guildID>-<action>
        int index = MusicModule.PLAYER_ID_PREFIX.length() + 1;
        return discordID.substring(discordID.indexOf("-", index) + 1);
    }
}

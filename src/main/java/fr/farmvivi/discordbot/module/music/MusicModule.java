package fr.farmvivi.discordbot.module.music;

import com.github.topi314.lavasrc.applemusic.AppleMusicSourceManager;
import com.github.topi314.lavasrc.deezer.DeezerAudioSourceManager;
import com.github.topi314.lavasrc.flowerytts.FloweryTTSSourceManager;
import com.github.topi314.lavasrc.mirror.DefaultMirroringAudioTrackResolver;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
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
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
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
import net.dv8tion.jda.api.EmbedBuilder;
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

    @Override
    public void onPreEnable() {
        super.onPreEnable();

        logger.info("Registering audio sources...");

        // Source providers

        // Remote sources

        // YouTube source provider
        //YoutubeAudioSourceManager youtubeAudioSourceManager;
        YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager(true,
                new MusicWithThumbnail(),
                new WebWithThumbnail(),
                new WebEmbeddedWithThumbnail(),
                new TvHtml5EmbeddedWithThumbnail(),
                new IosWithThumbnail(),
                new MediaConnectWithThumbnail(),
                new AndroidWithThumbnail(),
                new AndroidLiteWithThumbnail(),
                new AndroidMusicWithThumbnail(),
                new AndroidTestsuiteWithThumbnail()
        );
        try {
            String ytEmail = bot.getConfiguration().getValue("YOUTUBE_EMAIL");
            String ytPassword = bot.getConfiguration().getValue("YOUTUBE_PASSWORD");
            //youtubeAudioSourceManager = new YoutubeAudioSourceManager(true, ytEmail, ytPassword);
        } catch (Configuration.ValueNotFoundException e) {
            logger.warn("Playing restricted youtube videos will throws exceptions because no credentials provided : " + e.getLocalizedMessage());
            //youtubeAudioSourceManager = new YoutubeAudioSourceManager(true, null, null);
        }
        audioPlayerManager.registerSourceManager(youtubeAudioSourceManager);

        // Spotify source provider
        try {
            String spotifyId = bot.getConfiguration().getValue("SPOTIFY_ID");
            String spotifyToken = bot.getConfiguration().getValue("SPOTIFY_TOKEN");

            // create a new SpotifySourceManager with the default providers
            audioPlayerManager.registerSourceManager(new SpotifySourceManager(spotifyId, spotifyToken, bot.getConfiguration().countryCode, audioPlayerManager, new DefaultMirroringAudioTrackResolver(null)));
        } catch (Configuration.ValueNotFoundException e) {
            logger.warn("Could not initialise spotify source provider because, " + e.getLocalizedMessage());
        }

        // Deezer source provider
        try {
            String deezerMasterDecryptionKey = bot.getConfiguration().getValue("DEEZER_MASTER_DECRYPTION_KEY");

            // create a new DeezerAudioSourceManager with the default providers
            audioPlayerManager.registerSourceManager(new DeezerAudioSourceManager(deezerMasterDecryptionKey));
        } catch (Configuration.ValueNotFoundException e) {
            logger.warn("Could not initialise deezer source provider because, " + e.getLocalizedMessage());
        }

        // Apple Music source provider
        try {
            String appleMusicToken = bot.getConfiguration().getValue("APPLE_MUSIC_TOKEN");

            // create a new AppleMusicSourceManager with the default providers
            audioPlayerManager.registerSourceManager(new AppleMusicSourceManager(appleMusicToken, bot.getConfiguration().countryCode, audioPlayerManager, new DefaultMirroringAudioTrackResolver(null)));
        } catch (Configuration.ValueNotFoundException e) {
            logger.warn("Could not initialise apple music source provider because, " + e.getLocalizedMessage());
        }

        // Flowery TTS source provider
        try {
            String floweryTTSVoice = bot.getConfiguration().getValue("FLOWERY_TTS_VOICE");

            // create a new FloweryTTSSourceManager with the default providers
            audioPlayerManager.registerSourceManager(new FloweryTTSSourceManager(floweryTTSVoice));
        } catch (Configuration.ValueNotFoundException e) {
            logger.warn("Could not initialise flowery tts source provider because, " + e.getLocalizedMessage());
        }

        // SoundCloud source provider
        SoundCloudDataReader dataReader = new DefaultSoundCloudDataReader();
        SoundCloudDataLoader dataLoader = new DefaultSoundCloudDataLoader();
        SoundCloudFormatHandler formatHandler = new DefaultSoundCloudFormatHandler();
        SoundCloudPlaylistLoader playlistLoader = new DefaultSoundCloudPlaylistLoader(dataLoader, dataReader, formatHandler);

        audioPlayerManager.registerSourceManager(new SoundCloudAudioSourceManager(true, dataReader, dataLoader, formatHandler, playlistLoader));

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
        commandsModule.registerCommand(module, new LeaveCommand(this));
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
                // Log : [<Guild name> (Guild id)] Track loaded: "Track name" (Link)
                logger.info(String.format("[%s (%s)] Track loaded : \"%s\" (%s)", guild.getName(), guild.getId(), track.getInfo().title, track.getInfo().uri));

                if (reply != null) {
                    //reply.addContent(String.format("[%s](%s) ajouté à la file d'attente.", track.getInfo().title, track.getInfo().uri));
                    EmbedBuilder embedBuilder = reply.createSuccessEmbed();
                    embedBuilder.setTitle("Musique ajoutée à la file d'attente");
                    if (track.getInfo().artworkUrl != null) {
                        embedBuilder.setThumbnail(track.getInfo().artworkUrl);
                    }
                    embedBuilder.addField("Titre", String.format("[%s](%s)", track.getInfo().title, track.getInfo().uri), false);
                    reply.addEmbeds(embedBuilder.build());
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
                // If the playlist is a search result, we only want to queue the first track
                if (playlist.isSearchResult()) {
                    AudioTrack track = playlist.getTracks().get(0);

                    // Log : [<Guild name> (Guild id)] Track loaded (search): "Track name" (Link)
                    logger.info(String.format("[%s (%s)] Track loaded (search): \"%s\" (%s)", guild.getName(), guild.getId(), track.getInfo().title, track.getInfo().uri));

                    //builder.append(String.format("[%s](%s) ajouté à la file d'attente.", track.getInfo().title, track.getInfo().uri));
                    if (reply != null) {
                        EmbedBuilder embedBuilder = reply.createSuccessEmbed();
                        embedBuilder.setTitle("Musique ajoutée à la file d'attente");
                        if (track.getInfo().artworkUrl != null) {
                            embedBuilder.setThumbnail(track.getInfo().artworkUrl);
                        }
                        embedBuilder.addField("Titre", String.format("[%s](%s)", track.getInfo().title, track.getInfo().uri), false);
                        reply.addEmbeds(embedBuilder.build());
                        reply.replyNow();
                    }

                    if (playNow) {
                        player.playTrackNow(track);
                    } else {
                        player.playTrack(track);
                    }
                }
                // Else we queue the whole playlist
                else {
                    List<AudioTrack> tracks = playlist.getTracks();

                    // Log : [<Guild name> (Guild id)] Playlist loaded: "Playlist name" (Link) (<Playlist size> tracks)
                    logger.info(String.format("[%s (%s)] Playlist loaded: \"%s\" (%s) (%d tracks)", guild.getName(), guild.getId(), playlist.getName(), source, tracks.size()));

                    //builder.append(String.format("Ajout de la playlist [%s](%s) à la file d'attente (%d piste(s))", playlist.getName(), source, playlist.getTracks().size()));
                    if (reply != null) {
                        EmbedBuilder embedBuilder = reply.createSuccessEmbed();
                        embedBuilder.setTitle("Playlist ajoutée à la file d'attente");
                        embedBuilder.addField("Titre", String.format("[%s](%s)", playlist.getName(), source), false);
                        embedBuilder.addField("Nombre de pistes", String.valueOf(playlist.getTracks().size()), false);
                        reply.addEmbeds(embedBuilder.build());
                        reply.replyNow();
                    }

                    for (AudioTrack track : playlist.getTracks()) {
                        if (playNow) {
                            player.playTrackNow(track);
                        } else {
                            player.playTrack(track);
                        }
                    }
                }
            }

            @Override
            public void noMatches() {
                // Log : [<Guild name> (Guild id)] Track not found: <Track name>
                logger.warn(String.format("[%s (%s)] Track not found: %s", guild.getName(), guild.getId(), source));

                // Notify the user that we've got nothing
                if (reply != null) {
                    reply.error("Impossible de trouver la piste demandée.");
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
                    reply.error("Impossible de charger la piste demandée.");
                    reply.setEphemeral(true);
                    reply.replyNow();
                }
            }
        });
    }
}

package fr.farmvivi.discordbot.module.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
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
import fr.farmvivi.discordbot.module.music.spotify.LinkConverter;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicModule extends Module {
    public static final int QUIT_TIMEOUT = 900;
    public static final int DEFAULT_VOICE_VOLUME = 5;
    public static final int DEFAULT_RADIO_VOLUME = 25;

    private final Modules module;
    private final Bot bot;
    private final MusicListener musicListener;
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    private final Map<String, MusicPlayer> players = new HashMap<>();

    public MusicModule(Modules module, Bot bot) {
        super(module);

        this.module = module;
        this.bot = bot;
        this.musicListener = new MusicListener(this);

        // Remote sources
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        // Local source
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);

        Configuration botConfig = bot.getConfiguration();

        commandsModule.registerCommand(module, new PlayCommand(this));
        commandsModule.registerCommand(module, new NowCommand(this));
        commandsModule.registerCommand(module, new SkipCommand(this));
        commandsModule.registerCommand(module, new NextCommand(this));
        commandsModule.registerCommand(module, new ClearCommand(this));
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
        commandsModule.registerCommand(module, new ViewQueueCommand(this));
        commandsModule.registerCommand(module, new EqStartCommand(this));
        commandsModule.registerCommand(module, new EqStopCommand(this));
        commandsModule.registerCommand(module, new EqHighBassCommand(this));

        if (!botConfig.radioPath.equalsIgnoreCase(""))
            commandsModule.registerCommand(module, new RadioCommand(this, botConfig));

        JDAManager.getJDA().addEventListener(musicListener);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);

        commandsModule.unregisterCommands(module);

        JDAManager.getJDA().removeEventListener(musicListener);

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

    public void loadTrack(Guild guild, String source) {
        this.loadTrack(guild, source, null);
    }

    public void loadTrack(Guild guild, String source, boolean playNow) {
        this.loadTrack(guild, source, null, playNow);
    }

    public void loadTrack(Guild guild, String source, CommandMessageBuilder reply) {
        this.loadTrack(guild, source, reply, false);
    }

    public void loadTrack(Guild guild, String source, CommandMessageBuilder reply, boolean playNow) {
        MusicPlayer player = getPlayer(guild);

        guild.getAudioManager().setSendingHandler(player.getAudioPlayerSendHandler());

        if (reply != null) {
            reply.setDiffer(true);
        }

        if ((source.startsWith("http") && source.contains("://")) || source.startsWith("/") || source.startsWith("./")) {
            if (source.contains("open.spotify.com")) {
                LinkConverter linkConverter = new LinkConverter();
                try {
                    List<String> songs = linkConverter.convert(source);
                    if (reply != null) {
                        reply.setDiffer(false);
                        if (songs == null) {
                            reply.addContent("> _Erreur : Lien non supporté_");
                            reply.setEphemeral(true);
                            return;
                        }

                        reply.addContent("**" + source + "** ajouté à la file d'attente.");
                    }
                    for (String songName : songs)
                        audioPlayerManager.loadItem("ytmsearch:" + songName,
                                new FunctionalResultHandler(null, playlist -> {
                                    if (playNow) {
                                        player.playTrackNow(playlist.getTracks().get(0));
                                    } else {
                                        player.playTrack(playlist.getTracks().get(0));
                                    }
                                }, null, null));
                } catch (ParseException | SpotifyWebApiException | IOException e) {
                    logger.error("Unable to get tracks from " + source, e);
                }
            } else {
                audioPlayerManager.loadItemOrdered(player, source, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
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
                        builder.append("Ajout de la playlist **").append(playlist.getName()).append("** :");

                        for (AudioTrack track : playlist.getTracks()) {
                            builder.append("\n-> **").append(track.getInfo().title).append("**");

                            if (playNow) {
                                player.playTrackNow(track);
                            } else {
                                player.playTrack(track);
                            }
                        }

                        if (reply != null) {
                            reply.addContent(builder.toString());
                            reply.replyNow();
                        }
                    }

                    @Override
                    public void noMatches() {
                        // Notify the user that we've got nothing
                        if (reply != null) {
                            reply.addContent("La piste " + source + " n'a pas été trouvé.");
                            reply.setEphemeral(true);
                            reply.replyNow();
                        } else {
                            logger.warn("La piste " + source + " n'a pas été trouvé.");
                        }
                    }

                    @Override
                    public void loadFailed(FriendlyException throwable) {
                        // Notify the user that everything exploded
                        if (reply != null) {
                            reply.addContent("Impossible de jouer la piste (raison: " + throwable.getMessage() + ").");
                            reply.setEphemeral(true);
                            reply.replyNow();
                        } else {
                            logger.warn("Impossible de jouer la piste.", throwable);
                        }
                    }
                });
            }
        } else {
            audioPlayerManager.loadItem("ytmsearch:" + source, new FunctionalResultHandler(null, playlist -> {
                if (reply != null) {
                    reply.addContent("**" + playlist.getTracks().get(0).getInfo().title + "** ajouté à la file d'attente.");
                    reply.replyNow();
                }

                if (playNow) {
                    player.playTrackNow(playlist.getTracks().get(0));
                } else {
                    player.playTrack(playlist.getTracks().get(0));
                }
            }, null, null));
        }
    }
}

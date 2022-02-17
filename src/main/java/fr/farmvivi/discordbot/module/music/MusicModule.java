package fr.farmvivi.discordbot.module.music;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import org.apache.hc.core5.http.ParseException;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.Configuration;
import fr.farmvivi.discordbot.jda.JDAManager;
import fr.farmvivi.discordbot.module.Module;
import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.commands.CommandsModule;
import fr.farmvivi.discordbot.module.music.command.ClearCommand;
import fr.farmvivi.discordbot.module.music.command.CurrentCommand;
import fr.farmvivi.discordbot.module.music.command.LeaveCommand;
import fr.farmvivi.discordbot.module.music.command.LoopCommand;
import fr.farmvivi.discordbot.module.music.command.LoopQueueCommand;
import fr.farmvivi.discordbot.module.music.command.NextCommand;
import fr.farmvivi.discordbot.module.music.command.NowCommand;
import fr.farmvivi.discordbot.module.music.command.PauseCommand;
import fr.farmvivi.discordbot.module.music.command.PlayCommand;
import fr.farmvivi.discordbot.module.music.command.RadioCommand;
import fr.farmvivi.discordbot.module.music.command.ReplayCommand;
import fr.farmvivi.discordbot.module.music.command.SeekCommand;
import fr.farmvivi.discordbot.module.music.command.ShuffleCommand;
import fr.farmvivi.discordbot.module.music.command.SkipCommand;
import fr.farmvivi.discordbot.module.music.command.StopCommand;
import fr.farmvivi.discordbot.module.music.command.ViewQueueCommand;
import fr.farmvivi.discordbot.module.music.command.VolumeCommand;
import fr.farmvivi.discordbot.module.music.command.equalizer.EqHighBassCommand;
import fr.farmvivi.discordbot.module.music.command.equalizer.EqStartCommand;
import fr.farmvivi.discordbot.module.music.command.equalizer.EqStopCommand;
import fr.farmvivi.discordbot.module.music.spotify.LinkConverter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

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
    public void enable() {
        super.enable();

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager()
                .getModule(Modules.COMMANDS);

        Configuration botConfig = bot.getConfiguration();

        commandsModule.registerCommand(module, new PlayCommand(this, botConfig));
        commandsModule.registerCommand(module, new NowCommand(this, botConfig));
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
        commandsModule.registerCommand(module, new SeekCommand(this, botConfig));
        commandsModule.registerCommand(module, new ReplayCommand(this));
        commandsModule.registerCommand(module, new ViewQueueCommand(this));
        commandsModule.registerCommand(module, new EqStartCommand(this));
        commandsModule.registerCommand(module, new EqStopCommand(this));
        commandsModule.registerCommand(module, new EqHighBassCommand(this));

        if (!botConfig.radioPath.equalsIgnoreCase(""))
            commandsModule.registerCommand(module, new RadioCommand(this, botConfig));

        JDAManager.getShardManager().addEventListener(musicListener);
    }

    @Override
    public void disable() {
        super.disable();

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager()
                .getModule(Modules.COMMANDS);

        commandsModule.unregisterCommands(module);

        JDAManager.getShardManager().removeEventListener(musicListener);

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

    public void loadTrack(Guild guild, String source, TextChannel textChannel) {
        this.loadTrack(guild, source, textChannel, false);
    }

    public void loadTrack(Guild guild, String source, TextChannel textChannel, boolean playNow) {
        MusicPlayer player = getPlayer(guild);

        guild.getAudioManager().setSendingHandler(player.getAudioPlayerSendHandler());

        if ((source.startsWith("http") && source.contains("://")) || source.startsWith("/")
                || source.startsWith("./")) {
            if (source.contains("open.spotify.com")) {
                LinkConverter linkConverter = new LinkConverter();
                try {
                    List<String> songs = linkConverter.convert(source);
                    for (String songName : songs)
                        audioPlayerManager.loadItem("ytsearch: " + songName,
                                new FunctionalResultHandler(null, playlist -> {
                                    if (textChannel != null)
                                        textChannel.sendMessage("**" + playlist.getTracks().get(0).getInfo().title
                                                + "** ajouté à la file d'attente.").queue();
                                    player.playTrack(playlist.getTracks().get(0), playNow);
                                }, null, null));
                } catch (ParseException | SpotifyWebApiException | IOException e) {
                    logger.error("Unable to get tracks from " + source, e);
                }
            } else {
                audioPlayerManager.loadItemOrdered(player, source, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        if (textChannel != null)
                            textChannel.sendMessage("**" + track.getInfo().title + "** ajouté à la file d'attente.")
                                    .queue();
                        player.playTrack(track, playNow);
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("Ajout de la playlist **").append(playlist.getName()).append("** :");

                        for (AudioTrack track : playlist.getTracks()) {
                            builder.append("\n-> **").append(track.getInfo().title).append("**");
                            player.playTrack(track, playNow);
                        }

                        if (textChannel != null)
                            textChannel.sendMessage(builder.toString()).queue();
                    }

                    @Override
                    public void noMatches() {
                        // Notify the user that we've got nothing
                        if (textChannel != null)
                            textChannel.sendMessage("La piste " + source + " n'a pas été trouvé.").queue();
                        else
                            logger.warn("La piste " + source + " n'a pas été trouvé.");
                    }

                    @Override
                    public void loadFailed(FriendlyException throwable) {
                        // Notify the user that everything exploded
                        if (textChannel != null)
                            textChannel
                                    .sendMessage(
                                            "Impossible de jouer la piste (raison: " + throwable.getMessage() + ").")
                                    .queue();
                        else
                            logger.warn("Impossible de jouer la piste.", throwable);
                    }
                });
            }
        } else {
            audioPlayerManager.loadItem("ytsearch: " + source, new FunctionalResultHandler(null, playlist -> {
                if (textChannel != null)
                    textChannel.sendMessage(
                            "**" + playlist.getTracks().get(0).getInfo().title + "** ajouté à la file d'attente.")
                            .queue();
                player.playTrack(playlist.getTracks().get(0), playNow);
            }, null, null));
        }
    }
}

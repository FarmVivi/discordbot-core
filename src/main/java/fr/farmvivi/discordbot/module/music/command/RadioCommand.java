package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.Configuration;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import fr.farmvivi.discordbot.module.music.MusicPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class RadioCommand extends MusicCommand {
    private final Configuration botConfig;

    public RadioCommand(MusicModule musicModule, Configuration botConfig) {
        super(musicModule, "radio", CommandCategory.MUSIC, "Affiche/Joue une playlist préchargée");

        OptionData playlistOption = new OptionData(OptionType.STRING, "playlist", "Nom de la playlist", false);

        this.setArgs(new OptionData[]{playlistOption});

        this.botConfig = botConfig;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        Guild guild = event.getGuild();

        if (args.containsKey("playlist")) {
            String playlist = args.get("playlist").getAsString();
            playRadio(guild, botConfig.radioPath + File.separator + playlist + ".m3u");
            reply.addContent("Playlist **" + playlist + "** en cours de lecture.");
        } else {
            displayRadio(reply);
        }

        return true;
    }

    private void displayRadio(CommandMessageBuilder reply) {
        reply.setEphemeral(true);

        File directory = new File(botConfig.radioPath);
        if (!directory.exists()) {
            reply.addContent("Une erreur est survenue");
            return;
        }

        StringBuilder builder = new StringBuilder("Playlists :");

        for (File file : directory.listFiles())
            if (file.getName().endsWith(".m3u"))
                builder.append("\n- **").append(file.getName().replace(".m3u", "")).append("**");

        reply.addContent(builder.toString());
    }

    private void playRadio(Guild guild, String uri) {
        MusicPlayer musicPlayer = musicModule.getPlayer(guild);

        musicPlayer.getAudioPlayer().setPaused(false);
        musicPlayer.clearQueue();
        musicPlayer.skipTrack();

        musicPlayer.resetToDefaultSettings();
        musicPlayer.setVolume(MusicModule.DEFAULT_RADIO_VOLUME);
        musicPlayer.setLoopQueueMode(true);
        musicPlayer.setShuffleMode(true);

        if (uri.endsWith(".m3u")) {
            try (BufferedReader br = new BufferedReader(new FileReader(uri))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("http")) {
                        musicModule.loadTrack(guild, line, null, null, false);
                    } else {
                        String musicFile = uri.substring(0, uri.lastIndexOf(File.separator) + 1) + line;
                        musicModule.getLogger().info("Adding radio track: " + musicFile);
                        musicModule.loadTrack(guild, musicFile, null, null, false);
                    }
                }
            } catch (IOException ex) {
                musicModule.getLogger().error("Exception", ex);
            }
        }
    }
}

package fr.farmvivi.discordbot.module.music.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import fr.farmvivi.discordbot.Configuration;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.music.MusicModule;
import fr.farmvivi.discordbot.module.music.MusicPlayer;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RadioCommand extends Command {
    private final MusicModule musicModule;
    private final Configuration botConfig;

    public RadioCommand(MusicModule musicModule, Configuration botConfig) {
        this.name = "radio";
        this.category = CommandCategory.MUSIC;
        this.description = "Lance le bot en mode radio";
        this.args = "<radio>|<list>";

        this.musicModule = musicModule;
        this.botConfig = botConfig;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        TextChannel textChannel = event.getChannel().asTextChannel();
        Guild guild = textChannel.getGuild();

        if (!guild.getAudioManager().isConnected()) {
            AudioChannel voiceChannel = guild.getMember(event.getAuthor()).getVoiceState().getChannel();
            if (voiceChannel == null) {
                textChannel.sendMessage("Vous devez être connecté à un salon vocal.").queue();
                return false;
            }
            guild.getAudioManager().openAudioConnection(voiceChannel);
            guild.getAudioManager().setAutoReconnect(true);
        }

        if (content.equalsIgnoreCase("") || content.equalsIgnoreCase("list"))
            displayRadio(textChannel);
        else
            playRadio(guild, botConfig.radioPath + "/" + content + ".m3u");

        return true;
    }

    private void displayRadio(TextChannel textChannel) {
        StringBuilder builder = new StringBuilder("Radio:");

        for (File file : new File(botConfig.radioPath).listFiles())
            if (file.getName().endsWith(".m3u"))
                builder.append("\n- **").append(file.getName().replace(".m3u", "")).append("**");

        textChannel.sendMessage(builder.toString()).queue();
    }

    private void playRadio(Guild guild, String uri) {
        MusicPlayer musicPlayer = musicModule.getPlayer(guild);

        musicPlayer.getAudioPlayer().setPaused(false);
        musicPlayer.getListener().getTracks().clear();
        musicPlayer.skipTrack();

        musicPlayer.resetToDefaultSettings();
        musicPlayer.getAudioPlayer().setVolume(MusicModule.DEFAULT_RADIO_VOLUME);
        musicPlayer.setLoopQueueMode(true);
        musicPlayer.setShuffleMode(true);

        if ((uri.startsWith("/") || uri.startsWith("./") && uri.endsWith(".m3u"))) {
            try (BufferedReader br = new BufferedReader(new FileReader(new File(uri)))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("http")) {
                        musicModule.loadTrack(guild, line);
                    } else {
                        String musicFile = uri.substring(0, uri.lastIndexOf("/") + 1) + line;
                        musicModule.getLogger().info("Adding radio track: " + musicFile);
                        musicModule.loadTrack(guild, musicFile);
                    }
                }
            } catch (IOException ex) {
                musicModule.getLogger().error("Exception", ex);
            }
        }
    }
}

package fr.farmvivi.animecity.command.music;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import fr.farmvivi.animecity.command.CommandCategory;
import fr.farmvivi.animecity.music.MusicManager;
import fr.farmvivi.animecity.music.MusicPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RadioCommand extends Command {
    public static final Logger logger = LoggerFactory.getLogger(RadioCommand.class);

    public RadioCommand() {
        this.name = "radio";
        this.category = CommandCategory.MUSIC;
        this.description = "Lance le bot en mode radio";
        this.args = "<radio>|<list>";
    }

    @Override
    protected boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        final TextChannel textChannel = event.getTextChannel();
        final Guild guild = textChannel.getGuild();

        if (!guild.getAudioManager().isConnected()) {
            final VoiceChannel voiceChannel = guild.getMember(event.getAuthor()).getVoiceState().getChannel();
            if (voiceChannel == null) {
                textChannel.sendMessage("Vous devez être connecté à un salon vocal.").queue();
                return false;
            }
            Bot.logger.info("Join channel " + voiceChannel.getName() + "...");
            guild.getAudioManager().openAudioConnection(voiceChannel);
            guild.getAudioManager().setAutoReconnect(true);
        }

        if (content.equalsIgnoreCase("") || content.equalsIgnoreCase("list"))
            displayRadio(textChannel);
        else
            playRadio(guild, Bot.getInstance().getConfiguration().radioPath + "/" + content + ".m3u");

        return true;
    }

    private void displayRadio(TextChannel textChannel) {
        final StringBuilder builder = new StringBuilder("Radio:");

        for (File file : new File(Bot.getInstance().getConfiguration().radioPath).listFiles())
            if (file.getName().endsWith(".m3u"))
                builder.append("\n- **").append(file.getName().replace(".m3u", "")).append("**");

        textChannel.sendMessage(builder.toString()).queue();
    }

    private void playRadio(Guild guild, String uri) {
        final MusicManager musicManager = Bot.getInstance().getMusicManager();
        final MusicPlayer musicPlayer = musicManager.getPlayer(guild);

        musicPlayer.getListener().getTracks().clear();
        musicPlayer.skipTrack();

        musicPlayer.resetToDefaultSettings();
        musicPlayer.getAudioPlayer().setVolume(MusicManager.DEFAULT_RADIO_VOLUME);
        musicPlayer.setLoopQueueMode(true);
        musicPlayer.setShuffleMode(true);

        if ((uri.startsWith("/") || uri.startsWith("./") && uri.endsWith(".m3u"))) {
            try (BufferedReader br = new BufferedReader(new FileReader(new File(uri)))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("http")) {
                        musicManager.loadTrack(guild, line);
                    } else {
                        String musicFile = uri.substring(0, uri.lastIndexOf("/") + 1) + line;
                        logger.info("Adding radio track: " + musicFile);
                        musicManager.loadTrack(guild, musicFile);
                    }
                }
            } catch (IOException ex) {
                logger.error("Exception", ex);
            }
        }
    }
}

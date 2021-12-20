package fr.farmvivi.animecity.command.music;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import fr.farmvivi.animecity.command.CommandCategory;
import fr.farmvivi.animecity.music.MusicManager;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PlayCommand extends Command {
    public PlayCommand() {
        this.name = "play";
        this.aliases = new String[] { "p" };
        this.category = CommandCategory.MUSIC;
        this.description = "Ajoute une musique à la file d'attente";
        this.args = "<name>|<url>";
    }

    @Override
    protected boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;
        if (args != null && content.length() == 0) {
            event.getChannel().sendMessage("Utilisation de la commande: **"
                    + Bot.getInstance().getConfiguration().cmdPrefix + name + " " + args + "**").queue();
            return false;
        }

        final TextChannel textChannel = event.getTextChannel();
        final Guild guild = textChannel.getGuild();
        final MusicManager musicManager = Bot.getInstance().getMusicManager();

        if (!guild.getAudioManager().isConnected()) {
            final AudioChannel voiceChannel = guild.getMember(event.getAuthor()).getVoiceState().getChannel();
            if (voiceChannel == null) {
                textChannel.sendMessage("Vous devez être connecté à un salon vocal.").queue();
                return false;
            }
            Bot.logger.info("Join channel " + voiceChannel.getName() + "...");
            guild.getAudioManager().openAudioConnection(voiceChannel);
            guild.getAudioManager().setAutoReconnect(true);
            musicManager.getPlayer(guild).getAudioPlayer().setVolume(MusicManager.DEFAULT_VOICE_VOLUME);
        }

        if (musicManager.getPlayer(guild).getAudioPlayer().isPaused()) {
            musicManager.getPlayer(guild).getAudioPlayer().setPaused(false);
            textChannel.sendMessage("Lecture !").queue();
        }

        musicManager.loadTrack(textChannel.getGuild(), content, textChannel);

        return true;
    }
}

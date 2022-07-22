package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.Configuration;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PlayCommand extends Command {
    private final MusicModule musicModule;
    private final Configuration botConfig;

    public PlayCommand(MusicModule musicModule, Configuration botConfig) {
        this.name = "play";
        this.aliases = new String[] { "p" };
        this.category = CommandCategory.MUSIC;
        this.description = "Ajoute une musique à la file d'attente";
        this.args = "<name>|<url>";

        this.musicModule = musicModule;
        this.botConfig = botConfig;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;
        if (args != null && content.length() == 0) {
            event.getChannel().sendMessage("Utilisation de la commande: **"
                    + botConfig.cmdPrefix + name + " " + args + "**").queue();
            return false;
        }

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
            musicModule.getPlayer(guild).getAudioPlayer().setVolume(MusicModule.DEFAULT_VOICE_VOLUME);
        }

        if (musicModule.getPlayer(guild).getAudioPlayer().isPaused()) {
            musicModule.getPlayer(guild).getAudioPlayer().setPaused(false);
            textChannel.sendMessage("Lecture !").queue();
        }

        musicModule.loadTrack(textChannel.getGuild(), content, textChannel);

        return true;
    }
}

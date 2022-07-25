package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.Configuration;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class PlayCommand extends Command {
    private final MusicModule musicModule;
    private final Configuration botConfig;

    public PlayCommand(MusicModule musicModule, Configuration botConfig) {
        super("play", CommandCategory.MUSIC, "Ajoute une musique à la file d'attente", new OptionData[]{
                new OptionData(OptionType.STRING, "name_or_url", "URL ou nom de la musique à jouer maintenant")}, new String[]{"p"});

        this.musicModule = musicModule;
        this.botConfig = botConfig;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;
        if (this.getArgs().length > 0 && content.length() == 0) {
            event.getChannel().sendMessage("Utilisation de la commande: **"
                    + botConfig.cmdPrefix + this.getName() + " " + this.getArgsAsString() + "**").queue();
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

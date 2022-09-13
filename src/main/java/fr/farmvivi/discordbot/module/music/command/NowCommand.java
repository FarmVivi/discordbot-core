package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class NowCommand extends Command {
    private final MusicModule musicModule;

    public NowCommand(MusicModule musicModule) {
        super("now", CommandCategory.MUSIC, "Ajoute une musique en haut de la file d'attente", new OptionData[]{
                new OptionData(OptionType.STRING, "requête", "Musique à ajouter à la file d'attente")}, new String[]{"n"});

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content, CommandMessageBuilder reply) {
        if (!super.execute(event, content, reply))
            return false;
        if (this.getArgs().length > 0 && content.length() == 0) {
            reply.addContent("Utilisation de la commande: **/" + this.getName() + " " + this.getArgsAsString() + "**");
            return false;
        }

        Guild guild = event.getGuild();

        if (!guild.getAudioManager().isConnected()) {
            AudioChannel voiceChannel = guild.getMember(event.getAuthor()).getVoiceState().getChannel();
            if (voiceChannel == null) {
                reply.addContent("Vous devez être connecté à un salon vocal.");
                return false;
            }
            guild.getAudioManager().openAudioConnection(voiceChannel);
            guild.getAudioManager().setAutoReconnect(true);
            musicModule.getPlayer(guild).getAudioPlayer().setVolume(MusicModule.DEFAULT_VOICE_VOLUME);
        }

        if (musicModule.getPlayer(guild).getAudioPlayer().isPaused()) {
            musicModule.getPlayer(guild).getAudioPlayer().setPaused(false);
            reply.addContent("Lecture !");
        }

        musicModule.loadTrack(guild, content, reply, true);

        return true;
    }
}

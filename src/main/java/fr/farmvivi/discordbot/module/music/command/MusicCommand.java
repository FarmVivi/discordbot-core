package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public abstract class MusicCommand extends Command {
    protected final MusicModule musicModule;

    public MusicCommand(MusicModule musicModule, String name, CommandCategory category, String description) {
        super(name, category, description);

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        // Check if member exists
        Member member = event.getMember();
        if (member == null) {
            reply.addContent("Une erreur est survenue, veuillez réessayer.");
            return false;
        }

        // Check if member is connected to a voice channel
        GuildVoiceState memberVoiceState = member.getVoiceState();
        if (memberVoiceState == null || !memberVoiceState.inAudioChannel()) {
            reply.addContent("Connectez-vous à un salon vocal pour exécuter cette commande.");
            return false;
        }

        // Check if bot is connected to a voice channel, if not, join the member's voice channel
        GuildVoiceState botVoiceState = event.getGuild().getSelfMember().getVoiceState();
        if (botVoiceState == null || !botVoiceState.inAudioChannel()) {
            Guild guild = event.getGuild();
            guild.getAudioManager().openAudioConnection(memberVoiceState.getChannel());
            guild.getAudioManager().setAutoReconnect(true);
            musicModule.getPlayer(guild).setVolume(MusicModule.DEFAULT_VOICE_VOLUME);
            return true;
        }

        // Check if member and bot are in the same voice channel
        if (!memberVoiceState.getChannel().equals(botVoiceState.getChannel())) {
            reply.addContent("Connectez-vous au même salon vocal que le bot pour exécuter cette commande.");
            return false;
        }

        return true;
    }

    public MusicModule getMusicModule() {
        return musicModule;
    }
}

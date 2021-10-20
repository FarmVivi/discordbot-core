package fr.farmvivi.animecity.command.music;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import fr.farmvivi.animecity.command.CommandCategory;
import fr.farmvivi.animecity.music.MusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LeaveCommand extends Command {
    public LeaveCommand() {
        this.name = "leave";
        this.aliases = new String[] { "disconnect" };
        this.category = CommandCategory.MUSIC;
        this.description = "Déconnecte le bot du channel";
    }

    @Override
    protected boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        final TextChannel textChannel = event.getTextChannel();
        final Guild guild = textChannel.getGuild();
        final MusicManager musicManager = Bot.getInstance().getMusicManager();

        if (!guild.getAudioManager().isConnected()) {
            textChannel.sendMessage("Le bot n'est pas connecté à un salon vocal.").queue();
            return false;
        }

        musicManager.getPlayer(guild).getListener().getTracks().clear();
        musicManager.getPlayer(guild).skipTrack();
        musicManager.getPlayer(guild).resetToDefaultSettings();
        Bot.logger.info("Quit channel " + guild.getAudioManager().getConnectedChannel().getName() + "...");
        guild.getAudioManager().closeAudioConnection();

        textChannel.sendMessage("Déconnecté.").queue();

        return true;
    }
}

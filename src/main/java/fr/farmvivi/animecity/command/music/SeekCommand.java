package fr.farmvivi.animecity.command.music;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import fr.farmvivi.animecity.command.CommandsManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SeekCommand extends Command {
    public SeekCommand(){
        this.name = "seek";
        this.args = "<time>";
    }

    @Override
    protected void execute(MessageReceivedEvent event, String content) {
        if (args != null && content.length() == 0) {
            event.getChannel()
                    .sendMessage(
                            "Utilisation de la commande: **" + CommandsManager.CMD_PREFIX + name + " " + args + "**")
                    .queue();
            return;
        }
        Bot.getInstance().getMusicController().seekMusic(event.getTextChannel(), content);
    }
}

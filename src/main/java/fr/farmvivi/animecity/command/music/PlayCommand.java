package fr.farmvivi.animecity.command.music;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import fr.farmvivi.animecity.command.CommandsManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PlayCommand extends Command {
    public PlayCommand() {
        this.name = "play";
        this.aliases = new String[] { "p" };
        this.args = "<name>|<url>";
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
        Bot.getInstance().getMusicController().playMusic(event.getTextChannel(), event.getAuthor(), content);
    }
}

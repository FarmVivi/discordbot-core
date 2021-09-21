package fr.farmvivi.animecity.command.music;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CurrentCommand extends Command {
    public CurrentCommand() {
        this.name = "current";
        this.aliases = new String[] { "np" };
    }

    @Override
    protected void execute(MessageReceivedEvent event, String content) {
        Bot.getInstance().getMusicController().currentMusic(event.getTextChannel());
    }
}

package fr.farmvivi.animecity.command.music;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SkipCommand extends Command {
    public SkipCommand() {
        this.name = "skip";
        this.aliases = new String[] { "fs" };
    }

    @Override
    protected void execute(MessageReceivedEvent event, String content) {
        Bot.getInstance().getMusicController().skipMusic(event.getTextChannel());
    }
}

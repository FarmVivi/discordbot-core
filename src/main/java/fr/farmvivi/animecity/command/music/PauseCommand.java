package fr.farmvivi.animecity.command.music;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PauseCommand extends Command {
    public PauseCommand() {
        this.name = "pause";
        this.aliases = new String[] { "resume" };
    }

    @Override
    protected void execute(MessageReceivedEvent event, String content) {
        Bot.getInstance().getMusicController().pauseMusic(event.getTextChannel());
    }
}

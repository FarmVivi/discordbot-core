package fr.farmvivi.animecity.command.music;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LoopQueueCommand extends Command {
    public LoopQueueCommand() {
        this.name = "loopqueue";
    }

    @Override
    protected boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;
        Bot.getInstance().getMusicController().loopQueueMusic(event.getTextChannel());
        return true;
    }
}

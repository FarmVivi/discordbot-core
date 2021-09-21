package fr.farmvivi.animecity.command.music;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LoopQueueCommand extends Command {
    public LoopQueueCommand(){
        this.name = "loopqueue";
    }

    @Override
    protected void execute(MessageReceivedEvent event, String content) {
        Bot.getInstance().getMusicController().loopQueueMusic(event.getTextChannel());
    }
}

package fr.farmvivi.animecity.command.music;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LoopCommand extends Command {
    public LoopCommand() {
        this.name = "loop";
    }

    @Override
    protected boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;
        Bot.getInstance().getMusicController().loopMusic(event.getTextChannel());
        return true;
    }
}

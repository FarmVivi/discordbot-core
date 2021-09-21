package fr.farmvivi.animecity.command.music;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class StopCommand extends Command {
    public StopCommand() {
        this.name = "stop";
    }

    @Override
    protected void execute(MessageReceivedEvent event, String content) {
        Bot.getInstance().getMusicController().stopMusic(event.getTextChannel());
    }
}

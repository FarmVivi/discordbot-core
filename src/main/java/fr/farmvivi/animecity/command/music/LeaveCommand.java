package fr.farmvivi.animecity.command.music;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LeaveCommand extends Command {
    public LeaveCommand() {
        this.name = "leave";
        this.aliases = new String[] { "disconnect" };
    }

    @Override
    protected boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;
        Bot.getInstance().getMusicController().disconnectMusic(event.getTextChannel());
        return true;
    }
}

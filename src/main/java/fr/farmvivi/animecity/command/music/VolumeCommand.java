package fr.farmvivi.animecity.command.music;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class VolumeCommand extends Command {
    public VolumeCommand() {
        this.name = "volume";
        this.aliases = new String[] { "v" };
        this.args = "<volume>";
    }

    @Override
    protected boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;
        Bot.getInstance().getMusicController().volumeMusic(event.getTextChannel(), content);
        return true;
    }
}

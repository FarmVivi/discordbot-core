package fr.farmvivi.animecity.command;

import java.util.ArrayList;
import java.util.List;

import fr.farmvivi.animecity.command.music.ClearCommand;
import fr.farmvivi.animecity.command.music.CurrentCommand;
import fr.farmvivi.animecity.command.music.LeaveCommand;
import fr.farmvivi.animecity.command.music.LoopCommand;
import fr.farmvivi.animecity.command.music.LoopQueueCommand;
import fr.farmvivi.animecity.command.music.PauseCommand;
import fr.farmvivi.animecity.command.music.PlayCommand;
import fr.farmvivi.animecity.command.music.ReplayCommand;
import fr.farmvivi.animecity.command.music.SeekCommand;
import fr.farmvivi.animecity.command.music.ShutdownCommand;
import fr.farmvivi.animecity.command.music.SkipCommand;
import fr.farmvivi.animecity.command.music.StopCommand;
import fr.farmvivi.animecity.command.music.ViewQueueCommand;
import fr.farmvivi.animecity.command.music.VolumeCommand;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandsManager extends ListenerAdapter {
    public static final String CMD_PREFIX = "*";
    public static final List<Long> ADMINS = new ArrayList<>();

    private final List<Command> commands = new ArrayList<>();

    static {
        ADMINS.add(177135083222859776L);
    }

    public CommandsManager() {
        commands.add(new ShutdownCommand());
        commands.add(new PlayCommand());
        commands.add(new SkipCommand());
        commands.add(new ClearCommand());
        commands.add(new CurrentCommand());
        commands.add(new StopCommand());
        commands.add(new LeaveCommand());
        commands.add(new PauseCommand());
        commands.add(new LoopQueueCommand());
        commands.add(new LoopCommand());
        commands.add(new VolumeCommand());
        commands.add(new SeekCommand());
        commands.add(new ReplayCommand());
        commands.add(new ViewQueueCommand());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT) || event.isFromType(ChannelType.PRIVATE)) {
            final String message = event.getMessage().getContentDisplay();

            if (!message.startsWith(CMD_PREFIX))
                return;

            final String cmd = message.substring(CMD_PREFIX.length()).split(" ")[0];

            for (final Command command : commands) {
                final List<String> cmds = new ArrayList<>();
                cmds.add(command.name);
                if (command.aliases.length > 0)
                    for (final String tempCmd : command.aliases)
                        cmds.add(tempCmd);
                if (cmds.contains(cmd.toLowerCase())) {
                    if (command.args.length() > 0) {
                        final int commandLength = CMD_PREFIX.length() + cmd.length() + 1;
                        if (message.length() > commandLength) {
                            command.execute(event, message.substring(commandLength));
                            return;
                        }
                    }
                    command.execute(event, "");
                    return;
                }
            }
        }
    }

    public List<Command> getCommands() {
        return commands;
    }
}

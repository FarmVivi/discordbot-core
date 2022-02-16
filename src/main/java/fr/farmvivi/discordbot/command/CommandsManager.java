package fr.farmvivi.discordbot.command;

import java.util.ArrayList;
import java.util.List;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.command.music.ClearCommand;
import fr.farmvivi.discordbot.command.music.CurrentCommand;
import fr.farmvivi.discordbot.command.music.LeaveCommand;
import fr.farmvivi.discordbot.command.music.LoopCommand;
import fr.farmvivi.discordbot.command.music.LoopQueueCommand;
import fr.farmvivi.discordbot.command.music.NextCommand;
import fr.farmvivi.discordbot.command.music.NowCommand;
import fr.farmvivi.discordbot.command.music.PauseCommand;
import fr.farmvivi.discordbot.command.music.PlayCommand;
import fr.farmvivi.discordbot.command.music.RadioCommand;
import fr.farmvivi.discordbot.command.music.ReplayCommand;
import fr.farmvivi.discordbot.command.music.SeekCommand;
import fr.farmvivi.discordbot.command.music.ShuffleCommand;
import fr.farmvivi.discordbot.command.music.SkipCommand;
import fr.farmvivi.discordbot.command.music.StopCommand;
import fr.farmvivi.discordbot.command.music.ViewQueueCommand;
import fr.farmvivi.discordbot.command.music.VolumeCommand;
import fr.farmvivi.discordbot.command.music.equalizer.EqHighBassCommand;
import fr.farmvivi.discordbot.command.music.equalizer.EqStartCommand;
import fr.farmvivi.discordbot.command.music.equalizer.EqStopCommand;
import fr.farmvivi.discordbot.command.other.HelpCommand;
import fr.farmvivi.discordbot.command.other.ShutdownCommand;
import fr.farmvivi.discordbot.command.other.VersionCommand;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandsManager extends ListenerAdapter {
    private final List<Command> commands = new ArrayList<>();

    public CommandsManager() {
        commands.add(new PlayCommand());
        commands.add(new NowCommand());
        commands.add(new SkipCommand());
        commands.add(new NextCommand());
        commands.add(new ClearCommand());
        commands.add(new CurrentCommand());
        commands.add(new StopCommand());
        commands.add(new LeaveCommand());
        commands.add(new PauseCommand());
        commands.add(new LoopQueueCommand());
        commands.add(new LoopCommand());
        commands.add(new ShuffleCommand());
        commands.add(new VolumeCommand());
        commands.add(new SeekCommand());
        commands.add(new ReplayCommand());
        commands.add(new ViewQueueCommand());
        commands.add(new EqStartCommand());
        commands.add(new EqStopCommand());
        commands.add(new EqHighBassCommand());
        if (!Bot.getInstance().getConfiguration().radioPath.equalsIgnoreCase(""))
            commands.add(new RadioCommand());

        commands.add(new HelpCommand());
        commands.add(new VersionCommand());
        commands.add(new ShutdownCommand());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT) || event.isFromType(ChannelType.PRIVATE)) {
            final String message = event.getMessage().getContentDisplay();
            final String CMD_PREFIX = Bot.getInstance().getConfiguration().cmdPrefix;

            if (!message.startsWith(CMD_PREFIX))
                return;

            final String cmd = message.substring(CMD_PREFIX.length()).split(" ")[0];

            for (final Command command : commands) {
                final List<String> cmds = new ArrayList<>();
                cmds.add(command.name);
                if (command.aliases.length != 0)
                    for (final String tempCmd : command.aliases)
                        cmds.add(tempCmd);
                if (cmds.contains(cmd.toLowerCase())) {
                    if (command.args.length() != 0) {
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

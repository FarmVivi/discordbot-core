package fr.farmvivi.discordbot.module.commands.command;

import fr.farmvivi.discordbot.Configuration;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.commands.CommandsModule;

import java.util.HashMap;
import java.util.Map;

public class HelpCommand extends Command {
    private final CommandsModule commandsModule;
    private final Configuration botConfig;

    public HelpCommand(CommandsModule commandsModule, Configuration botConfig) {
        super("help", CommandCategory.OTHER, "Affiche toutes les commandes");

        this.guildOnly = false;

        this.commandsModule = commandsModule;
        this.botConfig = botConfig;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        Map<CommandCategory, StringBuilder> strBuilders = new HashMap<>();
        for (Command cmd : commandsModule.getCommands()) {
            if (!strBuilders.containsKey(cmd.getCategory()))
                strBuilders.put(cmd.getCategory(), new StringBuilder("**" + cmd.getCategory().getName() + "** :"));
            StringBuilder builder = strBuilders.get(cmd.getCategory());
            builder.append("\n->").append(formatCmdHelp(cmd));
        }

        StringBuilder finalBuilder = new StringBuilder();
        for (StringBuilder strBuilder : strBuilders.values())
            finalBuilder.append(strBuilder.toString()).append("\n\n");
        String message = finalBuilder.toString();
        event.getChannel().sendMessage(message.subSequence(0, message.length() - 2)).queue();

        return true;
    }

    private String formatCmdHelp(Command command) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("** ").append(botConfig.cmdPrefix).append(command.getName());
        if (command.getArgs().length != 0)
            stringBuilder.append(" ").append(command.getArgsAsString());
        stringBuilder.append(" **| ").append(command.getDescription());
        return stringBuilder.toString();
    }
}

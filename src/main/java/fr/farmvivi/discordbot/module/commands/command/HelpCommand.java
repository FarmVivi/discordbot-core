package fr.farmvivi.discordbot.module.commands.command;

import fr.farmvivi.discordbot.module.commands.*;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.HashMap;
import java.util.Map;

public class HelpCommand extends Command {
    private final CommandsModule commandsModule;

    public HelpCommand(CommandsModule commandsModule) {
        super("help", CommandCategory.OTHER, "Affiche toutes les commandes");

        this.setGuildOnly(false);

        this.commandsModule = commandsModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        Map<CommandCategory, StringBuilder> strBuilders = new HashMap<>();
        for (Command cmd : commandsModule.getCommands()) {
            if (!strBuilders.containsKey(cmd.getCategory()))
                strBuilders.put(cmd.getCategory(), new StringBuilder("**" + cmd.getCategory().getName() + "** :"));
            StringBuilder builder = strBuilders.get(cmd.getCategory());
            builder.append("\n> ").append(formatCmdHelp(cmd));
        }

        StringBuilder finalBuilder = new StringBuilder();
        for (StringBuilder strBuilder : strBuilders.values())
            finalBuilder.append(strBuilder.toString()).append("\n\n");
        String message = finalBuilder.toString();
        reply.addContent(message.substring(0, message.length() - 2));

        reply.setEphemeral(true);

        return true;
    }

    private String formatCmdHelp(Command command) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("**/").append(command.getName());
        if (command.getArgs().length != 0)
            stringBuilder.append(" ").append(command.getArgsAsString());
        stringBuilder.append(" **| ").append(command.getDescription());
        return stringBuilder.toString();
    }
}

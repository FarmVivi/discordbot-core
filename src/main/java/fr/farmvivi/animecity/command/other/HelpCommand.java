package fr.farmvivi.animecity.command.other;

import java.util.HashMap;
import java.util.Map;

import fr.farmvivi.animecity.Bot;
import fr.farmvivi.animecity.command.Command;
import fr.farmvivi.animecity.command.CommandCategory;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelpCommand extends Command {
    public HelpCommand() {
        this.name = "help";
        this.category = CommandCategory.OTHER;
        this.description = "Affiche toutes les commandes";
        this.guildOnly = false;
    }

    @Override
    protected boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        final Map<CommandCategory, StringBuilder> strBuilders = new HashMap<>();
        for (final Command cmd : Bot.getInstance().getCommandsManager().getCommands()) {
            if (!strBuilders.containsKey(cmd.getCategory()))
                strBuilders.put(cmd.getCategory(), new StringBuilder("**" + cmd.getCategory().getName() + "** :"));
            final StringBuilder builder = strBuilders.get(cmd.getCategory());
            builder.append("\n->" + formatCmdHelp(cmd));
        }

        final StringBuilder finalBuilder = new StringBuilder();
        for (StringBuilder strBuilder : strBuilders.values())
            finalBuilder.append(strBuilder.toString() + "\n\n");
        final String message = finalBuilder.toString();
        event.getChannel().sendMessage(message.subSequence(0, message.length() - 2)).queue();

        return true;
    }

    private String formatCmdHelp(Command command) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("** " + Bot.getInstance().getConfiguration().cmdPrefix + command.getName());
        if (command.getArgs().length() != 0)
            stringBuilder.append(" " + command.getArgs());
        stringBuilder.append(" **| " + command.getDescription());
        return stringBuilder.toString();
    }
}

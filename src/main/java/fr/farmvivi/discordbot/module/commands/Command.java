package fr.farmvivi.discordbot.module.commands;

import fr.farmvivi.discordbot.Bot;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Command {
    protected final List<Command> subCommands = new ArrayList<>();

    private final String name;
    private final CommandCategory category;
    private final String description;
    private final OptionData[] args;
    private final String[] aliases;

    protected boolean guildOnly = true;
    protected boolean adminOnly = false;

    public Command(String name, CommandCategory category, String description) {
        this(name, category, description, new OptionData[0], new String[0]);
    }

    public Command(String name, CommandCategory category, String description, String[] aliases) {
        this(name, category, description, new OptionData[0], aliases);
    }

    public Command(String name, CommandCategory category, String description, OptionData[] args) {
        this(name, category, description, args, new String[0]);
    }

    public Command(String name, CommandCategory category, String description, OptionData[] args, String[] aliases) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.args = args;
        this.aliases = aliases;
    }

    public boolean execute(CommandReceivedEvent event, String content) {
        if (guildOnly && !event.isFromGuild()) {
            event.getChannel().sendMessage("Cette commande peut seulement être exécuté sur un serveur discord.").queue();
            return false;
        } else if (adminOnly && !Bot.getInstance().getConfiguration().cmdAdmins.contains(event.getAuthor().getIdLong())) {
            event.getChannel().sendMessage("Vous n'avez pas la permission d'exécuter cette commande.").queue();
            return false;
        } else if (subCommands.size() != 0 && content.length() != 0) {
            String cmd = content.split(" ")[0];

            for (Command command : subCommands) {
                List<String> commands = new ArrayList<>();
                commands.add(command.name);
                if (command.aliases.length != 0)
                    Collections.addAll(commands, command.aliases);
                if (commands.contains(cmd.toLowerCase())) {
                    if (command.args.length != 0) {
                        int commandLength = cmd.length() + 1;
                        if (content.length() > commandLength) {
                            command.execute(event, content.substring(commandLength));
                            return false;
                        }
                    }
                    command.execute(event, "");
                    return false;
                }
            }
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }

    public CommandCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public OptionData[] getArgs() {
        return args;
    }

    public String getArgsAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (OptionData option : getArgs()) {
            stringBuilder.append(" ").append(option.getName());
        }
        return stringBuilder.toString().replaceFirst(" ", "");
    }

    public boolean isGuildOnly() {
        return guildOnly;
    }

    public boolean isAdminOnly() {
        return adminOnly;
    }

    public List<Command> getSubCommands() {
        return subCommands;
    }
}

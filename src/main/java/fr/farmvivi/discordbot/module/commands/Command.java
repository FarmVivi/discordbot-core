package fr.farmvivi.discordbot.module.commands;

import fr.farmvivi.discordbot.Bot;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Map;

public abstract class Command {
    private final String name;
    private final CommandCategory category;
    private final String description;
    private OptionData[] args;
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

    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (guildOnly && !event.isFromGuild()) {
            reply.addContent("Cette commande peut seulement être exécuté sur un serveur discord.");
            return false;
        } else if (adminOnly && !Bot.getInstance().getConfiguration().cmdAdmins.contains(event.getAuthor().getIdLong())) {
            reply.addContent("Vous n'avez pas la permission d'exécuter cette commande.");
            return false;
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

    protected void setArgs(OptionData[] args) {
        this.args = args;
    }

    public String getArgsAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (OptionData option : getArgs()) {
            stringBuilder.append(" ");
            if (!option.isRequired()) {
                stringBuilder.append("*");
            }
            stringBuilder.append("<").append(option.getName()).append(">");
            if (!option.isRequired()) {
                stringBuilder.append("*");
            }
        }
        return stringBuilder.toString().replaceFirst(" ", "");
    }

    public boolean isGuildOnly() {
        return guildOnly;
    }

    public boolean isAdminOnly() {
        return adminOnly;
    }
}

package fr.farmvivi.discordbot.module.commands;

import fr.farmvivi.discordbot.Bot;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Map;

public abstract class Command {
    private final String name;
    private final CommandCategory category;
    private final String description;

    private boolean guildOnly = true;
    private boolean adminOnly = false;
    private String[] aliases = new String[0];
    private OptionData[] args = new OptionData[0];

    public Command(String name, CommandCategory category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
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

    public String getArgsAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (OptionData option : getArgs()) {
            stringBuilder.append(" ");
            if (!option.isRequired()) {
                stringBuilder.append("*");
            }
            stringBuilder.append("<").append(option.getName()).append(" : ").append(typeToString(option.getType())).append(">");
            if (!option.isRequired()) {
                stringBuilder.append("*");
            }
        }
        return stringBuilder.toString().replaceFirst(" ", "");
    }

    private String typeToString(OptionType type) {
        return switch (type) {
            case STRING -> "texte";
            case INTEGER -> "nombre entier";
            case BOOLEAN -> "booléen";
            case USER -> "utilisateur";
            case ROLE -> "rôle";
            case CHANNEL -> "salon";
            case MENTIONABLE -> "rôle ou utilisateur";
            case SUB_COMMAND -> "sous-commande";
            case SUB_COMMAND_GROUP -> "groupe de sous-commandes";
            case NUMBER -> "nombre réel";
            case ATTACHMENT -> "fichier";
            case UNKNOWN -> "inconnu";
        };
    }

    public String getName() {
        return name;
    }

    public CommandCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public boolean isGuildOnly() {
        return guildOnly;
    }

    protected void setGuildOnly(boolean guildOnly) {
        this.guildOnly = guildOnly;
    }

    public boolean isAdminOnly() {
        return adminOnly;
    }

    protected void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    public String[] getAliases() {
        return aliases;
    }

    protected void setAliases(String[] aliases) {
        this.aliases = aliases;
    }

    public OptionData[] getArgs() {
        return args;
    }

    protected void setArgs(OptionData[] args) {
        this.args = args;
    }
}

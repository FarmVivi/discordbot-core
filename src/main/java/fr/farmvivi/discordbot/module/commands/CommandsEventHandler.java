package fr.farmvivi.discordbot.module.commands;

import fr.farmvivi.discordbot.Configuration;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CommandsEventHandler extends ListenerAdapter {
    private final CommandsModule commandsModule;
    private final Configuration botConfig;

    public CommandsEventHandler(CommandsModule commandsModule, Configuration botConfig) {
        this.commandsModule = commandsModule;
        this.botConfig = botConfig;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmd = event.getName();

        CommandReceivedEvent commandReceivedEvent = new CommandReceivedEvent(
                event.getGuild(),
                event.getChannel(),
                event.getChannelType(),
                event.getUser(),
                cmd,
                event.isFromGuild());

        CommandMessageBuilder reply = new CommandMessageBuilder(event);

        for (Command command : commandsModule.getCommands()) {
            if (command.getName().equalsIgnoreCase(cmd)) {
                Map<String, OptionMapping> args = new HashMap<>(event.getOptions().size());
                for (OptionMapping option : event.getOptions()) {
                    args.put(option.getName(), option);
                }

                boolean success = command.execute(commandReceivedEvent, args, reply);
                if (!success) {
                    reply.setEphemeral(true);
                }

                if (reply.isDiffer()) {
                    event.deferReply(reply.isEphemeral()).queue();
                } else if (reply.isEmpty()) {
                    event.reply("OK").flatMap(InteractionHook::deleteOriginal).queue();
                } else {
                    event.reply(reply.build()).setEphemeral(reply.isEphemeral()).queue();
                }
                return;
            }
        }

        event.reply("> Une erreur est survenue, la commande est inconnue :confused:").setEphemeral(true).queue();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);

        if (event.isFromType(ChannelType.TEXT) || event.isFromType(ChannelType.PRIVATE)) {
            String message = event.getMessage().getContentRaw();
            String CMD_PREFIX = botConfig.cmdPrefix;

            if (!message.startsWith(CMD_PREFIX))
                return;

            String cmd = message.substring(CMD_PREFIX.length()).split(" ")[0];

            CommandReceivedEvent commandReceivedEvent = new CommandReceivedEvent(
                    event.getGuild(),
                    event.getChannel(),
                    event.getChannelType(),
                    event.getAuthor(),
                    cmd,
                    event.isFromGuild());

            CommandMessageBuilder reply = new CommandMessageBuilder(event);
            //reply.addContent("> **Cette commande est obsolète !**\n"
            //        + "> Veuillez utiliser les commandes en commençant par **/** au lieu de **" + botConfig.cmdPrefix + "** !\n"
            //        + "\n");

            for (Command command : commandsModule.getCommands()) {
                List<String> commands = new ArrayList<>();
                commands.add(command.getName());
                if (command.getAliases().length != 0)
                    Collections.addAll(commands, command.getAliases());

                if (commands.contains(cmd.toLowerCase())) {
                    Map<String, OptionMapping> args = new HashMap<>();
                    if (command.getArgs().length != 0) {
                        int requiredArgs = 0;
                        for (OptionData option : command.getArgs()) {
                            if (option.isRequired()) {
                                requiredArgs++;
                            }
                        }

                        int commandLength = CMD_PREFIX.length() + cmd.length() + 1;

                        if (message.length() <= commandLength && requiredArgs > 0) {
                            reply.addContent("> **Erreur :** Vous devez spécifier des arguments !\n"
                                    + "> Utilisation : **" + CMD_PREFIX + command.getName() + " " + command.getArgsAsString() + "**");
                            sendErrorMessage(event, reply);
                            return;
                        } else if (message.length() > commandLength) {
                            String content = message.substring(commandLength);
                            String[] splitContent = content.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                            // Si le nombre d'arguments est inférieur au nombre d'arguments requis
                            if (splitContent.length < requiredArgs) {
                                reply.addContent("> **Erreur :** Vous n'avez pas fourni assez d'arguments !\n"
                                        + "> Utilisation : **" + CMD_PREFIX + command.getName() + " " + command.getArgsAsString() + "**");
                                sendErrorMessage(event, reply);
                                return;
                            }

                            int optionalArgs = splitContent.length - requiredArgs;

                            // Correctif pour le cas où la commande ne prend seulement qu'un seul argument et que l'utilisateur met des espaces
                            if (command.getArgs().length == 1 && splitContent.length > 1) {
                                splitContent = new String[]{content};
                                if (requiredArgs == 1) {
                                    optionalArgs = 0;
                                } else {
                                    optionalArgs = 1;
                                }
                            } else
                                // Si le nombre d'arguments est supérieur au nombre d'arguments possibles
                                if (optionalArgs + requiredArgs > command.getArgs().length) {
                                    reply.addContent("> **Erreur :** Vous avez fourni trop d'arguments !\n"
                                            + "> Utilisation : **" + CMD_PREFIX + command.getName() + " " + command.getArgsAsString() + "**");
                                    sendErrorMessage(event, reply);
                                    return;
                                }

                            int valueIndex = 0;
                            for (int i = 0; i < command.getArgs().length; i++) {
                                OptionData option = command.getArgs()[i];
                                if (!option.isRequired()) {
                                    if (optionalArgs == 0) {
                                        continue;
                                    }

                                    optionalArgs--;
                                }

                                String argValue = splitContent[valueIndex];

                                TLongObjectMap<Object> resolved = new TLongObjectHashMap<>();

                                DataObject dataObject = DataObject.empty();
                                dataObject.put("name", option.getName());
                                dataObject.put("type", option.getType().getKey());
                                try {
                                    switch (option.getType()) {
                                        case STRING -> parseString(option, dataObject, argValue);
                                        case INTEGER -> parseInteger(option, dataObject, argValue);
                                        case BOOLEAN -> parseBoolean(option, dataObject, argValue);
                                        case USER ->
                                                parseUser(option, dataObject, argValue, resolved, event.getJDA(), event.getGuild());
                                        case CHANNEL ->
                                                parseChannel(option, dataObject, argValue, resolved, event.getGuild());
                                        case ROLE ->
                                                parseRole(option, dataObject, argValue, resolved, event.getGuild());
                                        case MENTIONABLE ->
                                                parseMentionable(option, dataObject, argValue, resolved, event.getJDA(), event.getGuild());
                                        case NUMBER -> parseNumber(option, dataObject, argValue);
                                        default -> {
                                            reply.addContent("> **Erreur :** L'argument **" + option.getName() + "** est seulement compatible avec les commandes slash !");
                                            sendErrorMessage(event, reply);
                                            return;
                                        }
                                    }
                                } catch (CommandOptionParseErrorException e) {
                                    reply.addContent("> **Erreur :** L'argument **" + option.getName() + "** " + e.getMessage());
                                    sendErrorMessage(event, reply);
                                    return;
                                } catch (CommandOptionParseTypeException e) {
                                    if (option.isRequired()) {
                                        reply.addContent("> **Erreur :** L'argument **" + option.getName() + "** " + e.getMessage());
                                        sendErrorMessage(event, reply);
                                        return;
                                    } else {
                                        optionalArgs++;
                                        if (optionalArgs >= command.getArgs().length - i) {
                                            reply.addContent("> **Erreur :** L'argument **" + option.getName() + "** " + e.getMessage());
                                            sendErrorMessage(event, reply);
                                            return;
                                        }
                                    }
                                    continue;
                                }

                                valueIndex++;

                                OptionMapping optionMapping = new OptionMapping(dataObject, resolved, event.getJDA(), event.getGuild());

                                args.put(option.getName(), optionMapping);
                            }
                        }
                    }

                    boolean success = command.execute(commandReceivedEvent, args, reply);
                    if (!success) {
                        reply.setEphemeral(true);
                    }

                    if (!reply.isDiffer() && !reply.isEmpty()) {
                        Message originalMessage = event.getMessage();
                        MessageCreateAction messageAction = originalMessage.reply(reply.build());
                        if (reply.isEphemeral()) {
                            messageAction.delay(1, TimeUnit.MINUTES).flatMap(Message::delete).queue();
                            originalMessage.delete().queueAfter(1, TimeUnit.MINUTES);
                        } else {
                            messageAction.queue();
                        }
                    }
                    return;
                }
            }
        }
    }

    private void parseString(OptionData option, DataObject dataObject, String argValue) throws CommandOptionParseErrorException {
        if (argValue.startsWith("\"") && argValue.endsWith("\"")) {
            argValue = argValue.substring(1, argValue.length() - 1);
        }
        if (option.getMinLength() != null) {
            if (argValue.length() < option.getMinLength()) {
                throw new CommandOptionParseErrorException("doit contenir au moins " + option.getMinLength() + " caractères ! (Vous avez fourni " + argValue.length() + " caractères)");
            }
        }
        if (option.getMaxLength() != null) {
            if (argValue.length() > option.getMaxLength()) {
                throw new CommandOptionParseErrorException("doit contenir au plus " + option.getMaxLength() + " caractères ! (Vous avez fourni " + argValue.length() + " caractères)");
            }
        }
        dataObject.put("value", argValue);
    }

    private void parseInteger(OptionData option, DataObject dataObject, String argValue) throws CommandOptionParseErrorException, CommandOptionParseTypeException {
        try {
            int value = Integer.parseInt(argValue);
            if (option.getMinValue() != null) {
                if (value < option.getMinValue().intValue()) {
                    throw new CommandOptionParseErrorException("doit être supérieur ou égal à " + option.getMinValue() + " !");
                }
            }
            if (option.getMaxValue() != null) {
                if (value > option.getMaxValue().intValue()) {
                    throw new CommandOptionParseErrorException("doit être inférieur ou égal à " + option.getMaxValue() + " !");
                }
            }
            dataObject.put("value", value);
        } catch (NumberFormatException e) {
            throw new CommandOptionParseTypeException("doit être un nombre entier !");
        }
    }

    private void parseBoolean(OptionData option, DataObject dataObject, String argValue) throws CommandOptionParseTypeException {
        if (argValue.equalsIgnoreCase("true") || argValue.equalsIgnoreCase("false")) {
            dataObject.put("value", Boolean.parseBoolean(argValue));
        } else {
            throw new CommandOptionParseTypeException("doit être un booléen !");
        }
    }

    private void parseUser(OptionData option, DataObject dataObject, String argValue, TLongObjectMap<Object> resolved, JDA jda, Guild guild) throws CommandOptionParseErrorException, CommandOptionParseTypeException {
        if (argValue.startsWith("<@") && argValue.endsWith(">")) {
            argValue = argValue.substring(2, argValue.length() - 1);
            if (argValue.startsWith("!")) {
                argValue = argValue.substring(1);
            }
            try {
                long userId = Long.parseLong(argValue);
                dataObject.put("value", userId);
                if (guild != null) {
                    Member member = guild.getMemberById(userId);
                    if (member != null) {
                        resolved.put(member.getIdLong(), member);
                    } else {
                        throw new CommandOptionParseErrorException("doit être un utilisateur mentionné !");
                    }
                } else {
                    User user = jda.getUserById(userId);
                    if (user != null) {
                        resolved.put(user.getIdLong(), user);
                    } else {
                        throw new CommandOptionParseErrorException("doit être un utilisateur mentionné !");
                    }
                }
            } catch (NumberFormatException e) {
                throw new CommandOptionParseTypeException("doit être un utilisateur mentionné !");
            }
        } else {
            throw new CommandOptionParseTypeException("doit être un utilisateur mentionné !");
        }
    }

    private void parseChannel(OptionData option, DataObject dataObject, String argValue, TLongObjectMap<Object> resolved, Guild guild) throws CommandOptionParseErrorException, CommandOptionParseTypeException {
        if (argValue.startsWith("<#") && argValue.endsWith(">")) {
            argValue = argValue.substring(2, argValue.length() - 1);
            try {
                long channelId = Long.parseLong(argValue);
                dataObject.put("value", channelId);
                if (guild != null) {
                    GuildChannel channel = guild.getGuildChannelById(channelId);
                    if (channel != null) {
                        resolved.put(channel.getIdLong(), channel);
                    } else {
                        throw new CommandOptionParseErrorException("doit être un salon mentionné !");
                    }
                } else {
                    throw new CommandOptionParseErrorException("doit être un salon mentionné !");
                }
            } catch (NumberFormatException e) {
                throw new CommandOptionParseTypeException("doit être un salon mentionné !");
            }
        } else {
            throw new CommandOptionParseTypeException("doit être un salon mentionné !");
        }
    }

    private void parseRole(OptionData option, DataObject dataObject, String argValue, TLongObjectMap<Object> resolved, Guild guild) throws CommandOptionParseErrorException, CommandOptionParseTypeException {
        if (argValue.startsWith("<@&") && argValue.endsWith(">")) {
            argValue = argValue.substring(3, argValue.length() - 1);
            try {
                long roleId = Long.parseLong(argValue);
                dataObject.put("value", roleId);
                if (guild != null) {
                    Role role = guild.getRoleById(roleId);
                    if (role != null) {
                        resolved.put(role.getIdLong(), role);
                    } else {
                        throw new CommandOptionParseErrorException("doit être un rôle mentionné !");
                    }
                } else {
                    throw new CommandOptionParseErrorException("doit être un rôle mentionné !");
                }
            } catch (NumberFormatException e) {
                throw new CommandOptionParseTypeException("doit être un rôle mentionné !");
            }
        } else if (argValue.equals("@everyone")) {
            if (guild != null) {
                Role role = guild.getPublicRole();
                dataObject.put("value", role.getIdLong());
                resolved.put(role.getIdLong(), role);
            } else {
                throw new CommandOptionParseErrorException("doit être un rôle mentionné !");
            }
        } else {
            throw new CommandOptionParseTypeException("doit être un rôle mentionné !");
        }
    }

    private void parseMentionable(OptionData option, DataObject dataObject, String argValue, TLongObjectMap<Object> resolved, JDA jda, Guild guild) throws CommandOptionParseErrorException, CommandOptionParseTypeException {
        try {
            parseRole(option, dataObject, argValue, resolved, guild);
        } catch (CommandOptionParseErrorException | CommandOptionParseTypeException e) {
            parseUser(option, dataObject, argValue, resolved, jda, guild);
        }
    }

    private void parseNumber(OptionData option, DataObject dataObject, String argValue) throws CommandOptionParseErrorException, CommandOptionParseTypeException {
        try {
            if (argValue.contains(".")) {
                double value = Double.parseDouble(argValue);
                if (option.getMinValue() != null) {
                    if (value < option.getMinValue().doubleValue()) {
                        throw new CommandOptionParseErrorException("doit être supérieur ou égal à " + option.getMinValue() + " !");
                    }
                }
                if (option.getMaxValue() != null) {
                    if (value > option.getMaxValue().doubleValue()) {
                        throw new CommandOptionParseErrorException("doit être inférieur ou égal à " + option.getMaxValue() + " !");
                    }
                }
            } else {
                long value = Long.parseLong(argValue);
                if (option.getMinValue() != null) {
                    if (value < option.getMinValue().longValue()) {
                        throw new CommandOptionParseErrorException("doit être supérieur ou égal à " + option.getMinValue() + " !");
                    }
                }
                if (option.getMaxValue() != null) {
                    if (value > option.getMaxValue().longValue()) {
                        throw new CommandOptionParseErrorException("doit être inférieur ou égal à " + option.getMaxValue() + " !");
                    }
                }
            }
            dataObject.put("value", argValue);
        } catch (NumberFormatException e) {
            throw new CommandOptionParseTypeException("doit être un nombre !");
        }
    }

    private void sendErrorMessage(MessageReceivedEvent event, CommandMessageBuilder reply) {
        Message originalMessage = event.getMessage();
        MessageCreateAction messageAction = originalMessage.reply(reply.build());
        messageAction.delay(1, TimeUnit.MINUTES).flatMap(Message::delete).queue();
        originalMessage.delete().queueAfter(1, TimeUnit.MINUTES);
    }
}

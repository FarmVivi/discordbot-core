package fr.farmvivi.discordbot.core.command.parser;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandContext;
import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.command.SimpleCommandContext;
import fr.farmvivi.discordbot.core.api.command.exception.CommandParseException;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for text commands.
 * This parser extracts command information from MessageReceivedEvents.
 */
public class TextCommandParser implements CommandParser {
    
    private static final Logger logger = LoggerFactory.getLogger(TextCommandParser.class);
    
    // Patterns for parsing mentions and IDs
    private static final Pattern USER_MENTION_PATTERN = Pattern.compile("<@!?(\\d+)>");
    private static final Pattern ROLE_MENTION_PATTERN = Pattern.compile("<@&(\\d+)>");
    private static final Pattern CHANNEL_MENTION_PATTERN = Pattern.compile("<#(\\d+)>");
    
    private final String prefix;
    
    /**
     * Creates a new text command parser.
     * 
     * @param prefix the command prefix
     */
    public TextCommandParser(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean canParse(Event event) {
        return event instanceof MessageReceivedEvent;
    }

    @Override
    public CommandContext parse(Event event, Command command) throws CommandParseException {
        if (!(event instanceof MessageReceivedEvent messageEvent)) {
            throw new CommandParseException("Event is not a message received event");
        }
        
        Message message = messageEvent.getMessage();
        User user = messageEvent.getAuthor();
        Guild guild = messageEvent.isFromGuild() ? messageEvent.getGuild() : null;
        MessageChannel channel = messageEvent.getChannel();
        
        // Get locale
        Locale locale = Locale.US;
        if (guild != null) {
            Member member = guild.getMember(user);
            if (member != null) {
                locale = Locale.forLanguageTag(member.getUser().getLocale());
                if (locale.getLanguage().isEmpty()) {
                    locale = Locale.US;
                }
            }
        }
        
        // Parse the message content
        String content = message.getContentRaw();
        if (!content.startsWith(prefix)) {
            throw new CommandParseException("Message does not start with the prefix: " + prefix);
        }
        
        // Remove prefix and split command name from arguments
        content = content.substring(prefix.length());
        String[] parts = content.split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String argsStr = parts.length > 1 ? parts[1] : "";
        
        // Check if this is the correct command
        if (!commandName.equalsIgnoreCase(command.getName()) && 
                command.getAliases().stream().noneMatch(a -> a.equalsIgnoreCase(commandName))) {
            throw new CommandParseException("Command name does not match");
        }
        
        // Parse arguments
        Map<String, Object> options = parseOptions(argsStr, command, messageEvent);
        
        // Create and validate context
        SimpleCommandContext context = new SimpleCommandContext(
                event, command, user, guild, channel, locale, options
        );
        
        context.validateOptions();
        return context;
    }

    @Override
    public String extractCommandName(Event event) throws CommandParseException {
        if (!(event instanceof MessageReceivedEvent messageEvent)) {
            throw new CommandParseException("Event is not a message received event");
        }
        
        String content = messageEvent.getMessage().getContentRaw();
        if (!content.startsWith(prefix)) {
            throw new CommandParseException("Message does not start with the prefix: " + prefix);
        }
        
        // Remove prefix and get command name
        content = content.substring(prefix.length());
        String[] parts = content.split("\\s+", 2);
        return parts[0].toLowerCase();
    }

    @Override
    public boolean isCommandInvocation(Event event) {
        if (!(event instanceof MessageReceivedEvent messageEvent)) {
            return false;
        }
        
        // Ignore bots
        if (messageEvent.getAuthor().isBot()) {
            return false;
        }
        
        // Check if the message starts with the prefix
        String content = messageEvent.getMessage().getContentRaw();
        return content.startsWith(prefix);
    }
    
    /**
     * Parses command options from a string.
     * 
     * @param argsStr the arguments string
     * @param command the command
     * @param event the message event
     * @return the parsed options
     * @throws CommandParseException if parsing fails
     */
    private Map<String, Object> parseOptions(String argsStr, Command command, MessageReceivedEvent event) 
            throws CommandParseException {
        Map<String, Object> options = new HashMap<>();
        List<CommandOption<?>> commandOptions = command.getOptions();
        
        if (commandOptions.isEmpty() || argsStr.isEmpty()) {
            return options;
        }
        
        List<String> args = splitArguments(argsStr);
        int optionIndex = 0;
        
        for (String arg : args) {
            if (optionIndex >= commandOptions.size()) {
                break;
            }
            
            CommandOption<?> option = commandOptions.get(optionIndex);
            try {
                Object value = parseOptionValue(arg, option, event);
                options.put(option.getName(), value);
                optionIndex++;
            } catch (CommandParseException e) {
                logger.warn("Failed to parse option '{}': {}", option.getName(), e.getMessage());
                if (option.isRequired()) {
                    throw new CommandParseException("Required option '" + option.getName() + 
                            "' could not be parsed: " + e.getMessage(), option.getName());
                }
                // Skip optional argument that failed to parse
            }
        }
        
        return options;
    }
    
    /**
     * Splits a string into command arguments, respecting quotes.
     * 
     * @param argsStr the arguments string
     * @return the list of arguments
     */
    private List<String> splitArguments(String argsStr) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = '"';
        
        for (int i = 0; i < argsStr.length(); i++) {
            char c = argsStr.charAt(i);
            
            if (c == '"' || c == '\'') {
                if (inQuotes) {
                    if (c == quoteChar) {
                        inQuotes = false;
                    } else {
                        current.append(c);
                    }
                } else {
                    inQuotes = true;
                    quoteChar = c;
                    
                    // If there's content before the quote, add it as a separate arg
                    if (current.length() > 0) {
                        args.add(current.toString().trim());
                        current = new StringBuilder();
                    }
                }
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (current.length() > 0) {
                    args.add(current.toString().trim());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            args.add(current.toString().trim());
        }
        
        return args;
    }
    
    /**
     * Parses an option value from a string.
     * 
     * @param arg the argument string
     * @param option the option
     * @param event the message event
     * @return the parsed value
     * @throws CommandParseException if parsing fails
     */
    private Object parseOptionValue(String arg, CommandOption<?> option, MessageReceivedEvent event) 
            throws CommandParseException {
        try {
            return switch (option.getType()) {
                case STRING -> arg;
                case INTEGER -> Integer.parseInt(arg);
                case BOOLEAN -> parseBoolean(arg);
                case USER -> parseUser(arg, event);
                case CHANNEL -> parseChannel(arg, event);
                case ROLE -> parseRole(arg, event);
                case MENTIONABLE -> parseMentionable(arg, event);
                case NUMBER -> Double.parseDouble(arg);
                case ATTACHMENT -> {
                    if (event.getMessage().getAttachments().isEmpty()) {
                        throw new CommandParseException("No attachment found");
                    }
                    yield event.getMessage().getAttachments().get(0);
                }
            };
        } catch (Exception e) {
            if (e instanceof CommandParseException) {
                throw e;
            }
            throw new CommandParseException("Failed to parse option value: " + e.getMessage());
        }
    }
    
    /**
     * Parses a boolean value from a string.
     * 
     * @param arg the argument string
     * @return the parsed boolean
     * @throws CommandParseException if parsing fails
     */
    private boolean parseBoolean(String arg) throws CommandParseException {
        arg = arg.toLowerCase();
        if (arg.equals("true") || arg.equals("yes") || arg.equals("y") || arg.equals("1")) {
            return true;
        } else if (arg.equals("false") || arg.equals("no") || arg.equals("n") || arg.equals("0")) {
            return false;
        } else {
            throw new CommandParseException("Invalid boolean value: " + arg);
        }
    }
    
    /**
     * Parses a user from a string.
     * 
     * @param arg the argument string
     * @param event the message event
     * @return the parsed user
     * @throws CommandParseException if parsing fails
     */
    private User parseUser(String arg, MessageReceivedEvent event) throws CommandParseException {
        // Try to parse as mention
        Matcher matcher = USER_MENTION_PATTERN.matcher(arg);
        if (matcher.matches()) {
            String id = matcher.group(1);
            try {
                User user = event.getJDA().getUserById(id);
                if (user != null) {
                    return user;
                }
            } catch (Exception e) {
                // Ignore and try other methods
            }
        }
        
        // Try to parse as ID
        try {
            long id = Long.parseLong(arg);
            User user = event.getJDA().getUserById(id);
            if (user != null) {
                return user;
            }
        } catch (NumberFormatException e) {
            // Ignore and try other methods
        }
        
        // Try to find by name
        if (event.isFromGuild()) {
            List<Member> members = event.getGuild().getMembersByName(arg, true);
            if (!members.isEmpty()) {
                return members.get(0).getUser();
            }
            
            members = event.getGuild().getMembersByEffectiveName(arg, true);
            if (!members.isEmpty()) {
                return members.get(0).getUser();
            }
        }
        
        throw new CommandParseException("Could not find user: " + arg);
    }
    
    /**
     * Parses a channel from a string.
     * 
     * @param arg the argument string
     * @param event the message event
     * @return the parsed channel
     * @throws CommandParseException if parsing fails
     */
    private Channel parseChannel(String arg, MessageReceivedEvent event) throws CommandParseException {
        if (!event.isFromGuild()) {
            throw new CommandParseException("Channels can only be parsed in guilds");
        }
        
        // Try to parse as mention
        Matcher matcher = CHANNEL_MENTION_PATTERN.matcher(arg);
        if (matcher.matches()) {
            String id = matcher.group(1);
            try {
                Channel channel = event.getGuild().getChannelById(Channel.class, id);
                if (channel != null) {
                    return channel;
                }
            } catch (Exception e) {
                // Ignore and try other methods
            }
        }
        
        // Try to parse as ID
        try {
            long id = Long.parseLong(arg);
            Channel channel = event.getGuild().getChannelById(Channel.class, id);
            if (channel != null) {
                return channel;
            }
        } catch (NumberFormatException e) {
            // Ignore and try other methods
        }
        
        // Try to find by name
        List<Channel> channels = event.getGuild().getChannels().stream()
                .filter(c -> c.getName().equalsIgnoreCase(arg))
                .toList();
        
        if (!channels.isEmpty()) {
            return channels.get(0);
        }
        
        throw new CommandParseException("Could not find channel: " + arg);
    }
    
    /**
     * Parses a role from a string.
     * 
     * @param arg the argument string
     * @param event the message event
     * @return the parsed role
     * @throws CommandParseException if parsing fails
     */
    private Role parseRole(String arg, MessageReceivedEvent event) throws CommandParseException {
        if (!event.isFromGuild()) {
            throw new CommandParseException("Roles can only be parsed in guilds");
        }
        
        // Try to parse as mention
        Matcher matcher = ROLE_MENTION_PATTERN.matcher(arg);
        if (matcher.matches()) {
            String id = matcher.group(1);
            try {
                Role role = event.getGuild().getRoleById(id);
                if (role != null) {
                    return role;
                }
            } catch (Exception e) {
                // Ignore and try other methods
            }
        }
        
        // Try to parse as ID
        try {
            long id = Long.parseLong(arg);
            Role role = event.getGuild().getRoleById(id);
            if (role != null) {
                return role;
            }
        } catch (NumberFormatException e) {
            // Ignore and try other methods
        }
        
        // Try to find by name
        List<Role> roles = event.getGuild().getRolesByName(arg, true);
        if (!roles.isEmpty()) {
            return roles.get(0);
        }
        
        throw new CommandParseException("Could not find role: " + arg);
    }
    
    /**
     * Parses a mentionable entity from a string.
     * 
     * @param arg the argument string
     * @param event the message event
     * @return the parsed mentionable (User, Member, Role, or Channel)
     * @throws CommandParseException if parsing fails
     */
    private Object parseMentionable(String arg, MessageReceivedEvent event) throws CommandParseException {
        // Try user first
        try {
            return parseUser(arg, event);
        } catch (CommandParseException e) {
            // Try role
            if (event.isFromType(ChannelType.TEXT)) {
                try {
                    return parseRole(arg, event);
                } catch (CommandParseException e2) {
                    // Try channel
                    try {
                        return parseChannel(arg, event);
                    } catch (CommandParseException e3) {
                        throw new CommandParseException("Could not parse mentionable: " + arg);
                    }
                }
            }
        }
        
        throw new CommandParseException("Could not parse mentionable: " + arg);
    }
}

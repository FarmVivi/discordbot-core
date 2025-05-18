package fr.farmvivi.discordbot.core.command.impl.parser;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for text commands.
 * Converts text arguments to typed objects.
 */
public class TextCommandParser {

    private static final Logger logger = LoggerFactory.getLogger(TextCommandParser.class);
    
    // Patterns
    private static final Pattern QUOTED_PATTERN = Pattern.compile("\"([^\"]*)\"");
    private static final Pattern MENTION_USER_PATTERN = Pattern.compile("<@!?(\\d+)>");
    private static final Pattern MENTION_CHANNEL_PATTERN = Pattern.compile("<#(\\d+)>");
    private static final Pattern MENTION_ROLE_PATTERN = Pattern.compile("<@&(\\d+)>");
    private static final Pattern ATTACHMENT_PATTERN = Pattern.compile("attachment://([^ ]+)");
    
    private final Command command;
    private final String argsString;
    
    /**
     * Creates a new TextCommandParser.
     *
     * @param command    the command
     * @param argsString the arguments string
     */
    public TextCommandParser(Command command, String argsString) {
        this.command = command;
        this.argsString = argsString;
    }
    
    /**
     * Parses the arguments string.
     *
     * @return a map of option names to values
     */
    public Map<String, Object> parse() {
        Map<String, Object> result = new HashMap<>();
        
        if (argsString.isEmpty()) {
            return result;
        }
        
        // Get the command options
        List<CommandOption<?>> options = command.getOptions();
        
        // No options defined
        if (options.isEmpty()) {
            return result;
        }
        
        // Parse arguments
        List<String> args = splitArguments(argsString);
        
        // Add arguments to the result map
        for (int i = 0; i < args.size() && i < options.size(); i++) {
            CommandOption<?> option = options.get(i);
            String arg = args.get(i);
            
            // Parse the argument according to its type
            Object value = parseArgument(arg, option.getType());
            
            if (value != null) {
                result.put(option.getName(), value);
            }
        }
        
        return result;
    }
    
    /**
     * Splits the arguments string into a list of arguments.
     * Handles quoted strings, e.g. "argument with spaces".
     *
     * @param argsString the arguments string
     * @return a list of arguments
     */
    private List<String> splitArguments(String argsString) {
        List<String> args = new ArrayList<>();
        
        // Handle quoted arguments
        Matcher quotedMatcher = QUOTED_PATTERN.matcher(argsString);
        StringBuilder sb = new StringBuilder(argsString);
        List<String> quotedArgs = new ArrayList<>();
        
        // Find all quoted arguments
        while (quotedMatcher.find()) {
            String quoted = quotedMatcher.group(1);
            quotedArgs.add(quoted);
            
            // Replace the quoted argument with a placeholder
            int start = sb.indexOf("\"" + quoted + "\"");
            int end = start + quoted.length() + 2;
            sb.replace(start, end, "___QUOTED_" + (quotedArgs.size() - 1) + "___");
        }
        
        // Split the remaining arguments
        String[] splitArgs = sb.toString().trim().split("\\s+");
        
        // Replace placeholders with quoted arguments
        for (String arg : splitArgs) {
            if (arg.startsWith("___QUOTED_") && arg.endsWith("___")) {
                int index = Integer.parseInt(arg.substring(10, arg.length() - 3));
                args.add(quotedArgs.get(index));
            } else {
                args.add(arg);
            }
        }
        
        return args;
    }
    
    /**
     * Parses an argument according to its type.
     *
     * @param arg  the argument string
     * @param type the option type
     * @return the parsed value
     */
    private Object parseArgument(String arg, OptionType2 type) {
        try {
            return switch (type) {
                case STRING -> arg;
                case INTEGER -> Integer.parseInt(arg);
                case BOOLEAN -> parseBoolean(arg);
                case NUMBER -> parseNumber(arg);
                case USER -> parseUser(arg);
                case CHANNEL -> parseChannel(arg);
                case ROLE -> parseRole(arg);
                case MENTIONABLE -> parseMentionable(arg);
                case ATTACHMENT -> parseAttachment(arg);
                default -> arg;
            };
        } catch (NumberFormatException e) {
            logger.debug("Failed to parse {} as {}: {}", arg, type, e.getMessage());
            return null;
        }
    }
    
    /**
     * Parses a boolean value.
     *
     * @param arg the argument string
     * @return the boolean value
     */
    private Boolean parseBoolean(String arg) {
        if (arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("yes") || 
            arg.equalsIgnoreCase("y") || arg.equalsIgnoreCase("1") ||
            arg.equalsIgnoreCase("oui") || arg.equalsIgnoreCase("vrai")) {
            return true;
        } else if (arg.equalsIgnoreCase("false") || arg.equalsIgnoreCase("no") || 
                  arg.equalsIgnoreCase("n") || arg.equalsIgnoreCase("0") ||
                  arg.equalsIgnoreCase("non") || arg.equalsIgnoreCase("faux")) {
            return false;
        }
        
        throw new NumberFormatException("Invalid boolean value: " + arg);
    }
    
    /**
     * Parses a number value.
     *
     * @param arg the argument string
     * @return the number value
     */
    private Number parseNumber(String arg) {
        if (arg.contains(".")) {
            return Double.parseDouble(arg);
        } else {
            return Long.parseLong(arg);
        }
    }
    
    /**
     * Parses a user mention.
     *
     * @param arg the argument string
     * @return the user ID
     */
    private String parseUser(String arg) {
        Matcher matcher = MENTION_USER_PATTERN.matcher(arg);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        
        // Try to parse as a direct ID
        try {
            Long.parseLong(arg);
            return arg;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid user mention: " + arg);
        }
    }
    
    /**
     * Parses a channel mention.
     *
     * @param arg the argument string
     * @return the channel ID
     */
    private String parseChannel(String arg) {
        Matcher matcher = MENTION_CHANNEL_PATTERN.matcher(arg);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        
        // Try to parse as a direct ID
        try {
            Long.parseLong(arg);
            return arg;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid channel mention: " + arg);
        }
    }
    
    /**
     * Parses a role mention.
     *
     * @param arg the argument string
     * @return the role ID
     */
    private String parseRole(String arg) {
        Matcher matcher = MENTION_ROLE_PATTERN.matcher(arg);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        
        // Try to parse as a direct ID
        try {
            Long.parseLong(arg);
            return arg;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid role mention: " + arg);
        }
    }
    
    /**
     * Parses a mentionable (user or role).
     *
     * @param arg the argument string
     * @return the mentionable ID
     */
    private String parseMentionable(String arg) {
        try {
            return parseUser(arg);
        } catch (NumberFormatException e) {
            try {
                return parseRole(arg);
            } catch (NumberFormatException e2) {
                throw new NumberFormatException("Invalid mentionable: " + arg);
            }
        }
    }
    
    /**
     * Parses an attachment.
     *
     * @param arg the argument string
     * @return the attachment ID
     */
    private String parseAttachment(String arg) {
        Matcher matcher = ATTACHMENT_PATTERN.matcher(arg);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        
        return arg;
    }
}

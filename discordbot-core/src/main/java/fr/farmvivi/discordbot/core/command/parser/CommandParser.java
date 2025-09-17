package fr.farmvivi.discordbot.core.command.parser;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandContext;
import fr.farmvivi.discordbot.core.api.command.exception.CommandParseException;
import net.dv8tion.jda.api.events.Event;

/**
 * Parser for converting JDA events into command contexts.
 * Different implementations handle different types of command inputs (slash commands, text commands).
 */
public interface CommandParser {
    
    /**
     * Checks if this parser can handle the given event.
     *
     * @param event the JDA event
     * @return true if this parser can handle the event
     */
    boolean canParse(Event event);
    
    /**
     * Parses the event and creates a command context.
     *
     * @param event the JDA event
     * @param command the command to execute
     * @return the parsed command context
     * @throws CommandParseException if parsing fails
     */
    CommandContext parse(Event event, Command command) throws CommandParseException;
    
    /**
     * Extracts the command name from the event.
     * This is used to determine which command to execute.
     *
     * @param event the JDA event
     * @return the command name
     * @throws CommandParseException if extraction fails
     */
    String extractCommandName(Event event) throws CommandParseException;
    
    /**
     * Determines if the event actually contains a command invocation.
     * This is used to filter out events that are not command invocations.
     *
     * @param event the JDA event
     * @return true if the event contains a command invocation
     */
    boolean isCommandInvocation(Event event);
}

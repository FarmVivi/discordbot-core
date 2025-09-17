package fr.farmvivi.discordbot.core.api.command.event;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandContext;
import fr.farmvivi.discordbot.core.api.command.CommandResult;

/**
 * Event fired after a command is executed.
 * This event cannot be cancelled.
 */
public class CommandExecutedEvent extends CommandEvent {

    private final CommandContext context;
    private final CommandResult result;
    private final long executionTimeMs;
    
    /**
     * Creates a new command executed event.
     *
     * @param command        the command
     * @param context        the command context
     * @param result         the command result
     * @param executionTimeMs the execution time in milliseconds
     */
    public CommandExecutedEvent(Command command, CommandContext context, CommandResult result, long executionTimeMs) {
        super(command);
        this.context = context;
        this.result = result;
        this.executionTimeMs = executionTimeMs;
    }
    
    /**
     * Gets the command context.
     *
     * @return the command context
     */
    public CommandContext getContext() {
        return context;
    }
    
    /**
     * Gets the command result.
     *
     * @return the command result
     */
    public CommandResult getResult() {
        return result;
    }
    
    /**
     * Gets the execution time in milliseconds.
     *
     * @return the execution time
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
}

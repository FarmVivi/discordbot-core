package fr.farmvivi.discordbot.core.api.command.event;

import fr.farmvivi.discordbot.core.api.event.Cancellable;
import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandContext;

/**
 * Event fired before a command is executed.
 * This event can be cancelled to prevent the command from executing.
 */
public class CommandExecuteEvent extends CommandEvent implements Cancellable {

    private final CommandContext context;
    private boolean cancelled = false;
    
    /**
     * Creates a new command execute event.
     *
     * @param command the command
     * @param context the command context
     */
    public CommandExecuteEvent(Command command, CommandContext context) {
        super(command);
        this.context = context;
    }
    
    /**
     * Gets the command context.
     *
     * @return the command context
     */
    public CommandContext getContext() {
        return context;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

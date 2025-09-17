package fr.farmvivi.discordbot.core.api.command.event;

import fr.farmvivi.discordbot.core.api.event.Event;
import fr.farmvivi.discordbot.core.api.command.Command;

/**
 * Base class for command events.
 */
public abstract class CommandEvent implements Event {

    private final Command command;
    
    /**
     * Creates a new command event.
     *
     * @param command the command
     */
    protected CommandEvent(Command command) {
        this.command = command;
    }
    
    /**
     * Gets the command.
     *
     * @return the command
     */
    public Command getCommand() {
        return command;
    }
}

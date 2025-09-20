package fr.farmvivi.fluxcord.api.command.event;

import fr.farmvivi.fluxcord.api.command.Command;
import fr.farmvivi.fluxcord.api.event.Event;

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

package fr.farmvivi.discordbot.core.command.parser.event;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Event representing a command executed from the console.
 */
public class ConsoleCommandEvent extends Event {
    
    private final String input;
    
    /**
     * Creates a new console command event.
     *
     * @param jda   the JDA instance
     * @param input the console input
     */
    public ConsoleCommandEvent(@NotNull JDA jda, @NotNull String input) {
        super(jda);
        this.input = input;
    }
    
    /**
     * Gets the console input.
     *
     * @return the input string
     */
    @NotNull
    public String getInput() {
        return input;
    }
}
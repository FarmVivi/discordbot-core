package fr.farmvivi.discordbot.core.command.system;

import fr.farmvivi.discordbot.core.api.command.CommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Utility class for system commands to handle common functionality.
 */
public final class SystemCommandUtils {
    
    private SystemCommandUtils() {
        // Utility class, prevent instantiation
    }
    
    /**
     * Sets up ephemeral responses for system commands.
     * This method handles the logic for both slash commands and other command types,
     * ensuring ephemeral responses work correctly without code duplication.
     * 
     * @param context the command context
     */
    public static void setupEphemeralResponse(CommandContext context) {
        // For slash commands, defer reply as ephemeral to avoid timeout
        if (context.getOriginalEvent() instanceof SlashCommandInteractionEvent slashEvent && !slashEvent.isAcknowledged()) {
            context.deferReply(true);
        } else {
            // For non-slash commands, set ephemeral for direct replies
            context.setEphemeral(true);
        }
    }
}
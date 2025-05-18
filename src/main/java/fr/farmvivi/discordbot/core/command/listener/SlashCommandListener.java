package fr.farmvivi.discordbot.core.command.listener;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandContext;
import fr.farmvivi.discordbot.core.api.command.CommandResult;
import fr.farmvivi.discordbot.core.api.command.CommandService;
import fr.farmvivi.discordbot.core.api.command.event.CommandExecuteEvent;
import fr.farmvivi.discordbot.core.api.command.event.CommandExecutedEvent;
import fr.farmvivi.discordbot.core.command.impl.CommandContextImpl;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Listener for slash command interactions.
 */
public class SlashCommandListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SlashCommandListener.class);
    
    private final CommandService commandService;
    
    /**
     * Creates a new SlashCommandListener.
     *
     * @param commandService the command service
     */
    public SlashCommandListener(CommandService commandService) {
        this.commandService = commandService;
    }
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Récupérer la commande
        String commandName = event.getName();
        Optional<Command> optCommand = commandService.getRegistry().getCommand(commandName);
        
        if (optCommand.isEmpty()) {
            // Commande non trouvée
            event.reply("Commande inconnue.").setEphemeral(true).queue();
            return;
        }
        
        Command command = optCommand.get();
        
        // Vérifier si la commande est activée (via le registry impl)
        if (!command.isEnabled()) {
            event.reply("Cette commande est désactivée.").setEphemeral(true).queue();
            return;
        }
        
        // Vérifier si la commande est réservée aux serveurs
        if (command.isGuildOnly() && !event.isFromGuild()) {
            event.reply("Cette commande ne peut être utilisée que sur un serveur.").setEphemeral(true).queue();
            return;
        }
        
        // Vérifier le cooldown
        String userId = event.getUser().getId();
        if (commandService.isOnCooldown(userId, commandName)) {
            int remainingSeconds = commandService.getRemainingCooldown(userId, commandName);
            event.reply("Vous devez attendre " + remainingSeconds + " secondes avant de réutiliser cette commande.")
                .setEphemeral(true)
                .queue();
            return;
        }
        
        // Créer le contexte d'exécution
        CommandContext context = new CommandContextImpl(event, command);
        
        // Déclencher l'événement pré-exécution
        CommandExecuteEvent preEvent = new CommandExecuteEvent(command, context);
        commandService.getJDA().getEventManager().handle(preEvent);
        
        // Vérifier si l'événement a été annulé
        if (preEvent.isCancelled()) {
            logger.debug("Command execution cancelled by event: {}", commandName);
            return;
        }
        
        // Exécuter la commande
        long startTime = System.nanoTime();
        CommandResult result;
        
        try {
            result = command.execute(context);
        } catch (Exception e) {
            logger.error("Error executing command: {}", commandName, e);
            event.reply("Une erreur est survenue lors de l'exécution de la commande.")
                .setEphemeral(true)
                .queue();
            result = CommandResult.failure("Exception: " + e.getMessage());
        }
        
        long endTime = System.nanoTime();
        long executionTimeMs = (endTime - startTime) / 1_000_000;
        
        // Enregistrer les statistiques
        commandService.recordCommandExecution(result, executionTimeMs);
        
        // Déclencher l'événement post-exécution
        CommandExecutedEvent postEvent = new CommandExecutedEvent(command, context, result, executionTimeMs);
        commandService.getJDA().getEventManager().handle(postEvent);
        
        // Gérer le cooldown si la commande a un cooldown défini
        if (command.getCooldown() > 0 && result.isSuccess()) {
            commandService.setCooldown(userId, commandName, command.getCooldown());
        }
        
        logger.debug("Command {} executed in {}ms with result: {}", 
            commandName, executionTimeMs, result.isSuccess() ? "success" : "failure");
    }
}

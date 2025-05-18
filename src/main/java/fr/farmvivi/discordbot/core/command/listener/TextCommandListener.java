package fr.farmvivi.discordbot.core.command.listener;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandContext;
import fr.farmvivi.discordbot.core.api.command.CommandResult;
import fr.farmvivi.discordbot.core.api.command.CommandService;
import fr.farmvivi.discordbot.core.api.command.event.CommandExecuteEvent;
import fr.farmvivi.discordbot.core.api.command.event.CommandExecutedEvent;
import fr.farmvivi.discordbot.core.command.impl.CommandContextImpl;
import fr.farmvivi.discordbot.core.command.impl.parser.TextCommandParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Listener for text commands.
 */
public class TextCommandListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TextCommandListener.class);
    
    private final CommandService commandService;
    
    /**
     * Creates a new TextCommandListener.
     *
     * @param commandService the command service
     */
    public TextCommandListener(CommandService commandService) {
        this.commandService = commandService;
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignorer les messages des bots
        if (event.getAuthor().isBot()) {
            return;
        }
        
        // Obtenir le préfixe approprié
        String prefix;
        if (event.isFromGuild()) {
            prefix = commandService.getPrefix(event.getGuild().getId());
        } else {
            prefix = commandService.getPrefix();
        }
        
        // Vérifier si le message commence par le préfixe
        String content = event.getMessage().getContentRaw();
        if (!content.startsWith(prefix)) {
            return;
        }
        
        // Extraire la commande et les arguments
        String[] parts = content.substring(prefix.length()).trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String argsString = parts.length > 1 ? parts[1] : "";
        
        // Rechercher la commande (par nom ou alias)
        Optional<Command> optCommand = commandService.getRegistry().getCommand(commandName);
        if (optCommand.isEmpty()) {
            optCommand = commandService.getRegistry().getCommandByAlias(commandName);
            if (optCommand.isEmpty()) {
                // Commande non trouvée
                return;
            }
        }
        
        Command command = optCommand.get();
        
        // Vérifier si la commande est activée
        if (!command.isEnabled()) {
            event.getChannel().sendMessage("Cette commande est désactivée.").queue();
            return;
        }
        
        // Vérifier si la commande est réservée aux serveurs
        if (command.isGuildOnly() && !event.isFromGuild()) {
            event.getChannel().sendMessage("Cette commande ne peut être utilisée que sur un serveur.").queue();
            return;
        }
        
        // Vérifier le cooldown
        String userId = event.getAuthor().getId();
        if (commandService.isOnCooldown(userId, command.getName())) {
            int remainingSeconds = commandService.getRemainingCooldown(userId, command.getName());
            event.getChannel().sendMessage("Vous devez attendre " + remainingSeconds + 
                " secondes avant de réutiliser cette commande.").queue();
            return;
        }
        
        // Parser les arguments
        TextCommandParser parser = new TextCommandParser(command, argsString);
        
        // Créer le contexte d'exécution
        CommandContext context = new CommandContextImpl(event, command, parser.parse());
        
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
            event.getChannel().sendMessage("Une erreur est survenue lors de l'exécution de la commande.").queue();
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
            commandService.setCooldown(userId, command.getName(), command.getCooldown());
        }
        
        logger.debug("Command {} executed in {}ms with result: {}", 
            commandName, executionTimeMs, result.isSuccess() ? "success" : "failure");
    }
}

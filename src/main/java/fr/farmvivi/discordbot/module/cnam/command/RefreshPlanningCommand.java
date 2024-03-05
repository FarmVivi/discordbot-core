package fr.farmvivi.discordbot.module.cnam.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.utils.Debouncer;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class RefreshPlanningCommand extends Command {
    private final Debouncer planningScraperDebouncer;

    private long lastRefresh = 0;

    public RefreshPlanningCommand(Debouncer planningScraperDebouncer) {
        super("planning_refresh", CommandCategory.CNAM, "Forcer la mise à jour du planning");

        this.setAdminOnly(true);
        this.setGuildOnly(false);

        this.planningScraperDebouncer = planningScraperDebouncer;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        reply.setEphemeral(true);

        long currentTime = System.currentTimeMillis();

        // Si une mise à jour à déjà été effectuée dans la dernière minute, on refuse
        if (currentTime - lastRefresh < 60000) {
            reply.error("Une mise à jour a déjà été effectuée dans la dernière minute, veuillez réessayer plus tard.");
            return false;
        }

        // Schedule planning scraping
        planningScraperDebouncer.debounce();

        // Update last refresh time
        lastRefresh = currentTime;

        // Send success message
        reply.success("La mise à jour du planning a été programmée.");

        return true;
    }
}

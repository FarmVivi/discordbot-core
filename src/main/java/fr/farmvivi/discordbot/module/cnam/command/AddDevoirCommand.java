package fr.farmvivi.discordbot.module.cnam.command;

import fr.farmvivi.discordbot.module.cnam.database.DatabaseManager;
import fr.farmvivi.discordbot.module.cnam.form.devoir.add.AddDevoirForm;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.forms.Form;
import fr.farmvivi.discordbot.module.forms.FormsModule;
import fr.farmvivi.discordbot.utils.event.IEventManager;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class AddDevoirCommand extends Command {
    private final DatabaseManager databaseManager;
    private final IEventManager eventManager;
    private final FormsModule forms;

    public AddDevoirCommand(DatabaseManager databaseManager, IEventManager eventManager, FormsModule forms) {
        super("devoir_add", CommandCategory.CNAM, "Ajouter un devoir");

        this.setGuildOnly(true);

        this.databaseManager = databaseManager;
        this.eventManager = eventManager;
        this.forms = forms;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        if (event.getOriginalEvent() instanceof IReplyCallback replyCallback) {
            Form form = new AddDevoirForm(databaseManager, eventManager, forms);
            form.start(replyCallback);
        } else {
            reply.error("Une erreur est survenue lors de l'exécution de la commande.");
            return false;
        }

        return true;
    }
}

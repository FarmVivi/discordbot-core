package fr.farmvivi.discordbot.module.cnam.command;

import fr.farmvivi.discordbot.module.cnam.CnamModule;
import fr.farmvivi.discordbot.module.cnam.form.devoir.add.AddDevoirForm;
import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.forms.Form;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class AddDevoirCommand extends Command {
    private final CnamModule module;

    public AddDevoirCommand(CnamModule module) {
        super("add_devoir", CommandCategory.CNAM, "Ajouter un devoir");

        this.setGuildOnly(true);

        this.module = module;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        if (event.getOriginalEvent() instanceof IReplyCallback replyCallback) {
            Form form = new AddDevoirForm(module);
            form.start(replyCallback);
        } else {
            reply.addContent("Une erreur est survenue lors de l'exécution de la commande.");
            return false;
        }

        return true;
    }
}

package fr.farmvivi.animecity.jda;

import fr.farmvivi.animecity.Bot;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
    public static final String ADMIN_ID = "177135083222859776";
    public static final String PREFIX = "*";

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT)) {
            if (event.getMessage().getContentDisplay().equals(PREFIX + "shutdown")) {
                if (event.getAuthor().getId().equals(ADMIN_ID)) {
                    Bot.getInstance().shutdown(event.getMessage());
                } else {
                    event.getMessage().getChannel()
                            .sendMessage("Vous n'avez pas la permission d'exécuter cette commande.").queue();
                }
            }
        }
    }
}

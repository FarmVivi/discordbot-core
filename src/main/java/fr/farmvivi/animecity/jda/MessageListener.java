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
            System.out.printf("[%s][%s] %#s: %s%n", event.getGuild().getName(), event.getChannel().getName(),
                    event.getAuthor(), event.getMessage().getContentDisplay());
            if (event.getMessage().getContentDisplay().equals(PREFIX + "shutdown")) {
                if (event.getAuthor().getId().equals(ADMIN_ID)) {
                    Bot.getInstance().shutdown(event.getMessage());
                } else {
                    event.getMessage().getChannel()
                            .sendMessage("Vous n'avez pas la permission d'exécuter cette commande.").queue();
                }
            } else if (event.getMessage().getContentDisplay().contains(PREFIX + "play")) {
                Bot.getInstance().getMusicController().playMusic(event.getTextChannel(), event.getAuthor(),
                        event.getMessage().getContentDisplay());
            } else if (event.getMessage().getContentDisplay().contains(PREFIX + "skip")) {
                Bot.getInstance().getMusicController().skipMusic(event.getTextChannel());
            } else if (event.getMessage().getContentDisplay().contains(PREFIX + "clear")) {
                Bot.getInstance().getMusicController().clearMusic(event.getTextChannel());
            } else if (event.getMessage().getContentDisplay().contains(PREFIX + "current")) {
                Bot.getInstance().getMusicController().currentMusic(event.getTextChannel());
            } else if (event.getMessage().getContentDisplay().contains(PREFIX + "stop")) {
                Bot.getInstance().getMusicController().stopMusic(event.getTextChannel());
            } else if (event.getMessage().getContentDisplay().contains(PREFIX + "disconnect")) {
                Bot.getInstance().getMusicController().disconnectMusic(event.getTextChannel());
            } else if (event.getMessage().getContentDisplay().contains(PREFIX + "pause")) {
                Bot.getInstance().getMusicController().pauseMusic(event.getTextChannel());
            } else if (event.getMessage().getContentDisplay().contains(PREFIX + "loop")) {
                Bot.getInstance().getMusicController().loopMusic(event.getTextChannel());
            } else if (event.getMessage().getContentDisplay().contains(PREFIX + "volume")) {
                Bot.getInstance().getMusicController().volumeMusic(event.getTextChannel(),
                        event.getMessage().getContentDisplay());
            }
        } else {
            System.out.printf("[PM] %#s: %s%n", event.getAuthor(), event.getMessage().getContentDisplay());
        }
    }
}

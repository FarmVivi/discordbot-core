package fr.farmvivi.animecity;

import fr.farmvivi.animecity.jda.JDAManager;
import fr.farmvivi.animecity.jda.MessageListener;
import fr.farmvivi.animecity.music.MusicController;
import net.dv8tion.jda.api.entities.Message;

public class Bot {
    private static Bot instance;
    private MessageListener messageListener;

    private MusicController musicController = new MusicController();

    public Bot() {
        JDAManager.getShardManager();
        messageListener = new MessageListener();
        JDAManager.getShardManager().addEventListener(messageListener);
    }

    public static Bot getInstance() {
        return instance;
    }

    public static void setInstance(Bot instance) {
        Bot.instance = instance;
    }

    public void shutdown(Message message) {
        new Thread(() -> {
            System.out.println("Shutdown... (requested by " + message.getAuthor().getName() + ")");
            JDAManager.getShardManager().removeEventListener(messageListener);
            message.getChannel().sendMessage("Shutdown... (requested by " + message.getAuthor().getName() + ")")
                    .queue();
            musicController.getMusicManager().getAudioPlayerManager().shutdown();
            JDAManager.getShardManager().shutdown();
        }).start();
    }

    public MusicController getMusicController() {
        return musicController;
    }
}

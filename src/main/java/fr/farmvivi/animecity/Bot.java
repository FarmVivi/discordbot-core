package fr.farmvivi.animecity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.farmvivi.animecity.command.CommandsManager;
import fr.farmvivi.animecity.jda.JDAManager;
import fr.farmvivi.animecity.jda.MessageListener;
import fr.farmvivi.animecity.music.MusicController;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;

public class Bot {
    private static Bot instance;

    public static final String version = "1.0.0.0";
    public static final String name = "Feder";
    public static final boolean production = false;

    public static final Logger logger = LoggerFactory.getLogger(name);

    private MessageListener messageListener;
    private CommandsManager commandsManager;

    private MusicController musicController = new MusicController();

    public Bot() {
        logger.info("Démarrage de " + name + " (V" + version + ") (Prod: " + production + ") en cours...");

        logger.info("System.getProperty('os.name') == '" + System.getProperty("os.name") + "'");
        logger.info("System.getProperty('os.version') == '" + System.getProperty("os.version") + "'");
        logger.info("System.getProperty('os.arch') == '" + System.getProperty("os.arch") + "'");
        logger.info("System.getProperty('java.version') == '" + System.getProperty("java.version") + "'");
        logger.info("System.getProperty('java.vendor') == '" + System.getProperty("java.vendor") + "'");
        logger.info("System.getProperty('sun.arch.data.model') == '" + System.getProperty("sun.arch.data.model") + "'");

        JDAManager.getShardManager();
        messageListener = new MessageListener();
        commandsManager = new CommandsManager();
        JDAManager.getShardManager().addEventListener(messageListener);
        JDAManager.getShardManager().addEventListener(commandsManager);
        setDefaultActivity();
    }

    public static Bot getInstance() {
        return instance;
    }

    public static void setInstance(Bot instance) {
        Bot.instance = instance;
    }

    public void shutdown(Message message) {
        new Thread(() -> {
            logger.info("Shutdown... (requested by " + message.getAuthor().getName() + ")");
            JDAManager.getShardManager().removeEventListener(messageListener);
            JDAManager.getShardManager().removeEventListener(commandsManager);
            message.getChannel().sendMessage("Shutdown... (requested by " + message.getAuthor().getName() + ")")
                    .queue();
            musicController.getMusicManager().getAudioPlayerManager().shutdown();
            JDAManager.getShardManager().shutdown();
        }).start();
    }

    public void setDefaultActivity() {
        if (production)
            JDAManager.getShardManager().setActivity(Activity.playing("Prod - V" + version));
        else
            JDAManager.getShardManager().setActivity(Activity.playing("V" + version));
    }

    public MusicController getMusicController() {
        return musicController;
    }
}

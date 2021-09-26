package fr.farmvivi.animecity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.farmvivi.animecity.command.CommandsManager;
import fr.farmvivi.animecity.jda.JDAManager;
import fr.farmvivi.animecity.music.MusicManager;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;

public class Bot {
    private static Bot instance;

    public static final String version = "1.0.1.0";
    public static final String name = "AnimeCity";
    public static final boolean production = false;

    public static final Logger logger = LoggerFactory.getLogger(name);

    public static String JDA_TOKEN;
    public static String SPOTIFY_CLIENT_ID;
    public static String SPOTIFY_CLIENT_SECRET;

    private final CommandsManager commandsManager = new CommandsManager();
    private final MusicManager musicManager = new MusicManager();

    public Bot(String[] args) {
        logger.info("Démarrage de " + name + " (V" + version + ") (Prod: " + production + ") en cours...");

        logger.info("System.getProperty('os.name') == '" + System.getProperty("os.name") + "'");
        logger.info("System.getProperty('os.version') == '" + System.getProperty("os.version") + "'");
        logger.info("System.getProperty('os.arch') == '" + System.getProperty("os.arch") + "'");
        logger.info("System.getProperty('java.version') == '" + System.getProperty("java.version") + "'");
        logger.info("System.getProperty('java.vendor') == '" + System.getProperty("java.vendor") + "'");
        logger.info("System.getProperty('sun.arch.data.model') == '" + System.getProperty("sun.arch.data.model") + "'");

        if (args.length < 3) {
            logger.warn("Need at least 3 arguments to start.");
            System.exit(1);
            return;
        }

        JDA_TOKEN = args[0];
        SPOTIFY_CLIENT_ID = args[1];
        SPOTIFY_CLIENT_SECRET = args[2];

        JDAManager.getShardManager();
        JDAManager.getShardManager().addEventListener(commandsManager);
        setDefaultActivity();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown asked!");
            shutdown(null);
            logger.info("Bye!");
        }));
    }

    public static Bot getInstance() {
        return instance;
    }

    public static void setInstance(Bot instance) {
        Bot.instance = instance;
    }

    public void shutdown(Message message) {
        new Thread(() -> {
            if (message != null)
                logger.info("Shutdown... (requested by " + message.getAuthor().getAsTag() + ")");
            JDAManager.getShardManager().removeEventListener(commandsManager);
            musicManager.getAudioPlayerManager().shutdown();
            JDAManager.getShardManager().shutdown();
        }).start();
    }

    public void setDefaultActivity() {
        if (production)
            JDAManager.getShardManager().setActivity(Activity.playing("Prod - V" + version));
        else
            JDAManager.getShardManager().setActivity(Activity.playing("V" + version));
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }
}

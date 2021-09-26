package fr.farmvivi.animecity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.farmvivi.animecity.command.CommandsManager;
import fr.farmvivi.animecity.jda.JDAManager;
import fr.farmvivi.animecity.music.MusicManager;
import net.dv8tion.jda.api.entities.Activity;

public class Bot {
    private static Bot instance;

    public static final String version = "1.1.0.0";
    public static final String name = "AnimeCity";
    public static final boolean production = false;

    public static final Logger logger = LoggerFactory.getLogger(name);

    private final Configuration configuration;
    private final CommandsManager commandsManager;
    private final MusicManager musicManager;

    public Bot(String[] args) {
        logger.info("Démarrage de " + name + " (V" + version + ") (Prod: " + production + ") en cours...");

        logger.info("System.getProperty('os.name') == '" + System.getProperty("os.name") + "'");
        logger.info("System.getProperty('os.version') == '" + System.getProperty("os.version") + "'");
        logger.info("System.getProperty('os.arch') == '" + System.getProperty("os.arch") + "'");
        logger.info("System.getProperty('java.version') == '" + System.getProperty("java.version") + "'");
        logger.info("System.getProperty('java.vendor') == '" + System.getProperty("java.vendor") + "'");
        logger.info("System.getProperty('sun.arch.data.model') == '" + System.getProperty("sun.arch.data.model") + "'");

        instance = this;

        configuration = new Configuration();
        JDAManager.getShardManager();
        commandsManager = new CommandsManager();
        JDAManager.getShardManager().addEventListener(commandsManager);
        musicManager = new MusicManager();
        setDefaultActivity();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown asked!");
            JDAManager.getShardManager().removeEventListener(commandsManager);
            musicManager.getAudioPlayerManager().shutdown();
            JDAManager.getShardManager().shutdown();
            logger.info("Bye!");
        }));
    }

    public static Bot getInstance() {
        return instance;
    }

    public static void setInstance(Bot instance) {
        Bot.instance = instance;
    }

    public void setDefaultActivity() {
        if (production)
            JDAManager.getShardManager().setActivity(Activity.playing("Prod - V" + version));
        else
            JDAManager.getShardManager().setActivity(Activity.playing("V" + version));
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public CommandsManager getCommandsManager() {
        return commandsManager;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }
}

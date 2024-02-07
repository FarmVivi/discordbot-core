package fr.farmvivi.discordbot;

import fr.farmvivi.discordbot.jda.JDAManager;
import fr.farmvivi.discordbot.module.ModulesManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

public class Bot {
    public static final String name;
    public static final String version;
    public static final boolean production;
    public static final Logger logger;
    private static Bot instance;

    static {
        Properties properties = new Properties();
        try {
            properties.load(Bot.class.getClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException e) {
            System.out.println("ERROR: Cannot read properties file ! Using default values");
            System.exit(1);
        }

        name = properties.getProperty("name");
        version = properties.getProperty("version");
        production = !Bot.version.contains("-SNAPSHOT");

        logger = LoggerFactory.getLogger(Bot.name);
    }

    private final Configuration configuration;
    private final ModulesManager modulesManager;

    public Bot() {
        logger.info("Démarrage de " + name + " v" + version + " en cours...");

        logger.info("System.getProperty('os.name') == '" + System.getProperty("os.name") + "'");
        logger.info("System.getProperty('os.version') == '" + System.getProperty("os.version") + "'");
        logger.info("System.getProperty('os.arch') == '" + System.getProperty("os.arch") + "'");
        logger.info("System.getProperty('java.version') == '" + System.getProperty("java.version") + "'");
        logger.info("System.getProperty('java.vendor') == '" + System.getProperty("java.vendor") + "'");
        logger.info("System.getProperty('sun.arch.data.model') == '" + System.getProperty("sun.arch.data.model") + "'");

        instance = this;

        configuration = new Configuration();

        try {
            logger.info("Connecting to Discord API...");
            JDAManager.getJDA().awaitReady();
        } catch (InterruptedException e) {
            logger.error("Cannot connect to Discord API", e);
        }

        modulesManager = new ModulesManager(this);

        try {
            logger.info("Loading modules...");
            modulesManager.loadModules();
        } catch (Exception e) {
            logger.error("Cannot load modules", e);
            System.exit(1);
        }

        setDefaultActivity();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown asked!");

            modulesManager.unloadModules();

            JDA jda = JDAManager.getJDA();

            // Initiating the shutdown, this closes the gateway connection and subsequently closes the requester queue
            jda.shutdown();
            // Allow at most 30 seconds for remaining requests to finish
            try {
                if (!jda.awaitShutdown(Duration.ofSeconds(30))) { // returns true if shutdown is graceful, false if timeout exceeded
                    jda.shutdownNow(); // Cancel all remaining requests, and stop thread-pools
                    jda.awaitShutdown(); // Wait until shutdown is complete (indefinitely)
                }
            } catch (InterruptedException e) {
                logger.error("Cannot shutdown JDA", e);
            }

            logger.info("Bye!");
        }));
    }

    public static Bot getInstance() {
        return instance;
    }

    public static Activity getDefaultActivity() {
        return Activity.playing("v" + version);
    }

    public static void setDefaultActivity() {
        JDAManager.getJDA().getPresence().setActivity(getDefaultActivity());
    }

    public static boolean isDefaultActivity() {
        Activity currentActivity = JDAManager.getJDA().getPresence().getActivity();
        if (currentActivity == null) {
            return false;
        }
        Activity defaultActivity = getDefaultActivity();
        return currentActivity.equals(defaultActivity);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ModulesManager getModulesManager() {
        return modulesManager;
    }
}

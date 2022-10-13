package fr.farmvivi.discordbot.module.cnam;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.Configuration;
import fr.farmvivi.discordbot.jda.JDAManager;
import fr.farmvivi.discordbot.module.Module;
import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.cnam.database.DatabaseManager;
import fr.farmvivi.discordbot.module.cnam.task.PlanningScrapperTask;
import net.dv8tion.jda.api.JDA;

import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CnamModule extends Module {
    private final Bot bot;
    private final DatabaseManager databaseManager;
    private final ScheduledExecutorService executorService;

    private final PlanningScrapperTask planningScrapperTask;

    public CnamModule(Bot bot) {
        super(Modules.CNAM);

        this.bot = bot;

        Configuration configuration = bot.getConfiguration();
        try {
            String databaseHost = configuration.getValue("CNAM_DATABASE_HOST");
            String databaseUser = configuration.getValue("CNAM_DATABASE_USER");
            String databasePass = configuration.getValue("CNAM_DATABASE_PASSWORD");
            String databaseName = configuration.getValue("CNAM_DATABASE_NAME");
            int databasePort = Integer.parseInt(configuration.getValue("CNAM_DATABASE_PORT"));

            this.databaseManager = new DatabaseManager(databaseHost, databaseUser, databasePass, databaseName, databasePort);
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }

        this.executorService = new ScheduledThreadPoolExecutor(1);
        try {
            String planningCodeScolarite = configuration.getValue("CNAM_PLANNING_CODE_SCOLARITE");
            String planningUid = configuration.getValue("CNAM_PLANNING_UID");

            this.planningScrapperTask = new PlanningScrapperTask(planningCodeScolarite, planningUid, databaseManager.getDatabaseAccess());
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPreEnable() {
        super.onPreEnable();

        logger.info("Setting French locale");

        Locale.setDefault(Locale.FRENCH);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        logger.info("Connecting to database...");

        databaseManager.getDatabaseAccess().initPool();

        logger.info("Registering listeners...");

        try {
            long goulagRoleId = Long.parseLong(bot.getConfiguration().getValue("GOULAG_ROLE"));

            JDA jda = JDAManager.getJDA();

            jda.addEventListener(new GoulagRemoverEventHandler(executorService, jda.getRoleById(goulagRoleId), databaseManager.getDatabaseAccess()));
        } catch (Configuration.ValueNotFoundException e) {
            logger.warn("Failed to load goulag remover because Goulag module is not loaded");
        }

        try {
            String planningLogsChannelId = bot.getConfiguration().getValue("CNAM_PLANNING_LOGS_CHANNEL_ID");

            planningScrapperTask.registerListener(new PlanningEventHandler(JDAManager.getJDA().getTextChannelById(planningLogsChannelId)));
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPostEnable() {
        super.onPostEnable();

        logger.info("Starting planning scrapper task...");

        try {
            int scrapperTaskDelay = Integer.parseInt(bot.getConfiguration().getValue("CNAM_PLANNING_SCRAPPER_TASK_DELAY"));

            executorService.scheduleAtFixedRate(planningScrapperTask, 0, scrapperTaskDelay, TimeUnit.MINUTES);
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPreDisable() {
        super.onPreDisable();

        logger.info("Stopping planning scrapper...");

        executorService.shutdown();
        try {
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!executorService.isTerminated()) {
            logger.warn("Planning scrapper didn't stop in time, forcing shutdown");
            executorService.shutdownNow();
        }

        logger.info("Closing planning scrapper...");

        planningScrapperTask.close();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        logger.info("Disconnecting from database...");

        databaseManager.getDatabaseAccess().closePool();
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}

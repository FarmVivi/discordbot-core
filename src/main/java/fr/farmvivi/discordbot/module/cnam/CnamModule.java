package fr.farmvivi.discordbot.module.cnam;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.Configuration;
import fr.farmvivi.discordbot.jda.JDAManager;
import fr.farmvivi.discordbot.module.Module;
import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.cnam.command.AddDevoirCommand;
import fr.farmvivi.discordbot.module.cnam.command.EditDevoirCommand;
import fr.farmvivi.discordbot.module.cnam.database.DatabaseManager;
import fr.farmvivi.discordbot.module.cnam.task.PlanningDailyPrintTask;
import fr.farmvivi.discordbot.module.cnam.task.PlanningExporterTask;
import fr.farmvivi.discordbot.module.cnam.task.PlanningScrapperTask;
import fr.farmvivi.discordbot.module.commands.CommandsModule;
import fr.farmvivi.discordbot.module.forms.FormsModule;

import java.io.File;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CnamModule extends Module {
    private final Bot bot;
    private final DatabaseManager databaseManager;
    private final ScheduledExecutorService scheduler;

    private final PlanningScrapperTask planningScrapperTask;
    private final PlanningExporterTask planningExporterTask;
    private final PlanningDailyPrintTask planningDailyPrintTask;

    private final DevoirEventHandler devoirEventHandler;
    private GoulagRemoverEventHandler goulagRemoverEventHandler;
    private PlanningEventHandler planningEventHandler;

    private FormsModule formsModule;

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

        // Scheduler
        this.scheduler = new ScheduledThreadPoolExecutor(1);

        // Planning scrapper
        try {
            String planningCodeScolarite = configuration.getValue("CNAM_PLANNING_CODE_SCOLARITE");
            String planningUid = configuration.getValue("CNAM_PLANNING_UID");
            int planningYear = Integer.parseInt(configuration.getValue("CNAM_PLANNING_YEAR"));

            this.planningScrapperTask = new PlanningScrapperTask(planningYear, planningCodeScolarite, planningUid, databaseManager.getDatabaseAccess());
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Planning exporter
        try {
            int planningYear = Integer.parseInt(configuration.getValue("CNAM_PLANNING_YEAR"));
            String planningExportFilePath = configuration.getValue("CNAM_PLANNING_EXPORT_FILE_PATH");

            File planningFile = new File(planningExportFilePath);

            this.planningExporterTask = new PlanningExporterTask(planningYear, planningFile, databaseManager.getDatabaseAccess());
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Planning daily print
        try {
            String planningLogsChannelId = bot.getConfiguration().getValue("CNAM_PLANNING_DAILY_CHANNEL_ID");

            this.planningDailyPrintTask = new PlanningDailyPrintTask(JDAManager.getJDA().getTextChannelById(planningLogsChannelId), databaseManager.getDatabaseAccess());
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Devoirs
        try {
            String devoirTODOChannelId = bot.getConfiguration().getValue("CNAM_DEVOIRS_TODO_CHANNEL_ID");
            String devoirAlertChannelId = bot.getConfiguration().getValue("CNAM_DEVOIRS_ALERT_CHANNEL_ID");

            this.devoirEventHandler = new DevoirEventHandler(this, JDAManager.getJDA().getTextChannelById(devoirTODOChannelId), JDAManager.getJDA().getTextChannelById(devoirAlertChannelId));
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

        JDAManager.getJDA().addEventListener(devoirEventHandler);

        try {
            long goulagRoleId = Long.parseLong(bot.getConfiguration().getValue("GOULAG_ROLE"));

            this.goulagRemoverEventHandler = new GoulagRemoverEventHandler(scheduler, JDAManager.getJDA().getRoleById(goulagRoleId), databaseManager.getDatabaseAccess());
            JDAManager.getJDA().addEventListener(goulagRemoverEventHandler);
        } catch (Configuration.ValueNotFoundException e) {
            logger.warn("Failed to load goulag remover because Goulag module is not loaded");
        }

        try {
            String planningLogsChannelId = bot.getConfiguration().getValue("CNAM_PLANNING_LOGS_CHANNEL_ID");

            this.planningEventHandler = new PlanningEventHandler(JDAManager.getJDA().getTextChannelById(planningLogsChannelId));
            planningScrapperTask.registerListener(planningEventHandler);
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }

        logger.info("Registering commands...");

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);

        commandsModule.registerCommand(module, new AddDevoirCommand(this, devoirEventHandler));
        commandsModule.registerCommand(module, new EditDevoirCommand(this, devoirEventHandler));
    }

    @Override
    public void onPostEnable() {
        super.onPostEnable();

        formsModule = (FormsModule) bot.getModulesManager().getModule(Modules.FORMS);

        logger.info("Starting planning scrapper task...");

        try {
            int scrapperTaskDelay = Integer.parseInt(bot.getConfiguration().getValue("CNAM_PLANNING_SCRAPPER_DELAY"));

            // Scrap planning after 1 minute, then every X minutes
            scheduler.scheduleAtFixedRate(planningScrapperTask, 1, scrapperTaskDelay, TimeUnit.MINUTES);
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }

        logger.info("Starting planning exporter task...");

        try {
            int exporterTaskDelay = Integer.parseInt(bot.getConfiguration().getValue("CNAM_PLANNING_EXPORTER_DELAY"));

            // Export planning after 2 minutes, then every X minutes
            scheduler.scheduleAtFixedRate(planningExporterTask, 2, exporterTaskDelay, TimeUnit.MINUTES);
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }

        logger.info("Starting planning daily print task...");

        try {
            int dailyPrintTaskDelay = Integer.parseInt(bot.getConfiguration().getValue("CNAM_PLANNING_DAILY_PRINT_HOUR"));

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
            ZonedDateTime nextRun = now.withHour(dailyPrintTaskDelay).withMinute(0).withSecond(0);
            if (now.compareTo(nextRun) > 0)
                nextRun = nextRun.plusDays(1);

            Duration duration = Duration.between(now, nextRun);
            long initialDelay = duration.getSeconds();

            scheduler.scheduleAtFixedRate(planningDailyPrintTask, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPreDisable() {
        super.onPreDisable();

        logger.info("Unregistering event listener...");

        JDAManager.getJDA().removeEventListener(devoirEventHandler);
        if (goulagRemoverEventHandler != null) {
            JDAManager.getJDA().removeEventListener(goulagRemoverEventHandler);
            goulagRemoverEventHandler = null;
        }
        if (planningEventHandler != null) {
            planningScrapperTask.unregisterListener(planningEventHandler);
            planningEventHandler = null;
        }

        logger.info("Stopping planning scrapper...");

        scheduler.shutdown();
        try {
            scheduler.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!scheduler.isTerminated()) {
            logger.warn("Planning scrapper didn't stop in time, forcing shutdown");
            scheduler.shutdownNow();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        logger.info("Unregistering commands...");

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);

        commandsModule.unregisterCommands(module);

        logger.info("Unregistering forms...");

        formsModule.unregisterForms(module);

        logger.info("Disconnecting from database...");

        databaseManager.getDatabaseAccess().closePool();
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public FormsModule getFormsModule() {
        return formsModule;
    }
}

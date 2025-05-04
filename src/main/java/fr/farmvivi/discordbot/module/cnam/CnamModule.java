package fr.farmvivi.discordbot.module.cnam;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.Configuration;
import fr.farmvivi.discordbot.jda.JDAManager;
import fr.farmvivi.discordbot.module.Module;
import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.cnam.command.AddDevoirCommand;
import fr.farmvivi.discordbot.module.cnam.command.EditDevoirCommand;
import fr.farmvivi.discordbot.module.cnam.command.RefreshPlanningCommand;
import fr.farmvivi.discordbot.module.cnam.database.DatabaseManager;
import fr.farmvivi.discordbot.module.cnam.task.PlanningDailyPrintTask;
import fr.farmvivi.discordbot.module.cnam.task.PlanningExporterTask;
import fr.farmvivi.discordbot.module.cnam.task.PlanningScrapperTask;
import fr.farmvivi.discordbot.module.commands.CommandsModule;
import fr.farmvivi.discordbot.module.forms.FormsModule;
import fr.farmvivi.discordbot.core.util.Debouncer;
import fr.farmvivi.discordbot.utils.event.EventManager;
import fr.farmvivi.discordbot.utils.event.IEventManager;

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
    private DatabaseManager databaseManager;
    private ScheduledExecutorService scheduler;
    private IEventManager eventManager;

    private PlanningScrapperTask planningScrapperTask;
    private PlanningExporterTask planningExporterTask;
    private PlanningDailyPrintTask planningDailyPrintTask;

    private Debouncer planningScrapperDebouncer;
    private Debouncer planningExportDebouncer;

    private PlanningEventHandler planningEventHandler;
    private DevoirEventHandler devoirEventHandler;
    private GoulagRemoverEventHandler goulagRemoverEventHandler;

    private FormsModule formsModule;

    public CnamModule(Bot bot) {
        super(Modules.CNAM);

        this.bot = bot;
    }

    @Override
    public void onPreEnable() {
        super.onPreEnable();

        // Retrieve forms module
        formsModule = (FormsModule) bot.getModulesManager().getModule(Modules.FORMS);

        // Language
        logger.info("Setting French locale");
        Locale.setDefault(Locale.FRENCH);

        // Database
        logger.info("Setting up database from configuration...");
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
        logger.info("Setting up scheduler...");
        this.scheduler = new ScheduledThreadPoolExecutor(1);

        // Event manager
        logger.info("Setting up event manager...");
        this.eventManager = new EventManager();

        // Planning scrapper
        logger.info("Setting up planning scrapper task...");
        try {
            String planningCodeScolarite = configuration.getValue("CNAM_PLANNING_CODE_SCOLARITE");
            String planningUid = configuration.getValue("CNAM_PLANNING_UID");
            int planningYear = Integer.parseInt(configuration.getValue("CNAM_PLANNING_YEAR"));

            this.planningScrapperTask = new PlanningScrapperTask(planningYear, planningCodeScolarite, planningUid, eventManager, databaseManager);
            this.planningScrapperDebouncer = new Debouncer(15000, () -> {
                logger.info("Running planning scrapper task...");
                scheduler.execute(planningScrapperTask);
            });
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Planning exporter
        logger.info("Setting up planning exporter task...");
        try {
            int planningYear = Integer.parseInt(configuration.getValue("CNAM_PLANNING_YEAR"));
            String planningExportFilePath = configuration.getValue("CNAM_PLANNING_EXPORT_FILE_PATH");

            File planningFile = new File(planningExportFilePath);

            this.planningExporterTask = new PlanningExporterTask(planningYear, planningFile, databaseManager);
            this.planningExportDebouncer = new Debouncer(15000, () -> {
                logger.info("Running planning exporter task...");
                scheduler.execute(planningExporterTask);
            });
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Planning daily print
        logger.info("Setting up planning daily print task...");
        try {
            String planningLogsChannelId = bot.getConfiguration().getValue("CNAM_PLANNING_DAILY_CHANNEL_ID");

            this.planningDailyPrintTask = new PlanningDailyPrintTask(JDAManager.getJDA().getTextChannelById(planningLogsChannelId), databaseManager);
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Planning logs
        logger.info("Setting up planning event handler...");
        try {
            String planningLogsChannelId = bot.getConfiguration().getValue("CNAM_PLANNING_LOGS_CHANNEL_ID");

            this.planningEventHandler = new PlanningEventHandler(planningExportDebouncer, JDAManager.getJDA().getTextChannelById(planningLogsChannelId));
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Devoirs
        logger.info("Setting up devoir event handler...");
        try {
            String devoirTODOChannelId = bot.getConfiguration().getValue("CNAM_DEVOIRS_TODO_CHANNEL_ID");
            String devoirAlertChannelId = bot.getConfiguration().getValue("CNAM_DEVOIRS_ALERT_CHANNEL_ID");

            this.devoirEventHandler = new DevoirEventHandler(databaseManager, planningExportDebouncer, JDAManager.getJDA().getTextChannelById(devoirTODOChannelId), JDAManager.getJDA().getTextChannelById(devoirAlertChannelId));
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Goulag remover
        logger.info("Setting up goulag remover event handler...");
        try {
            long goulagRoleId = Long.parseLong(bot.getConfiguration().getValue("GOULAG_ROLE"));

            this.goulagRemoverEventHandler = new GoulagRemoverEventHandler(scheduler, JDAManager.getJDA().getRoleById(goulagRoleId), databaseManager);
        } catch (Configuration.ValueNotFoundException e) {
            logger.warn("Failed to load goulag remover because Goulag module is not loaded");
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // Connect to database
        logger.info("Connecting to database...");
        databaseManager.getDatabaseAccess().initPool();

        // Registering listeners
        logger.info("Registering listeners...");
        // JDA
        JDAManager.getJDA().addEventListener(devoirEventHandler);
        if (goulagRemoverEventHandler != null) {
            JDAManager.getJDA().addEventListener(goulagRemoverEventHandler);
        }
        // Event manager
        eventManager.register(planningEventHandler);
        eventManager.register(devoirEventHandler);

        // Registering commands
        logger.info("Registering commands...");
        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);
        commandsModule.registerCommand(module, new AddDevoirCommand(databaseManager, eventManager, formsModule));
        commandsModule.registerCommand(module, new EditDevoirCommand(databaseManager, eventManager, formsModule));
        commandsModule.registerCommand(module, new RefreshPlanningCommand(planningScrapperDebouncer, planningExportDebouncer));

        // Starting planning scrapper task
        logger.info("Starting planning scrapper task...");
        try {
            int scrapperTaskDelay = Integer.parseInt(bot.getConfiguration().getValue("CNAM_PLANNING_SCRAPPER_DELAY"));

            // Scrap planning after 1 minute, then every X minutes
            scheduler.scheduleAtFixedRate(planningScrapperDebouncer::debounce, 1, scrapperTaskDelay, TimeUnit.MINUTES);
        } catch (Configuration.ValueNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Starting planning daily print task
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

        // Unregistering listeners
        logger.info("Unregistering event listener...");
        // JDA
        JDAManager.getJDA().removeEventListener(devoirEventHandler);
        if (goulagRemoverEventHandler != null) {
            JDAManager.getJDA().removeEventListener(goulagRemoverEventHandler);
        }
        // Event manager
        eventManager.unregister(planningEventHandler);
        eventManager.unregister(devoirEventHandler);

        // Shutdown scheduler
        logger.info("Shutting down scheduler...");
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Planning scrapper shutdown interrupted", e);
        }
        if (!scheduler.isTerminated()) {
            logger.warn("Planning scrapper didn't stop in time, forcing shutdown");
            scheduler.shutdownNow();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // Unregistering commands
        logger.info("Unregistering commands...");
        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);
        commandsModule.unregisterCommands(module);

        // Unregistering forms
        logger.info("Unregistering forms...");
        formsModule.unregisterForms(module);

        // Disconnect from database
        logger.info("Disconnecting from database...");
        databaseManager.getDatabaseAccess().closePool();
    }
}

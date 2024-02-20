package fr.farmvivi.discordbot.module.cnam.task.planning;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.jda.JDAManager;
import org.htmlunit.*;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSelect;
import org.htmlunit.javascript.SilentJavaScriptErrorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class PlanningScrapper implements Closeable {
    private static final String PLANNING_URL = "https://senesi.gescicca.net/Planning.aspx?code_scolarite=%s&uid=%s";

    private final int year;
    private final String codeScolarite;
    private final String uid;

    private final Logger logger = LoggerFactory.getLogger(PlanningScrapper.class);

    private WebClient webClient;
    private HtmlPage webPage;
    private ByteArrayOutputStream csvFile;

    public PlanningScrapper(int year, String codeScolarite, String uid) {
        this.year = year;
        this.codeScolarite = codeScolarite;
        this.uid = uid;
    }

    /**
     * Scrap planning from the website and retrieve cours data
     *
     * @return List of cours or null if an error occurred
     */
    public synchronized List<PlanningItem> scrap() {
        long start = System.currentTimeMillis();

        List<PlanningItem> cours = null;

        logger.info("Scraping planning for codeScolarite={}", codeScolarite);

        // Build web client
        buildWebClient();

        // Retrieve planning page
        if (retrievePlanningPage() && downloadPlanningCSV()) {
            // Parse planning
            cours = parsePlanning();
        }

        // Close web client
        close();

        // WARNINGS
        if (cours == null) {
            this.warnAdminsError();
        } else if (cours.isEmpty()) {
            this.warnAdminsNoData();
        }

        long finish = System.currentTimeMillis();

        logger.info("Done (" + (float) (finish - start) / 1000 + "s)!");

        return cours;
    }

    private void buildWebClient() {
        webClient = new WebClient();
        // Enable CSS to check if elements are displayed or not
        webClient.getOptions().setCssEnabled(true);
        // Enable JavaScript to interact with the page and finally retrieve the data needed
        webClient.getOptions().setJavaScriptEnabled(true);
        // Disable throwing exceptions when encountering JavaScript errors
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        if (Bot.production) {
            // Disable logging
            webClient.setIncorrectnessListener((s, o) -> {
            });
            webClient.setCssErrorHandler(new SilentCssErrorHandler());
            webClient.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
        }
    }

    private boolean retrievePlanningPage() {
        try {
            webPage = webClient.getPage(String.format(PLANNING_URL, codeScolarite, uid));

            // Wait for the page to load
            webClient.waitForBackgroundJavaScript(60000);

            return true;
        } catch (Exception e) {
            logger.error("Error while retrieving planning page", e);
            return false;
        }
    }

    private boolean downloadPlanningCSV() {
        // Recovering the loading logo
        DomElement htmlLoadingLogoElement = webPage.getFirstByXPath("//*[@class=\"wait\"]");

        // Check that the loading logo is not visible
        if (htmlLoadingLogoElement == null || htmlLoadingLogoElement.isDisplayed()) {
            this.warnAdmins();
            return false;
        }

        // Recovering the select to choose the year
        DomElement htmlSelectYearElement = webPage.getElementById("m_c_planning_ddlANNSCO");

        // Check that the select is present and is HtmlSelect
        if (htmlSelectYearElement == null || !(htmlSelectYearElement instanceof HtmlSelect htmlSelectYear)) {
            this.warnAdmins();
            return false;
        }

        // Recovering the select option for the year
        HtmlOption htmlSelectYearOption;
        try {
            htmlSelectYearOption = htmlSelectYear.getOptionByValue(String.valueOf(year));
        } catch (ElementNotFoundException e) {
            this.warnAdmins();
            return false;
        }

        // Check that the option is present and is not disabled
        if (htmlSelectYearOption == null || htmlSelectYearOption.isDisabled()) {
            this.warnAdmins();
            return false;
        }

        // Select the year
        htmlSelectYear.setSelectedAttribute(htmlSelectYearOption, true);

        // Wait for the page to load
        webClient.waitForBackgroundJavaScript(60000);

        // Refresh elements
        htmlLoadingLogoElement = webPage.getFirstByXPath("//*[@class=\"wait\"]");

        // Check that the loading logo is not visible
        if (htmlLoadingLogoElement == null || htmlLoadingLogoElement.isDisplayed()) {
            this.warnAdmins();
            return false;
        }

        // Recovering the button to download CSV file
        DomElement htmlBtnDownloadCSVElement = webPage.getElementById("m_c_planning_lbExporterPlanningCSV");

        // Check that the button is present and has not changed (it should be " Planning au format CSV")
        if (htmlBtnDownloadCSVElement == null || !htmlBtnDownloadCSVElement.getVisibleText().equals(" Planning au format CSV")) {
            this.warnAdmins();
            return false;
        }

        Semaphore waitNewWindowSemaphore = new Semaphore(0);

        // Prepare to save the CSV file
        WebWindowListener listener = new WebWindowListener() {
            @Override
            public void webWindowOpened(WebWindowEvent event) {
                // Do nothing
            }

            @Override
            public void webWindowContentChanged(WebWindowEvent event) {
                // Cleanup the page
                webPage.cleanUp();
                webPage = null;

                // Get the new window
                Page page = event.getWebWindow().getEnclosedPage();

                // Retrieve the content type and charset
                String type = page.getWebResponse().getContentType();
                Charset charset = page.getWebResponse().getContentCharset();

                // Check if the new window is the CSV file
                if (!type.equals("application/octet-stream") || !charset.equals(StandardCharsets.ISO_8859_1)) {
                    PlanningScrapper.this.warnAdmins();
                    waitNewWindowSemaphore.release();
                    return;
                }

                // Store the CSV file
                try (InputStream is = page.getWebResponse().getContentAsStream()) {
                    csvFile = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        csvFile.write(buffer, 0, len);
                    }
                } catch (IOException e) {
                    logger.error("Error while reading the CSV file", e);
                }

                // Release the semaphore
                waitNewWindowSemaphore.release();
            }

            @Override
            public void webWindowClosed(WebWindowEvent event) {
                // Do nothing
            }
        };

        // Add the listener
        webClient.addWebWindowListener(listener);

        // Click on the button to download the CSV file
        try {
            htmlBtnDownloadCSVElement.click();
        } catch (IOException e) {
            logger.error("Error while clicking on element", e);
            return false;
        }

        // Wait for the new window to open
        try {
            // acquire with timeout = 60s
            if (!waitNewWindowSemaphore.tryAcquire(60, TimeUnit.SECONDS)) {
                logger.error("Timeout while waiting for the new window to open");
                return false;
            }
        } catch (InterruptedException e) {
            logger.error("Error while waiting for the new window to open", e);
            return false;
        }

        // Remove the listener
        webClient.removeWebWindowListener(listener);

        // Check if the CSV file has been downloaded
        if (csvFile == null) {
            logger.error("Error while downloading the CSV file");
            return false;
        }

        return true;
    }

    private List<PlanningItem> parsePlanning() {
        List<PlanningItem> cours = new LinkedList<>();

        // Read the CSV file with OpenCSV
        try (CSVReader reader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(csvFile.toByteArray()), StandardCharsets.ISO_8859_1))) {
            // Retrieve the header
            String[] header = reader.readNext();

            // Check if the header is correct
            if (header == null || header.length != 22
                    || !header[0].equals("Objet")
                    || !header[1].equals("Début")
                    || !header[2].equals("Début")
                    || !header[3].equals("Fin")
                    || !header[4].equals("Fin")
                    || !header[5].equals("Journée entière")
                    || !header[6].equals("Rappel actif/inactif")
                    || !header[7].equals("Date de rappel")
                    || !header[8].equals("Heure du rappel")
                    || !header[9].equals("Organisateur d'une réunion")
                    || !header[10].equals("Participants obligatoires")
                    || !header[11].equals("Participants facultatifs")
                    || !header[12].equals("Ressources de la réunion")
                    || !header[13].equals("Afficher la disponibilité")
                    || !header[14].equals("Catégories")
                    || !header[15].equals("Critère de diffusion")
                    || !header[16].equals("Description")
                    || !header[17].equals("Emplacement")
                    || !header[18].equals("Informations facturation")
                    || !header[19].equals("Kilométrage")
                    || !header[20].equals("Priorité")
                    || !header[21].equals("Privé")
            ) {
                this.warnAdmins();
                return null;
            }

            // Iterate over all lines
            String[] line;
            while ((line = reader.readNext()) != null) {
                // Parse the line
                try {
                    PlanningItem coursElement = parseCours(line);
                    if (coursElement != null) {
                        cours.add(coursElement);
                    } else {
                        this.warnAdmins();
                    }
                } catch (Exception e) {
                    this.warnAdmins();
                    logger.error("Error while parsing the CSV file", e);
                }
            }
        } catch (IOException e) {
            logger.error("Error while reading the CSV file", e);
        } catch (CsvValidationException e) {
            logger.error("Error while parsing the CSV file", e);
        }

        return cours;
    }

    private PlanningItem parseCours(String[] line) {
        // Check if the line is correct
        if (line.length != 22) {
            return null;
        }

        // A: Objet (ex. ACC002 - Cours 2h)
        String A = line[0];

        String[] UESplit = A.split(" - ");
        String codeUE = UESplit[0];
        String[] UESplit2 = UESplit[1].split(" ");
        PlanningItemType type = PlanningItemType.fromCSV(UESplit2[0]);
        if (type == null) {
            this.warnAdmins();
            return null;
        }

        // B: Début (ex. 21/02/2021)
        String B = line[1];

        // C: Début (ex. 14:30:00)
        String C = line[2];

        LocalDateTime dateDebut = LocalDateTime.of(LocalDate.parse(B, new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter()), LocalTime.parse(C, new DateTimeFormatterBuilder().appendPattern("HH:mm:ss").toFormatter()));

        // D: Fin (ex. 21/02/2021)
        String D = line[3];

        // E: Fin (ex. 16:30:00)
        String E = line[4];

        LocalDateTime dateFin = LocalDateTime.of(LocalDate.parse(D, new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter()), LocalTime.parse(E, new DateTimeFormatterBuilder().appendPattern("HH:mm:ss").toFormatter()));

        // F: Journée entière (ex. Faux)
        String F = line[5];

        if (!F.equals("Faux") && !F.equals("Vrai")) {
            this.warnAdmins();
            return null;
        }

        boolean journeeEntiere = F.equals("Vrai");

        // G: Rappel actif/inactif (ex. Faux)
        String G = line[6];

        if (!G.equals("Faux") && !G.equals("Vrai")) {
            this.warnAdmins();
            return null;
        }

        boolean rappelActif = G.equals("Vrai");

        // H: Date de rappel (ex. 21/02/2021)
        String H = line[7];

        // I: Heure du rappel (ex. 14:30:00)
        String I = line[8];

        LocalDateTime dateRappel = null;
        if (H != null && !H.isEmpty() && I != null && !I.isEmpty()) {
            dateRappel = LocalDateTime.of(LocalDate.parse(H, new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter()), LocalTime.parse(I, new DateTimeFormatterBuilder().appendPattern("HH:mm:ss").toFormatter()));
        }

        // J: Organisateur d'une réunion (ex. M. DUPONT Jean)
        String J = line[9];

        String[] organisateurSplit = J.split(" ");
        String organisateurNom = null;
        String organisateurPrenom = null;
        if (J.equals("Enseignant inconnu")) {
            organisateurNom = "INCONNU";
            organisateurPrenom = "INCONNU";
        } else if (organisateurSplit.length < 3) {
            this.warnAdmins();
            return null;
        } else {
            organisateurNom = organisateurSplit[1];
            organisateurPrenom = J.substring(J.indexOf(organisateurNom) + organisateurNom.length() + 1);
        }

        // K: Participants obligatoires (ex. M. DURAND Paul)
        String K = line[10];

        String participantsObligatoires = K;

        // L: Participants facultatifs (ex. M. MARTIN Jacques)
        String L = line[11];

        String participantsFacultatifs = L;

        // M: Ressources de la réunion (ex. )
        String M = line[12];

        String ressources = M;

        // N: Afficher la disponibilité (ex. 2)
        String N = line[13];

        String afficherDisponibilite = N;

        // O: Catégories (ex. )
        String O = line[14];

        String categories = O;

        // P: Critère de diffusion (ex. )
        String P = line[15];

        String critereDiffusion = P;

        // Q: Description (ex. ACC002 : Unité d'accompagnement)
        String Q = line[16];

        String description = Q;

        // R: Emplacement (ex. ), (ex. Distanciel     - Salle Salle virtuelle), (ex. CNAM 292 Rue Saint-Martin  75003 PARIS - Salle Salle 172)
        String R = line[17];

        String emplacementAdresse = "";
        String emplacementSalle = "INCONNU";
        boolean emplacementPresentiel = true;
        if (R != null && !R.isEmpty()) {
            String[] emplacement = R.split(" - ");
            if (emplacement.length == 2) {
                emplacementAdresse = emplacement[0];
                emplacementSalle = emplacement[1];

                // Fix pour "Salle Salle"
                emplacementSalle = emplacementSalle.replace("Salle Salle", "Salle");

                // Fix pour les salles virtuelles
                if (emplacementSalle.contains("virtuelle") && emplacementAdresse.contains("Distanciel")) {
                    emplacementPresentiel = false;
                }
            } else {
                emplacementSalle = R;
            }
        }

        // S: Informations facturation (ex. )
        String S = line[18];

        String informationsFacturation = S;

        // T: Kilométrage (ex. )
        String T = line[19];

        String kilometrage = T;

        // U: Priorité (ex. Normale)
        String U = line[20];

        PlanningItemPriorite priorite = PlanningItemPriorite.fromCSV(U);
        if (priorite == null) {
            this.warnAdmins();
            return null;
        }

        // V: Privé (ex. Faux)
        String V = line[21];

        if (!V.equals("Faux") && !V.equals("Vrai")) {
            this.warnAdmins();
            return null;
        }

        boolean prive = V.equals("Vrai");

        return new PlanningItem(codeUE, type, dateDebut, dateFin, journeeEntiere, rappelActif, dateRappel, organisateurNom, organisateurPrenom, participantsObligatoires, participantsFacultatifs, ressources, afficherDisponibilite, categories, critereDiffusion, description, emplacementAdresse, emplacementSalle, emplacementPresentiel, informationsFacturation, kilometrage, priorite, prive);
    }

    private void warnAdmins() {
        // Retrieve the called line number
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String calledLineNumber = stackTraceElements[2].getLineNumber() + "";
        String calledClassName = stackTraceElements[2].getClassName();
        String calledMethodName = stackTraceElements[2].getMethodName();

        String message = ":warning: CNAM - PlanningScrapper: Planning source code page seems to have changed, please review the source code (**" + calledClassName + "**, **" + calledMethodName + "**, line **" + calledLineNumber + "**)";

        logger.warn(message);

        for (long admin : Bot.getInstance().getConfiguration().cmdAdmins) {
            JDAManager.getJDA().retrieveUserById(admin).queue(user -> user.openPrivateChannel().queue(channel -> channel.sendMessage(message).queue()));
        }
    }

    private void warnAdminsError() {
        this.warnAdmins();
    }

    private void warnAdminsNoData() {
        this.warnAdmins();
    }

    @Override
    public void close() {
        if (csvFile != null) {
            try {
                csvFile.close();
            } catch (IOException e) {
                logger.error("Error while closing the CSV file", e);
            }
            csvFile = null;
        }
        if (webPage != null) {
            webPage.cleanUp();
            webPage = null;
        }
        if (webClient != null) {
            webClient.close();
            webClient = null;
        }
    }
}

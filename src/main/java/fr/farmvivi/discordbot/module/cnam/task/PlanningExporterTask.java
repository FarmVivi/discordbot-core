package fr.farmvivi.discordbot.module.cnam.task;

import fr.farmvivi.discordbot.module.cnam.database.DatabaseAccess;
import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.database.cours.CoursDAO;
import fr.farmvivi.discordbot.module.cnam.database.devoir.Devoir;
import fr.farmvivi.discordbot.module.cnam.database.devoir.DevoirDAO;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.EnseignantDAO;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.EnseignementDAO;
import fr.farmvivi.discordbot.module.cnam.database.salle.Salle;
import fr.farmvivi.discordbot.module.cnam.database.salle.SalleDAO;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.TextList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.model.property.immutable.ImmutableCalScale;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import net.fortuna.ical4j.validate.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class PlanningExporterTask implements Runnable {
    private final int year;
    private final File planningFile;
    private final SalleDAO salleDAO;
    private final EnseignantDAO enseignantDAO;
    private final EnseignementDAO enseignementDAO;
    private final CoursDAO coursDAO;
    private final DevoirDAO devoirDAO;

    private final Logger logger = LoggerFactory.getLogger(PlanningExporterTask.class);

    public PlanningExporterTask(int year, File planningFile, DatabaseAccess databaseAccess) {
        this.year = year;
        this.planningFile = planningFile;
        this.salleDAO = new SalleDAO(databaseAccess);
        this.enseignantDAO = new EnseignantDAO(databaseAccess);
        this.enseignementDAO = new EnseignementDAO(databaseAccess);
        this.coursDAO = new CoursDAO(databaseAccess);
        this.devoirDAO = new DevoirDAO(databaseAccess);
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        // Retrieve planning from database
        logger.info("Retrieving planning from database...");

        List<Cours> bddCourss;
        List<Devoir> bddDevoirs;
        List<Salle> bddSalles;
        List<Enseignant> bddEnseignants;
        List<Enseignement> bddEnseignements;

        try {
            bddSalles = salleDAO.selectAll();
            bddEnseignants = enseignantDAO.selectAll();
            bddEnseignements = enseignementDAO.selectAll();
            bddCourss = coursDAO.selectAll();
            bddDevoirs = devoirDAO.selectAll();
        } catch (SQLException e) {
            logger.error("Error while getting data from database", e);
            return;
        }

        // Constructing planning with ical4j
        logger.info("Constructing planning...");

        Calendar calendar = new Calendar();
        calendar.add(new ProdId("-//CnamBot//Planning 1.0//FR"));
        calendar.add(ImmutableVersion.VERSION_2_0);
        calendar.add(ImmutableCalScale.GREGORIAN);

        for (Cours cours : bddCourss) {
            Enseignement enseignement = getEnseignementForCours(cours, bddEnseignements);
            Enseignant enseignant = getEnseignantForCours(cours, bddEnseignants);
            Salle salle = getSalleForCours(cours, bddSalles);
            List<Devoir> devoirs = getDevoirsForCours(cours, bddDevoirs);
            VEvent event = createEvent(cours, enseignement, enseignant, salle, devoirs);
            calendar.add(event);
        }

        // Exporting planning to file with ical4j
        logger.info("Exporting planning to file...");

        OutputStream outputStream = null;
        CalendarOutputter outputter = new CalendarOutputter();
        try {
            planningFile.getParentFile().mkdirs();
            outputStream = new FileOutputStream(planningFile);
            outputter.output(calendar, outputStream);
        } catch (IOException e) {
            logger.error("Error while exporting planning to file", e);
        } catch (ValidationException e) {
            logger.error("Error while validating calendar", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error("Error while closing output stream", e);
                }
            }
        }

        long finish = System.currentTimeMillis();

        logger.info("Done (" + (float) (finish - start) / 1000 + "s)!");
    }

    /**
     * Créer un événement pour un cours
     *
     * @param cours        Le cours
     * @param enseignement L'enseignement
     * @param enseignant   L'enseignant
     * @param salle        La salle
     * @param devoirs      La liste des devoirs
     * @return L'événement
     */
    private VEvent createEvent(Cours cours, Enseignement enseignement, Enseignant enseignant, Salle salle, List<Devoir> devoirs) {
        VEvent event = new VEvent();

        int id = cours.getId();
        boolean examen = cours.isExamen();
        String[] categories = (examen ? new String[]{"CNAM", "Examen"} : new String[]{"CNAM", "Cours"});
        String titre = (examen ? "Examen" : "Cours") + " " + enseignement.getNom();
        LocalDateTime dateDebut = cours.getDebutCours();
        LocalDateTime dateFin = cours.getFinCours();
        StringBuilder description = new StringBuilder();
        if (salle != null) {
            description.append(salle.getNom()).append("\n");
        }
        if (enseignant != null) {
            description.append("Enseignant : ").append(enseignant.getNom()).append(" ").append(enseignant.getPrenom()).append("\n");
        }
        if (!devoirs.isEmpty()) {
            description.append("Devoirs : \n");
            for (Devoir devoir : devoirs) {
                description.append("Donné le ").append(devoir.getDatePour()).append(" : ").append(devoir.getDescription()).append("\n\n");
            }
        }

        event.add(new Uid("cnam-" + year + "-" + id));
        if (examen) {
            event.add(new Priority(Priority.VALUE_HIGH));
        }
        event.add(new Categories(new TextList(categories)));
        event.add(new Summary(titre));
        event.add(new DtStart<>(dateDebut));
        event.add(new DtEnd<>(dateFin));
        event.add(new Description(description.toString()));
        if (salle != null) {
            event.add(new Location(salle.getAdresse()));
        }

        return event;
    }


    /**
     * Méthode permettant de récupérer les devoirs pour un cours
     *
     * @param cours   Le cours
     * @param devoirs La liste des devoirs
     * @return La liste des devoirs pour le cours
     */
    private List<Devoir> getDevoirsForCours(Cours cours, List<Devoir> devoirs) {
        return devoirs.stream().filter(devoir ->
                (devoir.getIdCoursPour() != null && devoir.getIdCoursPour().equals(cours.getId())) ||
                        (devoir.getIdCoursPour() == null &&
                                devoir.getCodeEnseignement().equals(cours.getEnseignementCode()) &&
                                devoir.getIdEnseignant().equals(cours.getEnseignantId()) &&
                                devoir.getDatePour().equals(cours.getDebutCours().toLocalDate()))
        ).toList();
    }

    /**
     * Méthode permettant de récupérer l'enseignement pour un cours
     *
     * @param cours         Le cours
     * @param enseignements La liste des enseignements
     * @return L'enseignement pour le cours
     */
    private Enseignement getEnseignementForCours(Cours cours, List<Enseignement> enseignements) {
        String enseignementCode = cours.getEnseignementCode();

        return enseignements.stream().filter(enseignement -> enseignementCode.equals(enseignement.getCode())).findFirst().orElse(null);
    }

    /**
     * Méthode permettant de récupérer l'enseignant pour un cours
     *
     * @param cours       Le cours
     * @param enseignants La liste des enseignants
     * @return L'enseignant pour le cours
     */
    private Enseignant getEnseignantForCours(Cours cours, List<Enseignant> enseignants) {
        int enseignantId = cours.getEnseignantId();

        return enseignants.stream().filter(enseignant -> enseignant.getId() == enseignantId).findFirst().orElse(null);
    }

    /**
     * Méthode permettant de récupérer la salle pour un cours
     *
     * @param cours  Le cours
     * @param salles La liste des salles
     * @return La salle pour le cours
     */
    private Salle getSalleForCours(Cours cours, List<Salle> salles) {
        int salleId = cours.getSalleId();

        return salles.stream().filter(salle -> salle.getId() == salleId).findFirst().orElse(null);
    }
}

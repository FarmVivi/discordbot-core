package fr.farmvivi.discordbot.module.cnam.task;

import fr.farmvivi.discordbot.core.util.DiscordColor;
import fr.farmvivi.discordbot.module.cnam.database.DatabaseManager;
import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.database.cours.CoursDAO;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.EnseignantDAO;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.EnseignementDAO;
import fr.farmvivi.discordbot.module.cnam.database.salle.Salle;
import fr.farmvivi.discordbot.module.cnam.database.salle.SalleDAO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static java.util.Locale.FRANCE;

public class PlanningDailyPrintTask implements Runnable {
    private final TextChannel channel;
    private final CoursDAO coursDAO;
    private final EnseignementDAO enseignementDAO;
    private final EnseignantDAO enseignantDAO;
    private final SalleDAO salleDAO;

    private final Logger logger = LoggerFactory.getLogger(PlanningDailyPrintTask.class);

    public PlanningDailyPrintTask(TextChannel channel, DatabaseManager databaseManager) {
        this.channel = channel;
        this.coursDAO = new CoursDAO(databaseManager.getDatabaseAccess());
        this.enseignementDAO = new EnseignementDAO(databaseManager.getDatabaseAccess());
        this.enseignantDAO = new EnseignantDAO(databaseManager.getDatabaseAccess());
        this.salleDAO = new SalleDAO(databaseManager.getDatabaseAccess());
    }

    @Override
    public void run() {
        // Starting this task... (show planning)
        logger.info("Showing planning...");

        // Affichage du planning
        try {
            LocalDate date = LocalDate.now().plusDays(1);
            List<Cours> cours = coursDAO.selectAllByDate(date);
            if (cours.isEmpty()) {
                return;
            }
            Collections.sort(cours);
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(DiscordColor.BLURPLE.getColor());
            embedBuilder.setTitle("Planning du " + date.getDayOfWeek().getDisplayName(TextStyle.FULL, FRANCE) + " " + date.getDayOfMonth() + " " + date.getMonth().getDisplayName(TextStyle.FULL, FRANCE) + " " + date.getYear());
            Cours previousCours = null;
            for (Cours cours1 : cours) {
                Enseignement enseignement = enseignementDAO.selectById(cours1.getEnseignementCode());
                String title = enseignement.getNom();

                Enseignant enseignant = enseignantDAO.selectById(cours1.getEnseignantId());
                Salle salle = salleDAO.selectById(cours1.getSalleId());
                String description = "De **" + cours1.getDebutCours().toLocalTime() + "** à **" + cours1.getFinCours().toLocalTime() + "** (" + calculDuree(cours1.getDebutCours().toLocalTime(), cours1.getFinCours().toLocalTime()) + ") avec " + enseignant.getPrenom() + " " + enseignant.getNom() + " en " + salle.getNom();

                if (previousCours != null && !previousCours.getFinCours().toLocalTime().equals(cours1.getDebutCours().toLocalTime())) {
                    embedBuilder.addField("Pause", "De **" + previousCours.getFinCours().toLocalTime() + "** à **" + cours1.getDebutCours().toLocalTime() + "** (" + calculDuree(previousCours.getFinCours().toLocalTime(), cours1.getDebutCours().toLocalTime()) + ")", false);
                }

                embedBuilder.addField(title, description, false);

                previousCours = cours1;
            }
            channel.sendMessageEmbeds(embedBuilder.build()).queue();

            // Ending this task... (show planning)
            logger.info("Planning showed");
        } catch (SQLException e) {
            logger.error("Error while showing planning", e);
        }
    }

    // Méthode qui calcul la durée d'un cours entre 2 LocalTime
    private String calculDuree(LocalTime debut, LocalTime fin) {
        int duree = (int) debut.until(fin, ChronoUnit.MINUTES);
        int heure = duree / 60;
        int minute = duree % 60;
        return heure + "h" + (minute != 0 ? minute : "");
    }
}

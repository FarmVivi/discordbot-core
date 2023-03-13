package fr.farmvivi.discordbot.module.cnam.task;

import fr.farmvivi.discordbot.DiscordColor;
import fr.farmvivi.discordbot.module.cnam.database.DatabaseAccess;
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

import java.awt.*;
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

    public PlanningDailyPrintTask(TextChannel channel, DatabaseAccess databaseAccess) {
        this.channel = channel;
        this.coursDAO = new CoursDAO(databaseAccess);
        this.enseignementDAO = new EnseignementDAO(databaseAccess);
        this.enseignantDAO = new EnseignantDAO(databaseAccess);
        this.salleDAO = new SalleDAO(databaseAccess);
    }

    @Override
    public void run() {
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
                String description = "De **" + cours1.getHeureDebut() + "** à **" + cours1.getHeureFin() + "** (" + calculDuree(cours1.getHeureDebut(), cours1.getHeureFin()) + ") avec " + enseignant.getPrenom() + " " + enseignant.getNom() + " en " + salle.getNom();

                if (previousCours != null && !previousCours.getHeureFin().equals(cours1.getHeureDebut())) {
                    embedBuilder.addField("Pause", "De **" + previousCours.getHeureFin() + "** à **" + cours1.getHeureDebut() + "** (" + calculDuree(previousCours.getHeureFin(), cours1.getHeureDebut()) + ")", false);
                }

                embedBuilder.addField(title, description, false);

                previousCours = cours1;
            }
            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        } catch (SQLException e) {
            e.printStackTrace();
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

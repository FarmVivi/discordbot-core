package fr.farmvivi.discordbot.module.cnam.form.devoir;

import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.database.cours.CoursDAO;
import fr.farmvivi.discordbot.module.cnam.database.devoir.DevoirDAO;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.EnseignantDAO;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.EnseignementDAO;

import java.time.LocalDate;

public interface DevoirForm {
    DevoirDAO getDevoirDAO();

    CoursDAO getCoursDAO();

    EnseignantDAO getEnseignantDAO();

    EnseignementDAO getEnseignementDAO();

    Enseignement getEnseignement();

    void setEnseignement(Enseignement enseignement);

    Cours getCoursDonne();

    void setCoursDonne(Cours coursDonne);

    Cours getCoursPour();

    void setCoursPour(Cours coursPour);

    LocalDate getDatePour();

    void setDatePour(LocalDate datePour);

    Enseignant getEnseignant();

    void setEnseignant(Enseignant enseignant);

    String getDescription();

    void setDescription(String description);
}

package fr.farmvivi.discordbot.module.cnam.events.cours;

import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
import fr.farmvivi.discordbot.module.cnam.database.salle.Salle;
import fr.farmvivi.discordbot.utils.event.IEvent;

public class CoursCreateEvent implements IEvent {
    private final Cours cours;
    private final Salle salleCours;
    private final Enseignant enseignantCours;
    private final Enseignement enseignementCours;

    public CoursCreateEvent(Cours cours, Salle salleCours, Enseignant enseignantCours, Enseignement enseignementCours) {
        this.cours = cours;
        this.salleCours = salleCours;
        this.enseignantCours = enseignantCours;
        this.enseignementCours = enseignementCours;
    }

    public Cours getCours() {
        return cours;
    }

    public Salle getSalleCours() {
        return salleCours;
    }

    public Enseignant getEnseignantCours() {
        return enseignantCours;
    }

    public Enseignement getEnseignementCours() {
        return enseignementCours;
    }
}

package fr.farmvivi.discordbot.module.cnam.events.cours;

import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
import fr.farmvivi.discordbot.module.cnam.database.salle.Salle;
import fr.farmvivi.discordbot.utils.event.IEvent;

public class CoursUpdateEvent implements IEvent {
    private final Cours oldCours;
    private final Cours newCours;
    private final Salle newSalleCours;
    private final Enseignant newEnseignantCours;
    private final Enseignement newEnseignementCours;

    public CoursUpdateEvent(Cours oldCours, Cours newCours, Salle newSalleCours, Enseignant newEnseignantCours, Enseignement newEnseignementCours) {
        this.oldCours = oldCours;
        this.newCours = newCours;
        this.newSalleCours = newSalleCours;
        this.newEnseignantCours = newEnseignantCours;
        this.newEnseignementCours = newEnseignementCours;
    }

    public Cours getOldCours() {
        return oldCours;
    }

    public Cours getNewCours() {
        return newCours;
    }

    public Salle getNewSalleCours() {
        return newSalleCours;
    }

    public Enseignant getNewEnseignantCours() {
        return newEnseignantCours;
    }

    public Enseignement getNewEnseignementCours() {
        return newEnseignementCours;
    }
}

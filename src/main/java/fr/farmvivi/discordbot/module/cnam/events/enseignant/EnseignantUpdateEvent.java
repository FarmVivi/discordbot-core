package fr.farmvivi.discordbot.module.cnam.events.enseignant;

import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;

public class EnseignantUpdateEvent {
    private final Enseignant oldEnseignant;
    private final Enseignant newEnseignant;

    public EnseignantUpdateEvent(Enseignant oldEnseignant, Enseignant newEnseignant) {
        this.oldEnseignant = oldEnseignant;
        this.newEnseignant = newEnseignant;
    }

    public Enseignant getOldEnseignant() {
        return oldEnseignant;
    }

    public Enseignant getNewEnseignant() {
        return newEnseignant;
    }
}

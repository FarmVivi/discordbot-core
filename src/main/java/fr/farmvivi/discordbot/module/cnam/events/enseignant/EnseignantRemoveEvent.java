package fr.farmvivi.discordbot.module.cnam.events.enseignant;

import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;

public class EnseignantRemoveEvent {
    private final Enseignant enseignant;

    public EnseignantRemoveEvent(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }
}

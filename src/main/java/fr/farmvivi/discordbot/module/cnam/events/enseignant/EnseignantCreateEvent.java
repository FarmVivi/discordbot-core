package fr.farmvivi.discordbot.module.cnam.events.enseignant;

import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;

public class EnseignantCreateEvent {
    private final Enseignant enseignant;

    public EnseignantCreateEvent(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }
}

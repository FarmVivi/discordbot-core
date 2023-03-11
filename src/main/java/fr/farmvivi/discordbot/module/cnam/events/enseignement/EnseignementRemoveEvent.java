package fr.farmvivi.discordbot.module.cnam.events.enseignement;

import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;

public class EnseignementRemoveEvent {
    private final Enseignement enseignement;

    public EnseignementRemoveEvent(Enseignement enseignement) {
        this.enseignement = enseignement;
    }

    public Enseignement getEnseignement() {
        return enseignement;
    }
}

package fr.farmvivi.discordbot.module.cnam.events.enseignement;

import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;

public class EnseignementCreateEvent {
    private final Enseignement enseignement;

    public EnseignementCreateEvent(Enseignement enseignement) {
        this.enseignement = enseignement;
    }

    public Enseignement getEnseignement() {
        return enseignement;
    }
}

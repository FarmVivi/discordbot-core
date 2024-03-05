package fr.farmvivi.discordbot.module.cnam.events.enseignement;

import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
import fr.farmvivi.discordbot.utils.event.IEvent;

public class EnseignementUpdateEvent implements IEvent {
    private final Enseignement oldEnseignement;
    private final Enseignement newEnseignement;

    public EnseignementUpdateEvent(Enseignement oldEnseignement, Enseignement newEnseignement) {
        this.oldEnseignement = oldEnseignement;
        this.newEnseignement = newEnseignement;
    }

    public Enseignement getOldEnseignement() {
        return oldEnseignement;
    }

    public Enseignement getNewEnseignement() {
        return newEnseignement;
    }
}

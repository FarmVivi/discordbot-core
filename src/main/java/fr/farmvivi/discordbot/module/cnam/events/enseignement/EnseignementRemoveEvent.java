package fr.farmvivi.discordbot.module.cnam.events.enseignement;

import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
import fr.farmvivi.discordbot.utils.event.IEvent;

public class EnseignementRemoveEvent implements IEvent {
    private final Enseignement enseignement;

    public EnseignementRemoveEvent(Enseignement enseignement) {
        this.enseignement = enseignement;
    }

    public Enseignement getEnseignement() {
        return enseignement;
    }
}

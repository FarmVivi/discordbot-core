package fr.farmvivi.discordbot.module.cnam.events.enseignement;

import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
import fr.farmvivi.discordbot.utils.event.IEvent;

public class EnseignementCreateEvent implements IEvent {
    private final Enseignement enseignement;

    public EnseignementCreateEvent(Enseignement enseignement) {
        this.enseignement = enseignement;
    }

    public Enseignement getEnseignement() {
        return enseignement;
    }
}

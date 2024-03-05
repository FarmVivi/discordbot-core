package fr.farmvivi.discordbot.module.cnam.events.enseignant;

import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
import fr.farmvivi.discordbot.utils.event.IEvent;

public class EnseignantCreateEvent implements IEvent {
    private final Enseignant enseignant;

    public EnseignantCreateEvent(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }
}

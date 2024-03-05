package fr.farmvivi.discordbot.module.cnam.events.enseignant;

import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
import fr.farmvivi.discordbot.utils.event.IEvent;

public class EnseignantRemoveEvent implements IEvent {
    private final Enseignant enseignant;

    public EnseignantRemoveEvent(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }
}

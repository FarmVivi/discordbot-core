package fr.farmvivi.discordbot.module.cnam.events.salle;

import fr.farmvivi.discordbot.module.cnam.database.salle.Salle;

public class SalleUpdateEvent {
    private final Salle oldSalle;
    private final Salle newSalle;

    public SalleUpdateEvent(Salle oldSalle, Salle newSalle) {
        this.oldSalle = oldSalle;
        this.newSalle = newSalle;
    }

    public Salle getOldSalle() {
        return oldSalle;
    }

    public Salle getNewSalle() {
        return newSalle;
    }
}

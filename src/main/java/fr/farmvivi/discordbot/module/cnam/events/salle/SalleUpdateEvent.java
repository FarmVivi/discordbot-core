package fr.farmvivi.discordbot.module.cnam.events.salle;

import fr.farmvivi.discordbot.module.cnam.database.salle.Salle;
import fr.farmvivi.discordbot.utils.event.IEvent;

public class SalleUpdateEvent implements IEvent {
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

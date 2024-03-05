package fr.farmvivi.discordbot.module.cnam.events.salle;

import fr.farmvivi.discordbot.module.cnam.database.salle.Salle;
import fr.farmvivi.discordbot.utils.event.IEvent;

public class SalleCreateEvent implements IEvent {
    private final Salle salle;

    public SalleCreateEvent(Salle salle) {
        this.salle = salle;
    }

    public Salle getSalle() {
        return salle;
    }
}

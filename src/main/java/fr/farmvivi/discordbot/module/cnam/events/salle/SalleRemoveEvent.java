package fr.farmvivi.discordbot.module.cnam.events.salle;

import fr.farmvivi.discordbot.module.cnam.database.salle.Salle;

public class SalleRemoveEvent {
    private final Salle salle;

    public SalleRemoveEvent(Salle salle) {
        this.salle = salle;
    }

    public Salle getSalle() {
        return salle;
    }
}

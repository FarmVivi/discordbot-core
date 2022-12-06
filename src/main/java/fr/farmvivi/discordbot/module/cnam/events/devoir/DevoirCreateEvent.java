package fr.farmvivi.discordbot.module.cnam.events.devoir;

import fr.farmvivi.discordbot.module.cnam.database.devoir.Devoir;

public class DevoirCreateEvent {
    private final Devoir devoir;

    public DevoirCreateEvent(Devoir devoir) {
        this.devoir = devoir;
    }

    public Devoir getDevoir() {
        return devoir;
    }
}

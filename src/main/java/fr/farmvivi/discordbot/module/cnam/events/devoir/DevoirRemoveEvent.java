package fr.farmvivi.discordbot.module.cnam.events.devoir;

import fr.farmvivi.discordbot.module.cnam.database.devoir.Devoir;
import fr.farmvivi.discordbot.utils.event.IEvent;

public class DevoirRemoveEvent implements IEvent {
    private final Devoir devoir;

    public DevoirRemoveEvent(Devoir devoir) {
        this.devoir = devoir;
    }

    public Devoir getDevoir() {
        return devoir;
    }
}

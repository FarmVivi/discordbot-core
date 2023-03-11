package fr.farmvivi.discordbot.module.cnam.events;

import fr.farmvivi.discordbot.module.cnam.events.devoir.DevoirCreateEvent;
import fr.farmvivi.discordbot.module.cnam.events.devoir.DevoirRemoveEvent;
import fr.farmvivi.discordbot.module.cnam.events.devoir.DevoirUpdateEvent;

public interface DevoirListener {
    void onDevoirCreate(DevoirCreateEvent event);

    void onDevoirRemove(DevoirRemoveEvent event);

    void onDevoirUpdate(DevoirUpdateEvent event);
}

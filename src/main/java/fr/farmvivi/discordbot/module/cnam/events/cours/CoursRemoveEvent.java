package fr.farmvivi.discordbot.module.cnam.events.cours;

import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;

public class CoursRemoveEvent {
    private final Cours cours;

    public CoursRemoveEvent(Cours cours) {
        this.cours = cours;
    }

    public Cours getCours() {
        return cours;
    }
}

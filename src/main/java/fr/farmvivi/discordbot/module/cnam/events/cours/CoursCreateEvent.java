package fr.farmvivi.discordbot.module.cnam.events.cours;

import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;

public class CoursCreateEvent {
    private final Cours cours;

    public CoursCreateEvent(Cours cours) {
        this.cours = cours;
    }

    public Cours getCours() {
        return cours;
    }
}

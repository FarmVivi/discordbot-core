package fr.farmvivi.discordbot.module.cnam.events.cours;

import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;

public class CoursUpdateEvent {
    private final Cours oldCours;
    private final Cours newCours;

    public CoursUpdateEvent(Cours oldCours, Cours newCours) {
        this.oldCours = oldCours;
        this.newCours = newCours;
    }

    public Cours getOldCours() {
        return oldCours;
    }

    public Cours getNewCours() {
        return newCours;
    }
}

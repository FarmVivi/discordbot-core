package fr.farmvivi.discordbot.module.cnam.events;

import fr.farmvivi.discordbot.module.cnam.events.cours.CoursCreateEvent;
import fr.farmvivi.discordbot.module.cnam.events.cours.CoursRemoveEvent;
import fr.farmvivi.discordbot.module.cnam.events.cours.CoursUpdateEvent;
import fr.farmvivi.discordbot.module.cnam.events.enseignant.EnseignantCreateEvent;
import fr.farmvivi.discordbot.module.cnam.events.enseignant.EnseignantRemoveEvent;
import fr.farmvivi.discordbot.module.cnam.events.enseignant.EnseignantUpdateEvent;
import fr.farmvivi.discordbot.module.cnam.events.enseignement.EnseignementCreateEvent;
import fr.farmvivi.discordbot.module.cnam.events.enseignement.EnseignementRemoveEvent;
import fr.farmvivi.discordbot.module.cnam.events.enseignement.EnseignementUpdateEvent;
import fr.farmvivi.discordbot.module.cnam.events.salle.SalleCreateEvent;
import fr.farmvivi.discordbot.module.cnam.events.salle.SalleRemoveEvent;
import fr.farmvivi.discordbot.module.cnam.events.salle.SalleUpdateEvent;

public interface PlanningListener {
    void onSalleCreate(SalleCreateEvent event);

    void onSalleRemove(SalleRemoveEvent event);

    void onSalleUpdate(SalleUpdateEvent event);

    void onEnseignantCreate(EnseignantCreateEvent event);

    void onEnseignantRemove(EnseignantRemoveEvent event);

    void onEnseignantUpdate(EnseignantUpdateEvent event);

    void onEnseignementCreate(EnseignementCreateEvent event);

    void onEnseignementRemove(EnseignementRemoveEvent event);

    void onEnseignementUpdate(EnseignementUpdateEvent event);

    void onCoursCreate(CoursCreateEvent event);

    void onCoursRemove(CoursRemoveEvent event);

    void onCoursUpdate(CoursUpdateEvent event);
}

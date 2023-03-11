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

public class PlanningListenerAdapter implements PlanningListener {
    @Override
    public void onSalleCreate(SalleCreateEvent event) {

    }

    @Override
    public void onSalleRemove(SalleRemoveEvent event) {

    }

    @Override
    public void onSalleUpdate(SalleUpdateEvent event) {

    }

    @Override
    public void onEnseignantCreate(EnseignantCreateEvent event) {

    }

    @Override
    public void onEnseignantRemove(EnseignantRemoveEvent event) {

    }

    @Override
    public void onEnseignantUpdate(EnseignantUpdateEvent event) {

    }

    @Override
    public void onEnseignementCreate(EnseignementCreateEvent event) {

    }

    @Override
    public void onEnseignementRemove(EnseignementRemoveEvent event) {

    }

    @Override
    public void onEnseignementUpdate(EnseignementUpdateEvent event) {

    }

    @Override
    public void onCoursCreate(CoursCreateEvent event) {

    }

    @Override
    public void onCoursRemove(CoursRemoveEvent event) {

    }

    @Override
    public void onCoursUpdate(CoursUpdateEvent event) {

    }
}

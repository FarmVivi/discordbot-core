package fr.farmvivi.discordbot.module.cnam;

import fr.farmvivi.discordbot.module.cnam.events.PlanningListener;
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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class PlanningEventHandler implements PlanningListener {
    private final TextChannel channel;

    public PlanningEventHandler(TextChannel channel) {
        this.channel = channel;
    }

    @Override
    public void onSalleCreate(SalleCreateEvent event) {
        channel.sendMessage("Salle " + event.getSalle() + " créée").queue();
    }

    @Override
    public void onSalleRemove(SalleRemoveEvent event) {
        channel.sendMessage("Salle " + event.getSalle() + " supprimée").queue();
    }

    @Override
    public void onSalleUpdate(SalleUpdateEvent event) {
        channel.sendMessage("Salle " + event.getOldSalle() + " modifiée en " + event.getNewSalle()).queue();
    }

    @Override
    public void onEnseignantCreate(EnseignantCreateEvent event) {
        channel.sendMessage("Enseignant " + event.getEnseignant() + " créé").queue();
    }

    @Override
    public void onEnseignantRemove(EnseignantRemoveEvent event) {
        channel.sendMessage("Enseignant " + event.getEnseignant() + " supprimé").queue();
    }

    @Override
    public void onEnseignantUpdate(EnseignantUpdateEvent event) {
        channel.sendMessage("Enseignant " + event.getOldEnseignant() + " modifié en " + event.getNewEnseignant()).queue();
    }

    @Override
    public void onEnseignementCreate(EnseignementCreateEvent event) {
        channel.sendMessage("Enseignement " + event.getEnseignement() + " créé").queue();
    }

    @Override
    public void onEnseignementRemove(EnseignementRemoveEvent event) {
        channel.sendMessage("Enseignement " + event.getEnseignement() + " supprimé").queue();
    }

    @Override
    public void onEnseignementUpdate(EnseignementUpdateEvent event) {
        channel.sendMessage("Enseignement " + event.getOldEnseignement() + " modifié en " + event.getNewEnseignement()).queue();
    }

    @Override
    public void onCoursCreate(CoursCreateEvent event) {
        channel.sendMessage("Cours " + event.getCours() + " créé").queue();
    }

    @Override
    public void onCoursRemove(CoursRemoveEvent event) {
        channel.sendMessage("Cours " + event.getCours() + " supprimé").queue();
    }

    @Override
    public void onCoursUpdate(CoursUpdateEvent event) {
        channel.sendMessage("Cours " + event.getOldCours() + " modifié en " + event.getNewCours()).queue();
    }
}

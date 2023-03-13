package fr.farmvivi.discordbot.module.cnam;

import fr.farmvivi.discordbot.DiscordColor;
import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
import fr.farmvivi.discordbot.module.cnam.database.salle.Salle;
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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class PlanningEventHandler implements PlanningListener {
    private final TextChannel channel;

    public PlanningEventHandler(TextChannel channel) {
        this.channel = channel;
    }

    @Override
    public void onSalleCreate(SalleCreateEvent event) {
        channel.sendMessageEmbeds(buildSalleEmbed(buildBaseCreateEmbed("Une nouvelle salle a été ajoutée"), event.getSalle()).build()).queue();
    }

    @Override
    public void onSalleRemove(SalleRemoveEvent event) {
        channel.sendMessageEmbeds(buildSalleEmbed(buildBaseRemoveEmbed("Une salle a été supprimée"), event.getSalle()).build()).queue();
    }

    @Override
    public void onSalleUpdate(SalleUpdateEvent event) {
        channel.sendMessageEmbeds(buildSalleEmbed(buildBaseUpdateEmbed("Une salle a été modifiée"), event.getNewSalle()).build()).queue();
    }

    @Override
    public void onEnseignantCreate(EnseignantCreateEvent event) {
        channel.sendMessageEmbeds(buildEnseignantEmbed(buildBaseCreateEmbed("Un nouvel enseignant a été ajouté"), event.getEnseignant()).build()).queue();
    }

    @Override
    public void onEnseignantRemove(EnseignantRemoveEvent event) {
        channel.sendMessageEmbeds(buildEnseignantEmbed(buildBaseRemoveEmbed("Un enseignant a été supprimé"), event.getEnseignant()).build()).queue();
    }

    @Override
    public void onEnseignantUpdate(EnseignantUpdateEvent event) {
        channel.sendMessageEmbeds(buildEnseignantEmbed(buildBaseUpdateEmbed("Un enseignant a été modifié"), event.getNewEnseignant()).build()).queue();
    }

    @Override
    public void onEnseignementCreate(EnseignementCreateEvent event) {
        channel.sendMessageEmbeds(buildEnseignementEmbed(buildBaseCreateEmbed("Un nouvel enseignement a été ajouté"), event.getEnseignement()).build()).queue();
    }

    @Override
    public void onEnseignementRemove(EnseignementRemoveEvent event) {
        channel.sendMessageEmbeds(buildEnseignementEmbed(buildBaseRemoveEmbed("Un enseignement a été supprimé"), event.getEnseignement()).build()).queue();
    }

    @Override
    public void onEnseignementUpdate(EnseignementUpdateEvent event) {
        channel.sendMessageEmbeds(buildEnseignementEmbed(buildBaseUpdateEmbed("Un enseignement a été modifié"), event.getNewEnseignement()).build()).queue();
    }

    @Override
    public void onCoursCreate(CoursCreateEvent event) {
        channel.sendMessageEmbeds(buildCoursEmbed(buildBaseCreateEmbed("Un nouveau cours a été ajouté"), event.getCours(), event.getSalleCours(), event.getEnseignantCours(), event.getEnseignementCours()).build()).queue();
    }

    @Override
    public void onCoursRemove(CoursRemoveEvent event) {
        channel.sendMessageEmbeds(buildCoursEmbed(buildBaseRemoveEmbed("Un cours a été supprimé"), event.getCours(), event.getSalleCours(), event.getEnseignantCours(), event.getEnseignementCours()).build()).queue();
    }

    @Override
    public void onCoursUpdate(CoursUpdateEvent event) {
        channel.sendMessageEmbeds(buildCoursEmbed(buildBaseUpdateEmbed("Un cours a été modifié"), event.getNewCours(), event.getNewSalleCours(), event.getNewEnseignantCours(), event.getNewEnseignementCours()).build()).queue();
    }

    private EmbedBuilder buildBaseEmbed() {
        return new EmbedBuilder();
    }

    private EmbedBuilder buildBaseCreateEmbed(String title) {
        return buildBaseEmbed().setColor(DiscordColor.GREEN.getColor()).setTitle(title);
    }

    private EmbedBuilder buildBaseRemoveEmbed(String title) {
        return buildBaseEmbed().setColor(DiscordColor.RED.getColor()).setTitle(title);
    }

    private EmbedBuilder buildBaseUpdateEmbed(String title) {
        return buildBaseEmbed().setColor(DiscordColor.ORANGE.getColor()).setTitle(title);
    }

    private EmbedBuilder buildSalleEmbed(EmbedBuilder baseEmbed, Salle salle) {
        return baseEmbed.addField(salle.getNom(), salle.getAdresse(), false);
    }

    private EmbedBuilder buildEnseignantEmbed(EmbedBuilder baseEmbed, Enseignant enseignant) {
        return baseEmbed.addField(enseignant.getPrenom() + " " + enseignant.getNom(), "", false);
    }

    private EmbedBuilder buildEnseignementEmbed(EmbedBuilder baseEmbed, Enseignement enseignement) {
        return baseEmbed.addField(enseignement.getCode(), enseignement.getNom(), false);
    }

    private EmbedBuilder buildCoursEmbed(EmbedBuilder baseEmbed, Cours cours, Salle salle, Enseignant enseignant, Enseignement enseignement) {
        String description = "De **" + cours.getHeureDebut() + "** à **" + cours.getHeureFin() + "** (" + calculDuree(cours.getHeureDebut(), cours.getHeureFin()) + ") avec " + enseignant.getPrenom() + " " + enseignant.getNom() + " en " + salle.getNom();

        baseEmbed.setTitle(baseEmbed.build().getTitle() + " pour le " + cours.getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE) + " " + cours.getDate().getDayOfMonth() + " " + cours.getDate().getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE) + " " + cours.getDate().getYear());

        return baseEmbed.addField(enseignement.getNom(), description, false);
    }

    // Méthode qui calcul la durée d'un cours entre 2 LocalTime
    private String calculDuree(LocalTime debut, LocalTime fin) {
        int duree = (int) debut.until(fin, ChronoUnit.MINUTES);
        int heure = duree / 60;
        int minute = duree % 60;
        return heure + "h" + (minute != 0 ? minute : "");
    }
}

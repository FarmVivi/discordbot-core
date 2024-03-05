package fr.farmvivi.discordbot.module.cnam;

import fr.farmvivi.discordbot.DiscordColor;
import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
import fr.farmvivi.discordbot.module.cnam.database.salle.Salle;
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
import fr.farmvivi.discordbot.utils.Debouncer;
import fr.farmvivi.discordbot.utils.event.SubscribeEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class PlanningEventHandler {
    private final Debouncer planningExportDebouncer;
    private final TextChannel channel;

    public PlanningEventHandler(Debouncer planningExportDebouncer, TextChannel channel) {
        this.planningExportDebouncer = planningExportDebouncer;
        this.channel = channel;
    }

    @SubscribeEvent
    public void onSalleCreate(SalleCreateEvent event) {
        // Schedule planning export
        planningExportDebouncer.debounce();

        // Send message in log channel
        channel.sendMessageEmbeds(buildSalleEmbed(buildBaseCreateEmbed("Une nouvelle salle a été ajoutée"), event.getSalle()).build()).queue();
    }

    @SubscribeEvent
    public void onSalleRemove(SalleRemoveEvent event) {
        // Schedule planning export
        planningExportDebouncer.debounce();

        // Send message in log channel
        channel.sendMessageEmbeds(buildSalleEmbed(buildBaseRemoveEmbed("Une salle a été supprimée"), event.getSalle()).build()).queue();
    }

    @SubscribeEvent
    public void onSalleUpdate(SalleUpdateEvent event) {
        // Schedule planning export
        planningExportDebouncer.debounce();

        // Send message in log channel
        channel.sendMessageEmbeds(buildSalleEmbed(buildBaseUpdateEmbed("Une salle a été modifiée"), event.getNewSalle()).build()).queue();
    }

    @SubscribeEvent
    public void onEnseignantCreate(EnseignantCreateEvent event) {
        // Schedule planning export
        planningExportDebouncer.debounce();

        // Send message in log channel
        channel.sendMessageEmbeds(buildEnseignantEmbed(buildBaseCreateEmbed("Un nouvel enseignant a été ajouté"), event.getEnseignant()).build()).queue();
    }

    @SubscribeEvent
    public void onEnseignantRemove(EnseignantRemoveEvent event) {
        // Schedule planning export
        planningExportDebouncer.debounce();

        // Send message in log channel
        channel.sendMessageEmbeds(buildEnseignantEmbed(buildBaseRemoveEmbed("Un enseignant a été supprimé"), event.getEnseignant()).build()).queue();
    }

    @SubscribeEvent
    public void onEnseignantUpdate(EnseignantUpdateEvent event) {
        // Schedule planning export
        planningExportDebouncer.debounce();

        // Send message in log channel
        channel.sendMessageEmbeds(buildEnseignantEmbed(buildBaseUpdateEmbed("Un enseignant a été modifié"), event.getNewEnseignant()).build()).queue();
    }

    @SubscribeEvent
    public void onEnseignementCreate(EnseignementCreateEvent event) {
        // Schedule planning export
        planningExportDebouncer.debounce();

        // Send message in log channel
        channel.sendMessageEmbeds(buildEnseignementEmbed(buildBaseCreateEmbed("Un nouvel enseignement a été ajouté"), event.getEnseignement()).build()).queue();
    }

    @SubscribeEvent
    public void onEnseignementRemove(EnseignementRemoveEvent event) {
        // Schedule planning export
        planningExportDebouncer.debounce();

        // Send message in log channel
        channel.sendMessageEmbeds(buildEnseignementEmbed(buildBaseRemoveEmbed("Un enseignement a été supprimé"), event.getEnseignement()).build()).queue();
    }

    @SubscribeEvent
    public void onEnseignementUpdate(EnseignementUpdateEvent event) {
        // Schedule planning export
        planningExportDebouncer.debounce();

        // Send message in log channel
        channel.sendMessageEmbeds(buildEnseignementEmbed(buildBaseUpdateEmbed("Un enseignement a été modifié"), event.getNewEnseignement()).build()).queue();
    }

    @SubscribeEvent
    public void onCoursCreate(CoursCreateEvent event) {
        // Schedule planning export
        planningExportDebouncer.debounce();

        // Send message in log channel
        channel.sendMessageEmbeds(buildCoursEmbed(buildBaseCreateEmbed("Un nouveau cours a été ajouté"), event.getCours(), event.getSalleCours(), event.getEnseignantCours(), event.getEnseignementCours()).build()).queue();
    }

    @SubscribeEvent
    public void onCoursRemove(CoursRemoveEvent event) {
        // Schedule planning export
        planningExportDebouncer.debounce();

        // Send message in log channel
        channel.sendMessageEmbeds(buildCoursEmbed(buildBaseRemoveEmbed("Un cours a été supprimé"), event.getCours(), event.getSalleCours(), event.getEnseignantCours(), event.getEnseignementCours()).build()).queue();
    }

    @SubscribeEvent
    public void onCoursUpdate(CoursUpdateEvent event) {
        // Schedule planning export
        planningExportDebouncer.debounce();

        // Send message in log channel
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
        LocalDate date = cours.getDebutCours().toLocalDate();
        LocalTime heureDebut = cours.getDebutCours().toLocalTime();
        LocalTime heureFin = cours.getFinCours().toLocalTime();

        String description = "De **" + heureDebut + "** à **" + heureFin + "** (" + calculDuree(heureDebut, heureFin) + ") avec " + enseignant.getPrenom() + " " + enseignant.getNom() + " en " + salle.getNom();

        baseEmbed.setTitle(baseEmbed.build().getTitle() + " pour le " + date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE) + " " + date.getDayOfMonth() + " " + date.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE) + " " + date.getYear());

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

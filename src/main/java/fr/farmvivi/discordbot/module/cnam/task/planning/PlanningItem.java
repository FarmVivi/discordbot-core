package fr.farmvivi.discordbot.module.cnam.task.planning;

import java.time.LocalDateTime;

public class PlanningItem {
    private final String codeUE;
    private final PlanningItemType type;
    private final LocalDateTime dateDebut;
    private final LocalDateTime dateFin;
    private final boolean journeeEntiere;
    private final boolean rappelActif;
    private final LocalDateTime dateRappel;
    private final String organisateurNom;
    private final String organisateurPrenom;
    private final String participantsObligatoires;
    private final String participantsFacultatifs;
    private final String ressources;
    private final String afficherDisponibilite;
    private final String categories;
    private final String critereDiffusion;
    private final String description;
    private final String emplacementAdresse;
    private final String emplacementNom;
    private final boolean emplacementPresentiel;
    private final String informationsFacturation;
    private final String kilometrage;
    private final PlanningItemPriorite priorite;
    private final boolean prive;

    public PlanningItem(String codeUE, PlanningItemType type, LocalDateTime dateDebut, LocalDateTime dateFin, boolean journeeEntiere, boolean rappelActif, LocalDateTime dateRappel, String organisateurNom, String organisateurPrenom, String participantsObligatoires, String participantsFacultatifs, String ressources, String afficherDisponibilite, String categories, String critereDiffusion, String description, String emplacementAdresse, String emplacementNom, boolean emplacementPresentiel, String informationsFacturation, String kilometrage, PlanningItemPriorite priorite, boolean prive) {
        this.codeUE = codeUE;
        this.type = type;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.journeeEntiere = journeeEntiere;
        this.rappelActif = rappelActif;
        this.dateRappel = dateRappel;
        this.organisateurNom = organisateurNom;
        this.organisateurPrenom = organisateurPrenom;
        this.participantsObligatoires = participantsObligatoires;
        this.participantsFacultatifs = participantsFacultatifs;
        this.ressources = ressources;
        this.afficherDisponibilite = afficherDisponibilite;
        this.categories = categories;
        this.critereDiffusion = critereDiffusion;
        this.description = description;
        this.emplacementAdresse = emplacementAdresse;
        this.emplacementNom = emplacementNom;
        this.emplacementPresentiel = emplacementPresentiel;
        this.informationsFacturation = informationsFacturation;
        this.kilometrage = kilometrage;
        this.priorite = priorite;
        this.prive = prive;
    }

    public String getCodeUE() {
        return codeUE;
    }

    public PlanningItemType getType() {
        return type;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public boolean isJourneeEntiere() {
        return journeeEntiere;
    }

    public boolean isRappelActif() {
        return rappelActif;
    }

    public LocalDateTime getDateRappel() {
        return dateRappel;
    }

    public String getOrganisateurNom() {
        return organisateurNom;
    }

    public String getOrganisateurPrenom() {
        return organisateurPrenom;
    }

    public String getParticipantsObligatoires() {
        return participantsObligatoires;
    }

    public String getParticipantsFacultatifs() {
        return participantsFacultatifs;
    }

    public String getRessources() {
        return ressources;
    }

    public String getAfficherDisponibilite() {
        return afficherDisponibilite;
    }

    public String getCategories() {
        return categories;
    }

    public String getCritereDiffusion() {
        return critereDiffusion;
    }

    public String getDescription() {
        return description;
    }

    public String getEmplacementAdresse() {
        return emplacementAdresse;
    }

    public String getEmplacementNom() {
        return emplacementNom;
    }

    public boolean isEmplacementPresentiel() {
        return emplacementPresentiel;
    }

    public String getInformationsFacturation() {
        return informationsFacturation;
    }

    public String getKilometrage() {
        return kilometrage;
    }

    public PlanningItemPriorite getPriorite() {
        return priorite;
    }

    public boolean isPrive() {
        return prive;
    }
}

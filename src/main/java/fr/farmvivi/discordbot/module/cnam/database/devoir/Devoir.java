package fr.farmvivi.discordbot.module.cnam.database.devoir;

import java.time.LocalDate;
import java.util.Objects;

public class Devoir {
    private final int id;
    private final LocalDate datePour;
    private final String description;
    private final Boolean optionnel;
    private Long discordMessageId;
    private final Integer idEnseignant;
    private final String codeEnseignement;
    private Integer idCoursPour;
    private final Integer idCoursDonne;

    public Devoir(LocalDate datePour, String description, Boolean optionnel, Integer idEnseignant, String codeEnseignement, Integer idCoursDonne) {
        this(datePour, description, optionnel, null, idEnseignant, codeEnseignement, null, idCoursDonne);
    }

    public Devoir(LocalDate datePour, String description, Boolean optionnel, Long discordMessageId, Integer idEnseignant, String codeEnseignement, Integer idCoursPour, Integer idCoursDonne) {
        this(-1, datePour, description, optionnel, discordMessageId, idEnseignant, codeEnseignement, idCoursPour, idCoursDonne);
    }

    public Devoir(int id, LocalDate datePour, String description, Boolean optionnel, Long discordMessageId, Integer idEnseignant, String codeEnseignement, Integer idCoursPour, Integer idCoursDonne) {
        this.id = id;
        this.datePour = datePour;
        this.description = description;
        this.optionnel = optionnel;
        this.discordMessageId = discordMessageId;
        this.idEnseignant = idEnseignant;
        this.codeEnseignement = codeEnseignement;
        this.idCoursPour = idCoursPour;
        this.idCoursDonne = idCoursDonne;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDatePour() {
        return datePour;
    }

    public String getDescription() {
        return description;
    }

    public Boolean isOptionnel() {
        return optionnel;
    }

    public Long getDiscordMessageId() {
        return discordMessageId;
    }

    public void setDiscordMessageId(Long discordMessageId) {
        this.discordMessageId = discordMessageId;
    }

    public Integer getIdEnseignant() {
        return idEnseignant;
    }

    public String getCodeEnseignement() {
        return codeEnseignement;
    }

    public Integer getIdCoursPour() {
        return idCoursPour;
    }

    public void setIdCoursPour(Integer idCoursPour) {
        this.idCoursPour = idCoursPour;
    }

    public Integer getIdCoursDonne() {
        return idCoursDonne;
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Devoir devoir = (Devoir) o;
        if (id != -1 && id == devoir.id) return true;
        return Objects.equals(datePour, devoir.datePour) && Objects.equals(description, devoir.description) && Objects.equals(discordMessageId, devoir.discordMessageId) && Objects.equals(idEnseignant, devoir.idEnseignant) && Objects.equals(codeEnseignement, devoir.codeEnseignement) && Objects.equals(idCoursPour, devoir.idCoursPour) && Objects.equals(idCoursDonne, devoir.idCoursDonne);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, datePour, description, discordMessageId, idEnseignant, codeEnseignement, idCoursPour, idCoursDonne);
    }
}

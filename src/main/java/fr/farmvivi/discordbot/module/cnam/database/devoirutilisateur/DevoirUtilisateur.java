package fr.farmvivi.discordbot.module.cnam.database.devoirutilisateur;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Objects;

public class DevoirUtilisateur {
    private final int idDevoir;
    private final int idUtilisateur;
    private LocalDateTime dateFait;

    public DevoirUtilisateur(int idDevoir, int idUtilisateur, LocalDateTime dateFait) {
        this.idDevoir = idDevoir;
        this.idUtilisateur = idUtilisateur;
        this.dateFait = dateFait;
    }

    public int getIdDevoir() {
        return idDevoir;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public LocalDateTime getDateFait() {
        return dateFait;
    }

    public void setDateFait(LocalDateTime dateFait) {
        this.dateFait = dateFait;
    }

    @Override
    public String toString() {
        return "fait le " + dateFait.format(new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DevoirUtilisateur devoirUtilisateur = (DevoirUtilisateur) o;
        return idDevoir == devoirUtilisateur.idDevoir && idUtilisateur == devoirUtilisateur.idUtilisateur && Objects.equals(dateFait, devoirUtilisateur.dateFait);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDevoir, idUtilisateur, dateFait);
    }
}

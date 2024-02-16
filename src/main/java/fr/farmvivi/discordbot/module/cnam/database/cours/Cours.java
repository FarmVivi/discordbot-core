package fr.farmvivi.discordbot.module.cnam.database.cours;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Objects;

public class Cours implements Comparable<Cours> {
    private final int id;
    private LocalDateTime debutCours;
    private LocalDateTime finCours;
    private boolean presentiel;
    private int enseignantId;
    private int salleId;
    private String enseignementCode;

    public Cours(LocalDateTime debutCours, LocalDateTime finCours, boolean presentiel, int enseignantId, int salleId, String enseignementCode) {
        this(-1, debutCours, finCours, presentiel, enseignantId, salleId, enseignementCode);
    }

    public Cours(int id, LocalDateTime debutCours, LocalDateTime finCours, boolean presentiel, int enseignantId, int salleId, String enseignementCode) {
        this.id = id;
        this.debutCours = debutCours;
        this.finCours = finCours;
        this.presentiel = presentiel;
        this.enseignantId = enseignantId;
        this.salleId = salleId;
        this.enseignementCode = enseignementCode;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getDebutCours() {
        return debutCours;
    }

    public void setDebutCours(LocalDateTime debutCours) {
        this.debutCours = debutCours;
    }

    public LocalDateTime getFinCours() {
        return finCours;
    }

    public void setFinCours(LocalDateTime finCours) {
        this.finCours = finCours;
    }

    public boolean isPresentiel() {
        return presentiel;
    }

    public void setPresentiel(boolean presentiel) {
        this.presentiel = presentiel;
    }

    public int getEnseignantId() {
        return enseignantId;
    }

    public void setEnseignantId(int enseignantId) {
        this.enseignantId = enseignantId;
    }

    public int getSalleId() {
        return salleId;
    }

    public void setSalleId(int salleId) {
        this.salleId = salleId;
    }

    public String getEnseignementCode() {
        return enseignementCode;
    }

    public void setEnseignementCode(String enseignementCode) {
        this.enseignementCode = enseignementCode;
    }

    @Override
    public int compareTo(@NotNull Cours o) {
        // Comparaison par début de cours
        return debutCours.compareTo(o.debutCours);
    }

    @Override
    public String toString() {
        return debutCours.format(new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter()) + " de " + debutCours.format(new DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter()) + " à " + finCours.format(new DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cours cours = (Cours) o;
        if (id != -1 && id == cours.id) return true;
        return presentiel == cours.presentiel && enseignantId == cours.enseignantId && salleId == cours.salleId && Objects.equals(debutCours, cours.debutCours) && Objects.equals(finCours, cours.finCours) && Objects.equals(enseignementCode, cours.enseignementCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, debutCours, finCours, presentiel, enseignantId, salleId, enseignementCode);
    }
}

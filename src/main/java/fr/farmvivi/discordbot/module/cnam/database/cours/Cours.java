package fr.farmvivi.discordbot.module.cnam.database.cours;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Objects;

public class Cours implements Comparable<Cours> {
    private final int id;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private boolean presentiel;
    private int enseignantId;
    private int salleId;
    private String enseignementCode;

    public Cours(LocalDate date, LocalTime heureDebut, LocalTime heureFin, boolean presentiel, int enseignantId, int salleId, String enseignementCode) {
        this(-1, date, heureDebut, heureFin, presentiel, enseignantId, salleId, enseignementCode);
    }

    public Cours(int id, LocalDate date, LocalTime heureDebut, LocalTime heureFin, boolean presentiel, int enseignantId, int salleId, String enseignementCode) {
        this.id = id;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.presentiel = presentiel;
        this.enseignantId = enseignantId;
        this.salleId = salleId;
        this.enseignementCode = enseignementCode;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
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
        // Comparaison par date puis par heure de début
        int dateCompare = date.compareTo(o.date);
        if (dateCompare == 0) {
            return heureDebut.compareTo(o.heureDebut);
        }
        return dateCompare;
    }

    @Override
    public String toString() {
        return date.format(new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter()) + " de " + heureDebut.format(new DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter()) + " à " + heureFin.format(new DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cours cours = (Cours) o;
        if (id != -1 && id == cours.id) return true;
        return presentiel == cours.presentiel && enseignantId == cours.enseignantId && salleId == cours.salleId && Objects.equals(date, cours.date) && Objects.equals(heureDebut, cours.heureDebut) && Objects.equals(heureFin, cours.heureFin) && Objects.equals(enseignementCode, cours.enseignementCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, heureDebut, heureFin, presentiel, enseignantId, salleId, enseignementCode);
    }
}

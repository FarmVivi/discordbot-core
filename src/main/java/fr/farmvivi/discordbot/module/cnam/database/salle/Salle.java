package fr.farmvivi.discordbot.module.cnam.database.salle;

import java.util.Objects;

public class Salle {
    private final int id;
    private final String nom;
    private final String adresse;

    public Salle(String nom, String adresse) {
        this(-1, nom, adresse);
    }

    public Salle(int id, String nom, String adresse) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getAdresse() {
        return adresse;
    }

    @Override
    public String toString() {
        return nom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Salle that = (Salle) o;
        if (id == that.id) return true;
        return Objects.equals(nom, that.nom) && Objects.equals(adresse, that.adresse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, adresse);
    }
}

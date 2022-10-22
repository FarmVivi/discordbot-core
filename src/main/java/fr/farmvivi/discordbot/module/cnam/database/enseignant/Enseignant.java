package fr.farmvivi.discordbot.module.cnam.database.enseignant;

import java.util.Objects;

public class Enseignant {
    private final int id;
    private final String nom;
    private final String prenom;

    public Enseignant(String nom, String prenom) {
        this(-1, nom, prenom);
    }

    public Enseignant(int id, String nom, String prenom) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    @Override
    public String toString() {
        return nom + " " + prenom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enseignant that = (Enseignant) o;
        if (id == that.id) return true;
        return Objects.equals(nom, that.nom) && Objects.equals(prenom, that.prenom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, prenom);
    }
}

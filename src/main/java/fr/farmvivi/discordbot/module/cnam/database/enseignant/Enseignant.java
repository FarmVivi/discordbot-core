package fr.farmvivi.discordbot.module.cnam.database.enseignant;

import java.util.Objects;

public class Enseignant {
    private final int id;
    private String nom;
    private String prenom;

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

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    @Override
    public String toString() {
        return nom + " " + prenom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enseignant enseignant = (Enseignant) o;
        if (id != -1 && id == enseignant.id) return true;
        return Objects.equals(nom, enseignant.nom) && Objects.equals(prenom, enseignant.prenom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, prenom);
    }
}

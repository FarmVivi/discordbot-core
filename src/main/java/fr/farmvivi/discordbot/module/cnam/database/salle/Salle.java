package fr.farmvivi.discordbot.module.cnam.database.salle;

import java.util.Objects;

public class Salle {
    private final int id;
    private String nom;
    private String adresse;

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

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    @Override
    public String toString() {
        return nom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Salle salle = (Salle) o;
        if (id != -1 && id == salle.id) return true;
        return Objects.equals(nom, salle.nom) && Objects.equals(adresse, salle.adresse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, adresse);
    }
}

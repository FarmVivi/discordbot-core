package fr.farmvivi.discordbot.module.cnam.database.enseignement;

import java.util.Objects;

public class Enseignement {
    private final String code;
    private String nom;

    public Enseignement(String code, String nom) {
        this.code = code;
        this.nom = nom;
    }

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return nom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enseignement enseignement = (Enseignement) o;
        if (!code.isBlank() && code.equals(enseignement.code)) return true;
        return Objects.equals(nom, enseignement.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, nom);
    }
}

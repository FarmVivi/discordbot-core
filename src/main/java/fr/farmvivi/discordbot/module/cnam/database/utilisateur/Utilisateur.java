package fr.farmvivi.discordbot.module.cnam.database.utilisateur;

import java.util.Objects;

public class Utilisateur {
    private final int id;
    private String codeScolarite;
    private Long discordId;

    public Utilisateur() {
        this(null, null);
    }

    public Utilisateur(String codeScolarite, Long discordId) {
        this(-1, codeScolarite, discordId);
    }

    public Utilisateur(int id, String codeScolarite, Long discordId) {
        this.id = id;
        this.codeScolarite = codeScolarite;
        this.discordId = discordId;
    }

    public int getId() {
        return id;
    }

    public String getCodeScolarite() {
        return codeScolarite;
    }

    public void setCodeScolarite(String codeScolarite) {
        this.codeScolarite = codeScolarite;
    }

    public Long getDiscordId() {
        return discordId;
    }

    public void setDiscordId(Long discordId) {
        this.discordId = discordId;
    }

    @Override
    public String toString() {
        return id + "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Utilisateur utilisateur = (Utilisateur) o;
        if (id != -1 && id == utilisateur.id) return true;
        return Objects.equals(discordId, utilisateur.discordId) && Objects.equals(codeScolarite, utilisateur.codeScolarite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, codeScolarite, discordId);
    }
}

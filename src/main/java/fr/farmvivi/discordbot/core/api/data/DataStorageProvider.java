package fr.farmvivi.discordbot.core.api.data;

/**
 * Interface pour le fournisseur de stockage de données.
 * Permet d'obtenir les différents types de stockage disponibles.
 */
public interface DataStorageProvider {
    /**
     * Récupère le stockage de données par utilisateur.
     *
     * @return le stockage de données par utilisateur
     */
    UserStorage getUserStorage();

    /**
     * Récupère le stockage de données par guild.
     *
     * @return le stockage de données par guild
     */
    GuildStorage getGuildStorage();

    /**
     * Récupère le stockage de données par utilisateur par guild.
     *
     * @return le stockage de données par utilisateur par guild
     */
    UserGuildStorage getUserGuildStorage();

    /**
     * Récupère le type de stockage utilisé.
     *
     * @return le type de stockage
     */
    StorageType getStorageType();

    /**
     * Sauvegarde toutes les données en attente.
     *
     * @return true si l'opération a réussi, false sinon
     */
    boolean saveAll();

    /**
     * Ferme le fournisseur de stockage et libère les ressources.
     *
     * @return true si l'opération a réussi, false sinon
     */
    boolean close();

    /**
     * Type de stockage disponible.
     */
    enum StorageType {
        FILE, DB
    }
}
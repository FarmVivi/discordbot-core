package fr.farmvivi.discordbot.core.api.data;

/**
 * Interface pour le stockage de données par utilisateur par guild.
 */
public interface UserGuildStorage extends DataStorage<String> {
    /**
     * Récupère une valeur pour un utilisateur dans une guild donnée.
     *
     * @param userId  l'ID de l'utilisateur
     * @param guildId l'ID de la guild
     * @param key     la clé de la donnée
     * @param type    le type de la valeur
     * @param <T>     le type de retour
     * @return la valeur, wrapped dans un Optional
     */
    <T> java.util.Optional<T> getUserGuildData(String userId, String guildId, String key, Class<T> type);

    /**
     * Enregistre une valeur pour un utilisateur dans une guild donnée.
     *
     * @param userId  l'ID de l'utilisateur
     * @param guildId l'ID de la guild
     * @param key     la clé de la donnée
     * @param value   la valeur à enregistrer
     * @param <T>     le type de la valeur
     * @return true si l'opération a réussi, false sinon
     */
    <T> boolean setUserGuildData(String userId, String guildId, String key, T value);

    /**
     * Supprime une donnée pour un utilisateur dans une guild donnée.
     *
     * @param userId  l'ID de l'utilisateur
     * @param guildId l'ID de la guild
     * @param key     la clé de la donnée
     * @return true si la donnée a été supprimée, false sinon
     */
    boolean removeUserGuildData(String userId, String guildId, String key);

    /**
     * Vérifie si une donnée existe pour un utilisateur dans une guild donnée.
     *
     * @param userId  l'ID de l'utilisateur
     * @param guildId l'ID de la guild
     * @param key     la clé de la donnée
     * @return true si la donnée existe, false sinon
     */
    boolean hasUserGuildData(String userId, String guildId, String key);

    /**
     * Récupère toutes les clés pour un utilisateur dans une guild donnée.
     *
     * @param userId  l'ID de l'utilisateur
     * @param guildId l'ID de la guild
     * @return un ensemble de toutes les clés
     */
    java.util.Set<String> getUserGuildKeys(String userId, String guildId);

    /**
     * Efface toutes les données d'un utilisateur dans une guild.
     *
     * @param userId  l'ID de l'utilisateur
     * @param guildId l'ID de la guild
     * @return true si l'opération a réussi, false sinon
     */
    boolean clearUserGuild(String userId, String guildId);
}
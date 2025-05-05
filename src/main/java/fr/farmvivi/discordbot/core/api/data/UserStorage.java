package fr.farmvivi.discordbot.core.api.data;

/**
 * Interface pour le stockage de données par utilisateur.
 */
public interface UserStorage extends DataStorage<String> {
    /**
     * Récupère une valeur pour un utilisateur donné.
     *
     * @param userId l'ID de l'utilisateur
     * @param key    la clé de la donnée
     * @param type   le type de la valeur
     * @param <T>    le type de retour
     * @return la valeur, wrapped dans un Optional
     */
    <T> java.util.Optional<T> getUserData(String userId, String key, Class<T> type);

    /**
     * Enregistre une valeur pour un utilisateur donné.
     *
     * @param userId l'ID de l'utilisateur
     * @param key    la clé de la donnée
     * @param value  la valeur à enregistrer
     * @param <T>    le type de la valeur
     * @return true si l'opération a réussi, false sinon
     */
    <T> boolean setUserData(String userId, String key, T value);

    /**
     * Supprime une donnée pour un utilisateur donné.
     *
     * @param userId l'ID de l'utilisateur
     * @param key    la clé de la donnée
     * @return true si la donnée a été supprimée, false sinon
     */
    boolean removeUserData(String userId, String key);

    /**
     * Vérifie si une donnée existe pour un utilisateur donné.
     *
     * @param userId l'ID de l'utilisateur
     * @param key    la clé de la donnée
     * @return true si la donnée existe, false sinon
     */
    boolean hasUserData(String userId, String key);

    /**
     * Récupère toutes les clés pour un utilisateur donné.
     *
     * @param userId l'ID de l'utilisateur
     * @return un ensemble de toutes les clés
     */
    java.util.Set<String> getUserKeys(String userId);

    /**
     * Efface toutes les données d'un utilisateur.
     *
     * @param userId l'ID de l'utilisateur
     * @return true si l'opération a réussi, false sinon
     */
    boolean clearUser(String userId);
}
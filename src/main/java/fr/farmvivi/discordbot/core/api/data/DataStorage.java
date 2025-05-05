package fr.farmvivi.discordbot.core.api.data;

import java.util.Optional;
import java.util.Set;

/**
 * Interface générique pour les opérations de stockage de données.
 */
public interface DataStorage<K> {
    /**
     * Vérifie si une clé existe dans le stockage.
     *
     * @param key la clé à vérifier
     * @return true si la clé existe, false sinon
     */
    boolean exists(K key);

    /**
     * Récupère une valeur pour une clé donnée.
     *
     * @param key la clé
     * @param <T> le type de la valeur
     * @return la valeur, wrapped dans un Optional
     */
    <T> Optional<T> get(K key, Class<T> type);

    /**
     * Enregistre une valeur pour une clé donnée.
     *
     * @param key   la clé
     * @param value la valeur
     * @param <T>   le type de la valeur
     * @return true si l'opération a réussi, false sinon
     */
    <T> boolean set(K key, T value);

    /**
     * Supprime une clé du stockage.
     *
     * @param key la clé à supprimer
     * @return true si la clé a été supprimée, false sinon
     */
    boolean remove(K key);

    /**
     * Récupère toutes les clés stockées.
     *
     * @return un ensemble de toutes les clés
     */
    Set<K> getKeys();

    /**
     * Efface toutes les données du stockage.
     *
     * @return true si l'opération a réussi, false sinon
     */
    boolean clear();

    /**
     * Sauvegarde les modifications en attente.
     * Cette méthode peut ne rien faire pour les stockages à écriture immédiate.
     *
     * @return true si l'opération a réussi, false sinon
     */
    boolean save();
}
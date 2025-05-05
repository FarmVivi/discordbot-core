package fr.farmvivi.discordbot.core.api.data;

/**
 * Interface pour le stockage de données par guild.
 */
public interface GuildStorage extends DataStorage<String> {
    /**
     * Récupère une valeur pour une guild donnée.
     *
     * @param guildId l'ID de la guild
     * @param key     la clé de la donnée
     * @param type    le type de la valeur
     * @param <T>     le type de retour
     * @return la valeur, wrapped dans un Optional
     */
    <T> java.util.Optional<T> getGuildData(String guildId, String key, Class<T> type);

    /**
     * Enregistre une valeur pour une guild donnée.
     *
     * @param guildId l'ID de la guild
     * @param key     la clé de la donnée
     * @param value   la valeur à enregistrer
     * @param <T>     le type de la valeur
     * @return true si l'opération a réussi, false sinon
     */
    <T> boolean setGuildData(String guildId, String key, T value);

    /**
     * Supprime une donnée pour une guild donnée.
     *
     * @param guildId l'ID de la guild
     * @param key     la clé de la donnée
     * @return true si la donnée a été supprimée, false sinon
     */
    boolean removeGuildData(String guildId, String key);

    /**
     * Vérifie si une donnée existe pour une guild donnée.
     *
     * @param guildId l'ID de la guild
     * @param key     la clé de la donnée
     * @return true si la donnée existe, false sinon
     */
    boolean hasGuildData(String guildId, String key);

    /**
     * Récupère toutes les clés pour une guild donnée.
     *
     * @param guildId l'ID de la guild
     * @return un ensemble de toutes les clés
     */
    java.util.Set<String> getGuildKeys(String guildId);

    /**
     * Efface toutes les données d'une guild.
     *
     * @param guildId l'ID de la guild
     * @return true si l'opération a réussi, false sinon
     */
    boolean clearGuild(String guildId);
}
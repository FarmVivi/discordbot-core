package fr.farmvivi.discordbot.core.data;

import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.api.data.DataStorageProvider;
import fr.farmvivi.discordbot.core.data.db.DatabaseStorageProvider;
import fr.farmvivi.discordbot.core.data.file.FileDataStorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Factory pour créer des fournisseurs de stockage de données.
 */
public class DataStorageFactory {
    private static final Logger logger = LoggerFactory.getLogger(DataStorageFactory.class);
    private static final String DEFAULT_DATA_FOLDER = "data";

    /**
     * Crée un fournisseur de stockage de données en fonction de la configuration.
     *
     * @param config      la configuration
     * @param defaultType le type de stockage par défaut
     * @return le fournisseur de stockage créé
     */
    public static DataStorageProvider createStorageProvider(
            Configuration config,
            DataStorageProvider.StorageType defaultType
    ) {
        DataStorageProvider.StorageType storageType = determineStorageType(config, defaultType);

        try {
            return switch (storageType) {
                case FILE -> createFileDataStorageProvider(config);
                case DB -> createDatabaseStorageProvider(config);
            };
        } catch (Exception e) {
            logger.error("Échec de création du fournisseur de stockage {}: {}", storageType, e.getMessage(), e);
            logger.warn("Retour au stockage de type FILE");
            return createFileDataStorageProvider(config);
        }
    }

    /**
     * Détermine le type de stockage à utiliser basé sur la configuration.
     *
     * @param config      la configuration
     * @param defaultType le type de stockage par défaut
     * @return le type de stockage à utiliser
     */
    private static DataStorageProvider.StorageType determineStorageType(
            Configuration config,
            DataStorageProvider.StorageType defaultType
    ) {
        try {
            String configType = config.getString("data.storage.type");
            try {
                return DataStorageProvider.StorageType.valueOf(configType.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Type de stockage invalide: {}, utilisation de la valeur par défaut: {}",
                        configType, defaultType);
                return defaultType;
            }
        } catch (ConfigurationException e) {
            logger.info("Type de stockage non configuré, utilisation de la valeur par défaut: {}", defaultType);
            return defaultType;
        }
    }

    /**
     * Crée un fournisseur de stockage basé sur des fichiers.
     *
     * @param config la configuration
     * @return le fournisseur de stockage fichier
     */
    private static FileDataStorageProvider createFileDataStorageProvider(Configuration config) {
        String dataFolderPath = DEFAULT_DATA_FOLDER;

        try {
            dataFolderPath = config.getString("data.storage.file.folder", DEFAULT_DATA_FOLDER);
        } catch (Exception e) {
            logger.debug("Utilisation du dossier de données par défaut: {}", DEFAULT_DATA_FOLDER);
        }

        File dataFolder = new File(dataFolderPath);

        // Création du dossier de stockage s'il n'existe pas
        if (!dataFolder.exists()) {
            if (dataFolder.mkdirs()) {
                logger.info("Dossier de données créé: {}", dataFolder.getAbsolutePath());
            } else {
                logger.warn("Échec de création du dossier de données: {}", dataFolder.getAbsolutePath());
            }
        }

        File storageFolder = new File(dataFolder, "storage");
        if (!storageFolder.exists() && !storageFolder.mkdirs()) {
            logger.warn("Échec de création du dossier de stockage: {}", storageFolder.getAbsolutePath());
        }

        return new FileDataStorageProvider(storageFolder);
    }

    /**
     * Crée un fournisseur de stockage basé sur une base de données.
     *
     * @param config la configuration
     * @return le fournisseur de stockage base de données
     */
    private static DatabaseStorageProvider createDatabaseStorageProvider(Configuration config) throws ConfigurationException {
        validateDatabaseConfig(config);

        // Chargement du driver de base de données
        String url = config.getString("data.storage.db.url");
        loadDatabaseDriver(url);

        return new DatabaseStorageProvider(config);
    }

    /**
     * Valide que tous les paramètres de configuration de base de données requis sont présents.
     *
     * @param config la configuration
     * @throws ConfigurationException si un paramètre requis est manquant
     */
    private static void validateDatabaseConfig(Configuration config) throws ConfigurationException {
        // Vérification des paramètres de base de données requis
        String url = config.getString("data.storage.db.url");
        String username = config.getString("data.storage.db.username");
        String password = config.getString("data.storage.db.password");

        if (url == null || url.isEmpty()) {
            throw new ConfigurationException("L'URL de la base de données est requise");
        }

        // Vérification que l'URL est une URL JDBC valide
        if (!url.startsWith("jdbc:")) {
            throw new ConfigurationException("URL de base de données invalide: " + url);
        }

        logger.info("Configuration de la base de données validée avec succès");
    }

    /**
     * Charge le driver de base de données approprié basé sur l'URL JDBC.
     *
     * @param jdbcUrl l'URL JDBC
     */
    private static void loadDatabaseDriver(String jdbcUrl) {
        try {
            if (jdbcUrl.startsWith("jdbc:mysql:") || jdbcUrl.startsWith("jdbc:mariadb:")) {
                Class.forName("org.mariadb.jdbc.Driver");
                logger.debug("Driver JDBC MariaDB chargé");
            } else {
                logger.warn("Type de base de données inconnu dans l'URL: {}", jdbcUrl);
            }
        } catch (ClassNotFoundException e) {
            logger.warn("Échec de chargement du driver de base de données: {}", e.getMessage());
            // On continue quand même, car les drivers JDBC modernes se chargent généralement automatiquement
        }
    }
}
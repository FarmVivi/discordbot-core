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
 * Fabrique pour créer des fournisseurs de stockage de données.
 */
public class DataStorageFactory {
    private static final Logger logger = LoggerFactory.getLogger(DataStorageFactory.class);

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
        DataStorageProvider.StorageType storageType = defaultType;

        try {
            String configType = config.getString("data.storage.type");
            try {
                storageType = DataStorageProvider.StorageType.valueOf(configType.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid storage type: {}, using default: {}", configType, defaultType);
            }
        } catch (ConfigurationException e) {
            logger.info("Storage type not configured, using default: {}", defaultType);
        }

        switch (storageType) {
            case FILE:
                File dataFolder = new File("data");

                // Création du dossier de stockage s'il n'existe pas
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                    logger.info("Created data folder: {}", dataFolder.getAbsolutePath());
                }

                return createFileDataStorageProvider(dataFolder);
            case DB:
                return createDatabaseStorageProvider(config);
            default:
                logger.warn("Unknown storage type: {}, falling back to FILE", storageType);

                File dataFolder2 = new File("data");

                // Création du dossier de stockage s'il n'existe pas
                if (!dataFolder2.exists()) {
                    dataFolder2.mkdirs();
                    logger.info("Created data folder: {}", dataFolder2.getAbsolutePath());
                }

                return createFileDataStorageProvider(dataFolder2);
        }
    }

    private static FileDataStorageProvider createFileDataStorageProvider(File dataFolder) {
        File storageFolder = new File(dataFolder, "storage");
        storageFolder.mkdirs();

        return new FileDataStorageProvider(storageFolder);
    }

    private static DatabaseStorageProvider createDatabaseStorageProvider(
            Configuration config
    ) {
        // Note: implémentation à compléter
        // return new RemoteDatabaseStorageProvider(config);
        logger.warn("Remote database storage not implemented yet, falling back to file storage");
        return createFileDataStorageProvider(new File("data"));
    }
}
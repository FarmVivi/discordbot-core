package fr.farmvivi.discordbot.core.data.file;

import fr.farmvivi.discordbot.core.api.data.DataStorageProvider;
import fr.farmvivi.discordbot.core.api.data.GuildStorage;
import fr.farmvivi.discordbot.core.api.data.UserGuildStorage;
import fr.farmvivi.discordbot.core.api.data.UserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Implémentation du fournisseur de stockage basée sur des fichiers.
 */
public class FileDataStorageProvider implements DataStorageProvider {
    private static final Logger logger = LoggerFactory.getLogger(FileDataStorageProvider.class);

    private final File dataFolder;
    private final FileDataUserStorage userStorage;
    private final FileDataGuildStorage guildStorage;
    private final FileDataUserGuildStorage userGuildStorage;

    /**
     * Crée un nouveau fournisseur de stockage basé sur des fichiers.
     *
     * @param dataFolder le dossier de base pour stocker les données
     */
    public FileDataStorageProvider(File dataFolder) {
        this.dataFolder = dataFolder;

        // Création des dossiers nécessaires
        File usersFolder = new File(dataFolder, "users");
        File guildsFolder = new File(dataFolder, "guilds");
        File userGuildsFolder = new File(dataFolder, "user_guilds");

        usersFolder.mkdirs();
        guildsFolder.mkdirs();
        userGuildsFolder.mkdirs();

        // Initialisation des stockages
        this.userStorage = new FileDataUserStorage(usersFolder);
        this.guildStorage = new FileDataGuildStorage(guildsFolder);
        this.userGuildStorage = new FileDataUserGuildStorage(userGuildsFolder);

        logger.info("Initialized file-based data storage in {}", dataFolder.getAbsolutePath());
    }

    @Override
    public UserStorage getUserStorage() {
        return userStorage;
    }

    @Override
    public GuildStorage getGuildStorage() {
        return guildStorage;
    }

    @Override
    public UserGuildStorage getUserGuildStorage() {
        return userGuildStorage;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.FILE;
    }

    @Override
    public boolean saveAll() {
        boolean success = true;

        if (!userStorage.save()) {
            success = false;
            logger.error("Failed to save user storage");
        }

        if (!guildStorage.save()) {
            success = false;
            logger.error("Failed to save guild storage");
        }

        if (!userGuildStorage.save()) {
            success = false;
            logger.error("Failed to save user-guild storage");
        }

        return success;
    }

    @Override
    public boolean close() {
        return saveAll();
    }
}
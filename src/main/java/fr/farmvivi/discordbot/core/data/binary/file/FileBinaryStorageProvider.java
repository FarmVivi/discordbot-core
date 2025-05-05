package fr.farmvivi.discordbot.core.data.binary.file;

import fr.farmvivi.discordbot.core.api.data.binary.BinaryStorage;
import fr.farmvivi.discordbot.core.api.data.binary.BinaryStorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of BinaryStorageProvider that uses the local file system.
 */
public class FileBinaryStorageProvider implements BinaryStorageProvider {
    private static final Logger logger = LoggerFactory.getLogger(FileBinaryStorageProvider.class);

    private final File baseDirectory;
    private final FileBinaryStorage globalStorage;
    private final Map<String, FileBinaryStorage> userStorages = new HashMap<>();
    private final Map<String, FileBinaryStorage> guildStorages = new HashMap<>();
    private final Map<String, Map<String, FileBinaryStorage>> userGuildStorages = new HashMap<>();

    /**
     * Creates a new file binary storage provider with the specified base directory.
     *
     * @param baseDirectory the base directory for storing files
     */
    public FileBinaryStorageProvider(File baseDirectory) {
        this.baseDirectory = baseDirectory;

        if (!baseDirectory.exists()) {
            if (!baseDirectory.mkdirs()) {
                logger.error("Failed to create base directory: {}", baseDirectory.getAbsolutePath());
            }
        }

        // Initialize global storage
        File globalDir = new File(baseDirectory, "global");
        if (!globalDir.exists()) {
            if (!globalDir.mkdirs()) {
                logger.error("Failed to create global directory: {}", globalDir.getAbsolutePath());
            }
        }

        this.globalStorage = new FileBinaryStorage("global", globalDir);
        logger.info("Initialized file-based binary storage in {}", baseDirectory.getAbsolutePath());
    }

    @Override
    public BinaryStorage getGlobalStorage() {
        return globalStorage;
    }

    @Override
    public BinaryStorage getUserStorage(String userId) {
        return userStorages.computeIfAbsent(userId, id -> {
            File userDir = new File(baseDirectory, "users/" + id);
            if (!userDir.exists()) {
                if (!userDir.mkdirs()) {
                    logger.error("Failed to create user directory: {}", userDir.getAbsolutePath());
                }
            }
            return new FileBinaryStorage("user-" + id, userDir);
        });
    }

    @Override
    public BinaryStorage getGuildStorage(String guildId) {
        return guildStorages.computeIfAbsent(guildId, id -> {
            File guildDir = new File(baseDirectory, "guilds/" + id);
            if (!guildDir.exists()) {
                if (!guildDir.mkdirs()) {
                    logger.error("Failed to create guild directory: {}", guildDir.getAbsolutePath());
                }
            }
            return new FileBinaryStorage("guild-" + id, guildDir);
        });
    }

    @Override
    public BinaryStorage getUserGuildStorage(String userId, String guildId) {
        Map<String, FileBinaryStorage> guildMap = userGuildStorages.computeIfAbsent(userId, id -> new HashMap<>());

        return guildMap.computeIfAbsent(guildId, id -> {
            File userGuildDir = new File(baseDirectory, "user_guilds/" + userId + "/" + guildId);
            if (!userGuildDir.exists()) {
                if (!userGuildDir.mkdirs()) {
                    logger.error("Failed to create user-guild directory: {}", userGuildDir.getAbsolutePath());
                }
            }
            return new FileBinaryStorage("user-" + userId + "-guild-" + guildId, userGuildDir);
        });
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.FILE;
    }

    @Override
    public boolean close() {
        // No resources to release for file-based storage
        return true;
    }
}
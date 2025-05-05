package fr.farmvivi.discordbot.core.data.binary.s3;

import fr.farmvivi.discordbot.core.api.data.binary.BinaryStorage;
import fr.farmvivi.discordbot.core.api.data.binary.BinaryStorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of BinaryStorageProvider that uses Amazon S3 or compatible storage services.
 */
public class S3BinaryStorageProvider implements BinaryStorageProvider {
    private static final Logger logger = LoggerFactory.getLogger(S3BinaryStorageProvider.class);

    private final String bucketName;
    private final String prefix;
    private final String endpoint;
    private final String region;
    private final String accessKey;
    private final String secretKey;

    private final S3BinaryStorage globalStorage;
    private final Map<String, S3BinaryStorage> userStorages = new HashMap<>();
    private final Map<String, S3BinaryStorage> guildStorages = new HashMap<>();
    private final Map<String, Map<String, S3BinaryStorage>> userGuildStorages = new HashMap<>();

    /**
     * Creates a new S3 binary storage provider with the specified configuration.
     *
     * @param bucketName the S3 bucket name
     * @param prefix     the prefix for all objects in the bucket (optional)
     * @param endpoint   the S3 endpoint URL (optional, null for AWS)
     * @param region     the AWS region
     * @param accessKey  the AWS access key
     * @param secretKey  the AWS secret key
     */
    public S3BinaryStorageProvider(String bucketName, String prefix, String endpoint,
                                   String region, String accessKey, String secretKey) {
        this.bucketName = bucketName;
        this.prefix = prefix != null ? prefix : "";
        this.endpoint = endpoint;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;

        // Initialize global storage
        this.globalStorage = new S3BinaryStorage("global", bucketName,
                getStoragePath("global"), endpoint, region, accessKey, secretKey);

        logger.info("Initialized S3-based binary storage in bucket {} with prefix {}", bucketName, this.prefix);
    }

    @Override
    public BinaryStorage getGlobalStorage() {
        return globalStorage;
    }

    @Override
    public BinaryStorage getUserStorage(String userId) {
        return userStorages.computeIfAbsent(userId, id ->
                new S3BinaryStorage("user-" + id, bucketName, getStoragePath("users/" + id),
                        endpoint, region, accessKey, secretKey));
    }

    @Override
    public BinaryStorage getGuildStorage(String guildId) {
        return guildStorages.computeIfAbsent(guildId, id ->
                new S3BinaryStorage("guild-" + id, bucketName, getStoragePath("guilds/" + id),
                        endpoint, region, accessKey, secretKey));
    }

    @Override
    public BinaryStorage getUserGuildStorage(String userId, String guildId) {
        Map<String, S3BinaryStorage> guildMap = userGuildStorages.computeIfAbsent(userId, id -> new HashMap<>());

        return guildMap.computeIfAbsent(guildId, id ->
                new S3BinaryStorage("user-" + userId + "-guild-" + guildId, bucketName,
                        getStoragePath("user_guilds/" + userId + "/" + guildId),
                        endpoint, region, accessKey, secretKey));
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.S3;
    }

    @Override
    public boolean close() {
        // Close all storage instances to release resources
        try {
            globalStorage.close();

            for (S3BinaryStorage storage : userStorages.values()) {
                storage.close();
            }

            for (S3BinaryStorage storage : guildStorages.values()) {
                storage.close();
            }

            for (Map<String, S3BinaryStorage> guildMap : userGuildStorages.values()) {
                for (S3BinaryStorage storage : guildMap.values()) {
                    storage.close();
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("Error closing S3 binary storage provider: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Builds a complete S3 prefix path for a specific storage scope.
     *
     * @param scope the storage scope path
     * @return the full S3 prefix for the scope
     */
    private String getStoragePath(String scope) {
        if (prefix == null || prefix.isEmpty()) {
            return scope;
        }
        return prefix + "/" + scope;
    }
}
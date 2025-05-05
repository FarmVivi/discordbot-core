package fr.farmvivi.discordbot.core.data.binary;

import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.api.data.binary.BinaryStorageProvider;
import fr.farmvivi.discordbot.core.data.binary.file.FileBinaryStorageProvider;
import fr.farmvivi.discordbot.core.data.binary.s3.S3BinaryStorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Factory for creating binary storage providers.
 */
public class BinaryStorageFactory {
    private static final Logger logger = LoggerFactory.getLogger(BinaryStorageFactory.class);
    private static final String DEFAULT_BINARY_FOLDER = "binary";

    /**
     * Creates a binary storage provider based on the configuration.
     *
     * @param config      the configuration
     * @param defaultType the default storage type to use if not specified in the configuration
     * @return the binary storage provider
     */
    public static BinaryStorageProvider createBinaryStorageProvider(
            Configuration config,
            BinaryStorageProvider.StorageType defaultType
    ) {
        BinaryStorageProvider.StorageType storageType = determineStorageType(config, defaultType);

        try {
            return switch (storageType) {
                case FILE -> createFileBinaryStorageProvider(config);
                case S3 -> createS3BinaryStorageProvider(config);
            };
        } catch (Exception e) {
            logger.error("Failed to create binary storage provider of type {}: {}", storageType, e.getMessage(), e);
            logger.warn("Falling back to FILE binary storage provider");
            return createFileBinaryStorageProvider(config);
        }
    }

    /**
     * Determines the storage type to use based on the configuration.
     *
     * @param config      the configuration
     * @param defaultType the default storage type
     * @return the storage type to use
     */
    private static BinaryStorageProvider.StorageType determineStorageType(
            Configuration config,
            BinaryStorageProvider.StorageType defaultType
    ) {
        try {
            String configType = config.getString("data.binary.storage.type");
            try {
                return BinaryStorageProvider.StorageType.valueOf(configType.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid binary storage type: {}, using default: {}", configType, defaultType);
                return defaultType;
            }
        } catch (ConfigurationException e) {
            logger.info("Binary storage type not configured, using default: {}", defaultType);
            return defaultType;
        }
    }

    /**
     * Creates a file-based binary storage provider.
     *
     * @param config the configuration
     * @return the file binary storage provider
     */
    private static FileBinaryStorageProvider createFileBinaryStorageProvider(Configuration config) {
        String binaryFolderPath = DEFAULT_BINARY_FOLDER;

        try {
            binaryFolderPath = config.getString("data.binary.storage.file.folder", DEFAULT_BINARY_FOLDER);
        } catch (Exception e) {
            logger.debug("Using default binary folder: {}", DEFAULT_BINARY_FOLDER);
        }

        File binaryFolder = new File(binaryFolderPath);

        // Create the binary storage folder if it doesn't exist
        if (!binaryFolder.exists()) {
            if (binaryFolder.mkdirs()) {
                logger.info("Created binary folder: {}", binaryFolder.getAbsolutePath());
            } else {
                logger.warn("Failed to create binary folder: {}", binaryFolder.getAbsolutePath());
            }
        }

        return new FileBinaryStorageProvider(binaryFolder);
    }

    /**
     * Creates an S3-based binary storage provider.
     *
     * @param config the configuration
     * @return the S3 binary storage provider
     * @throws ConfigurationException if the configuration is invalid or incomplete
     */
    private static S3BinaryStorageProvider createS3BinaryStorageProvider(Configuration config) throws ConfigurationException {
        validateS3Config(config);

        // Get the S3 configuration
        String bucketName = config.getString("data.binary.storage.s3.bucket");
        String region = config.getString("data.binary.storage.s3.region");
        String accessKey = config.getString("data.binary.storage.s3.access_key");
        String secretKey = config.getString("data.binary.storage.s3.secret_key");
        String endpoint = config.getString("data.binary.storage.s3.endpoint", "");
        String prefix = config.getString("data.binary.storage.s3.prefix", "");

        return new S3BinaryStorageProvider(bucketName, prefix, endpoint, region, accessKey, secretKey);
    }

    /**
     * Validates that all required S3 configuration parameters are present.
     *
     * @param config the configuration
     * @throws ConfigurationException if any required parameter is missing
     */
    private static void validateS3Config(Configuration config) throws ConfigurationException {
        // Check required S3 parameters
        String bucketName = config.getString("data.binary.storage.s3.bucket");
        String region = config.getString("data.binary.storage.s3.region");
        String accessKey = config.getString("data.binary.storage.s3.access_key");
        String secretKey = config.getString("data.binary.storage.s3.secret_key");

        if (bucketName == null || bucketName.isEmpty()) {
            throw new ConfigurationException("S3 bucket name is required");
        }

        if (region == null || region.isEmpty()) {
            throw new ConfigurationException("S3 region is required");
        }

        if (accessKey == null || accessKey.isEmpty()) {
            throw new ConfigurationException("S3 access key is required");
        }

        if (secretKey == null || secretKey.isEmpty()) {
            throw new ConfigurationException("S3 secret key is required");
        }

        logger.info("S3 configuration validated successfully");
    }
}
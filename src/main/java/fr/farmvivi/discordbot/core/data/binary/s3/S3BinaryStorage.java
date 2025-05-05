package fr.farmvivi.discordbot.core.data.binary.s3;

import fr.farmvivi.discordbot.core.data.binary.AbstractBinaryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.*;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of BinaryStorage that uses Amazon S3 or compatible storage services.
 */
public class S3BinaryStorage extends AbstractBinaryStorage {
    private static final Logger logger = LoggerFactory.getLogger(S3BinaryStorage.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String prefix;

    /**
     * Creates a new S3 binary storage with the specified configuration.
     *
     * @param storageName the name of the storage
     * @param bucketName  the S3 bucket name
     * @param prefix      the prefix for all objects in the bucket
     * @param endpoint    the S3 endpoint URL (null for AWS)
     * @param region      the AWS region
     * @param accessKey   the AWS access key
     * @param secretKey   the AWS secret key
     */
    public S3BinaryStorage(String storageName, String bucketName, String prefix,
                           String endpoint, String region, String accessKey, String secretKey) {
        super(storageName);
        this.bucketName = bucketName;
        this.prefix = normalizePath(prefix);

        // Build S3 client
        S3ClientBuilder clientBuilder = S3Client.builder();
        S3Presigner.Builder presignerBuilder = S3Presigner.builder();

        // Set region
        Region awsRegion = Region.of(region);
        clientBuilder.region(awsRegion);
        presignerBuilder.region(awsRegion);

        // Set credentials
        if (accessKey != null && secretKey != null) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
            clientBuilder.credentialsProvider(credentialsProvider);
            presignerBuilder.credentialsProvider(credentialsProvider);
        }

        // Set custom endpoint for S3-compatible services
        if (endpoint != null && !endpoint.isEmpty()) {
            clientBuilder.endpointOverride(URI.create(endpoint));
            presignerBuilder.endpointOverride(URI.create(endpoint));
        }

        this.s3Client = clientBuilder.build();
        this.s3Presigner = presignerBuilder.build();

        // Ensure bucket exists
        ensureBucketExists();
    }

    /**
     * Ensures the S3 bucket exists, creating it if necessary.
     */
    private void ensureBucketExists() {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.headBucket(headBucketRequest);
        } catch (NoSuchBucketException e) {
            logger.info("[{}] Bucket {} does not exist, creating it", storageName, bucketName);
            try {
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build();
                s3Client.createBucket(createBucketRequest);
            } catch (Exception ex) {
                logger.error("[{}] Failed to create bucket {}: {}", storageName, bucketName, ex.getMessage());
            }
        } catch (Exception e) {
            logger.error("[{}] Error checking if bucket {} exists: {}", storageName, bucketName, e.getMessage());
        }
    }

    @Override
    public OutputStream getOutputStream(String path, boolean overwrite) {
        path = getFullPath(path);

        if (!overwrite && fileExists(path)) {
            logger.warn("[{}] File already exists at path {} and overwrite is false", storageName, path);
            return null;
        }

        // Since S3 doesn't provide a direct output stream, we'll use a pipe stream
        // to write locally first, then upload to S3 when the stream is closed
        PipedOutputStream outputStream = new PipedOutputStream();
        try {
            final PipedInputStream inputStream = new PipedInputStream(outputStream);
            final String finalPath = path;

            // Start a separate thread to handle the upload to S3
            new Thread(() -> {
                try {
                    // Upload the content to S3
                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(finalPath)
                            .contentType(getContentTypeFromPath(finalPath))
                            .build();

                    s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, -1));
                } catch (Exception e) {
                    logger.error("[{}] Error uploading to S3 for path {}: {}", storageName, finalPath, e.getMessage());
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        logger.error("[{}] Error closing input stream for path {}: {}", storageName, finalPath, e.getMessage());
                    }
                }
            }).start();

            return outputStream;
        } catch (IOException e) {
            logger.error("[{}] Error creating output stream for path {}: {}", storageName, path, e.getMessage());
            return null;
        }
    }

    @Override
    public InputStream getInputStream(String path) {
        path = getFullPath(path);

        try {
            // Check if the object exists first
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();

            try {
                s3Client.headObject(headObjectRequest);
            } catch (NoSuchKeyException e) {
                logger.error("[{}] File does not exist at path: {}", storageName, path);
                return null;
            }

            // Get the object
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            logger.error("[{}] Error getting input stream for path {}: {}", storageName, path, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean fileExists(String path) {
        path = getFullPath(path);

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            logger.error("[{}] Error checking if file exists at path {}: {}", storageName, path, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteFile(String path) {
        path = getFullPath(path);

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            return true;
        } catch (Exception e) {
            logger.error("[{}] Error deleting file at path {}: {}", storageName, path, e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> listFiles(String directory) {
        directory = getFullPath(directory);

        // Ensure directory ends with a slash for proper prefix filtering
        if (!directory.isEmpty() && !directory.endsWith("/")) {
            directory += "/";
        }

        List<String> files = new ArrayList<>();

        try {
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(directory)
                    .delimiter("/")
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsRequest);

            // Extract file names, removing the directory prefix
            for (S3Object s3Object : response.contents()) {
                String key = s3Object.key();

                // Skip the directory itself
                if (!key.equals(directory)) {
                    // Remove the prefix part to get the relative path
                    String relativePath = removePrefix(key);
                    files.add(relativePath);
                }
            }
        } catch (Exception e) {
            logger.error("[{}] Error listing files in directory {}: {}", storageName, directory, e.getMessage());
        }

        return files;
    }

    @Override
    public long getFileSize(String path) {
        path = getFullPath(path);

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.contentLength();
        } catch (NoSuchKeyException e) {
            return -1;
        } catch (Exception e) {
            logger.error("[{}] Error getting file size for path {}: {}", storageName, path, e.getMessage());
            return -1;
        }
    }

    @Override
    public long getLastModifiedTime(String path) {
        path = getFullPath(path);

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.lastModified().toEpochMilli();
        } catch (NoSuchKeyException e) {
            return -1;
        } catch (Exception e) {
            logger.error("[{}] Error getting last modified time for path {}: {}", storageName, path, e.getMessage());
            return -1;
        }
    }

    @Override
    public String getContentType(String path) {
        path = getFullPath(path);

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.contentType();
        } catch (NoSuchKeyException e) {
            return super.getContentType(path);
        } catch (Exception e) {
            logger.error("[{}] Error getting content type for path {}: {}", storageName, path, e.getMessage());
            return super.getContentType(path);
        }
    }

    @Override
    public boolean createDirectory(String path) {
        // S3 doesn't have real directories, but we can create an empty object with a trailing slash
        path = getFullPath(path);
        if (!path.endsWith("/")) {
            path += "/";
        }

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.empty());
            return true;
        } catch (Exception e) {
            logger.error("[{}] Error creating directory at path {}: {}", storageName, path, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isDirectory(String path) {
        path = getFullPath(path);
        if (!path.endsWith("/")) {
            path += "/";
        }

        try {
            // Check if there's an empty object with a trailing slash
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();

            try {
                s3Client.headObject(headObjectRequest);
                return true;
            } catch (NoSuchKeyException e) {
                // If the object doesn't exist, check if there are objects with this prefix
                ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .maxKeys(1)
                        .build();

                ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsRequest);
                return response.hasContents();
            }
        } catch (Exception e) {
            logger.error("[{}] Error checking if path {} is a directory: {}", storageName, path, e.getMessage());
            return false;
        }
    }

    @Override
    public String getPublicUrl(String path, int expireIn) {
        path = getFullPath(path);

        try {
            // Check if the object exists first
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();

            try {
                s3Client.headObject(headObjectRequest);
            } catch (NoSuchKeyException e) {
                logger.error("[{}] File does not exist at path: {}", storageName, path);
                return null;
            }

            // Create a presigned request for the object
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expireIn > 0 ? expireIn : 3600)) // Default to 1 hour
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(path)
                            .build())
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            logger.error("[{}] Error generating public URL for path {}: {}", storageName, path, e.getMessage());
            return null;
        }
    }

    /**
     * Combines the configured prefix with the given path to get the full S3 object key.
     *
     * @param path the path
     * @return the full S3 object key
     */
    private String getFullPath(String path) {
        path = normalizePath(path);
        if (prefix.isEmpty()) {
            return path;
        }

        return prefix + "/" + path;
    }

    /**
     * Removes the configured prefix from an S3 object key to get the relative path.
     *
     * @param key the S3 object key
     * @return the relative path
     */
    private String removePrefix(String key) {
        if (prefix.isEmpty()) {
            return key;
        }

        String prefixWithSlash = prefix + "/";
        if (key.startsWith(prefixWithSlash)) {
            return key.substring(prefixWithSlash.length());
        }

        return key;
    }

    /**
     * Closes the S3 client and releases resources.
     */
    public void close() {
        if (s3Client != null) {
            s3Client.close();
        }
        if (s3Presigner != null) {
            s3Presigner.close();
        }
    }
}
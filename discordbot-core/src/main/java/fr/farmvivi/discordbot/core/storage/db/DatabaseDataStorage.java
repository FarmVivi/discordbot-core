package fr.farmvivi.discordbot.core.storage.db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.storage.StorageKey;
import fr.farmvivi.discordbot.core.storage.AbstractDataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * Database implementation of DataStorage using HikariCP.
 * Stores data in a relational database with a simple key-value schema.
 */
public class DatabaseDataStorage extends AbstractDataStorage {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseDataStorage.class);
    private static final Gson gson = new GsonBuilder().create();

    private final HikariDataSource dataSource;

    /**
     * Creates a new database data storage.
     *
     * @param configuration the configuration for database connection
     * @param eventManager  the event manager
     */
    public DatabaseDataStorage(Configuration configuration, EventManager eventManager) {
        super("database", eventManager);
        this.dataSource = initializeDataSource(configuration);
        initializeSchema();
    }

    /**
     * Initializes the database connection using HikariCP.
     *
     * @param config the configuration
     * @return the HikariCP data source
     */
    private HikariDataSource initializeDataSource(Configuration config) {
        try {
            // Required settings
            String jdbcUrl = config.getString("data.storage.db.url");
            String username = config.getString("data.storage.db.username");
            String password = config.getString("data.storage.db.password");

            // Create and configure HikariCP
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(jdbcUrl);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);

            // Set optimal default values
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setIdleTimeout(30000);
            hikariConfig.setMaxLifetime(1800000);
            hikariConfig.setConnectionTimeout(30000);
            hikariConfig.setAutoCommit(true);

            // Additional optional configuration
            try {
                int maxPoolSize = config.getInt("data.storage.db.max_pool_size");
                hikariConfig.setMaximumPoolSize(maxPoolSize);
            } catch (ConfigurationException ignored) {
                // Use default
            }

            try {
                boolean autoCommit = config.getBoolean("data.storage.db.auto_commit");
                hikariConfig.setAutoCommit(autoCommit);
            } catch (ConfigurationException ignored) {
                // Use default
            }

            logger.info("Initializing database connection pool to {}", jdbcUrl);
            return new HikariDataSource(hikariConfig);
        } catch (ConfigurationException e) {
            logger.error("Missing required database configuration", e);
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    /**
     * Creates the necessary database schema if it doesn't exist.
     */
    private void initializeSchema() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create the data table
            stmt.execute("CREATE TABLE IF NOT EXISTS storage_data ("
                    + "scope VARCHAR(255) NOT NULL, "
                    + "key_name VARCHAR(255) NOT NULL, "
                    + "value_data LONGTEXT, "
                    + "PRIMARY KEY (scope, key_name))");

            // Create index for faster scope-based queries
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_storage_data_scope ON storage_data (scope)");

            logger.info("Database schema initialized successfully");
        } catch (SQLException e) {
            logger.error("Error initializing database schema", e);
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    // Implementation des méthodes abstraites d'AbstractDataStorage

    @Override
    protected <T> Optional<T> doGet(StorageKey key, Class<T> type) {
        String scope = key.getScope();
        String keyName = key.getKey();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT value_data FROM storage_data WHERE scope = ? AND key_name = ?")) {

            stmt.setString(1, scope);
            stmt.setString(2, keyName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("value_data");
                    T value = gson.fromJson(json, type);
                    return Optional.ofNullable(value);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting data for key {} in scope {}: {}",
                    keyName, scope, e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    protected <T> boolean doSet(StorageKey key, T value) {
        String scope = key.getScope();
        String keyName = key.getKey();
        String json = gson.toJson(value);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO storage_data (scope, key_name, value_data) VALUES (?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE value_data = ?")) {

            stmt.setString(1, scope);
            stmt.setString(2, keyName);
            stmt.setString(3, json);
            stmt.setString(4, json);

            int updated = stmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            logger.error("Error setting data for key {} in scope {}: {}",
                    keyName, scope, e.getMessage());
            return false;
        }
    }

    @Override
    protected boolean doExists(StorageKey key) {
        String scope = key.getScope();
        String keyName = key.getKey();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM storage_data WHERE scope = ? AND key_name = ?")) {

            stmt.setString(1, scope);
            stmt.setString(2, keyName);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking if key {} exists in scope {}: {}",
                    keyName, scope, e.getMessage());
            return false;
        }
    }

    @Override
    protected boolean doRemove(StorageKey key) {
        String scope = key.getScope();
        String keyName = key.getKey();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM storage_data WHERE scope = ? AND key_name = ?")) {

            stmt.setString(1, scope);
            stmt.setString(2, keyName);

            int deleted = stmt.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            logger.error("Error removing key {} from scope {}: {}",
                    keyName, scope, e.getMessage());
            return false;
        }
    }

    @Override
    protected Set<String> doGetKeys(String scope) {
        Set<String> keys = new HashSet<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT key_name FROM storage_data WHERE scope = ?")) {

            stmt.setString(1, scope);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    keys.add(rs.getString("key_name"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting keys for scope {}: {}", scope, e.getMessage());
        }

        return keys;
    }

    @Override
    protected Map<String, Object> doGetAll(String scope) {
        Map<String, Object> data = new HashMap<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT key_name, value_data FROM storage_data WHERE scope = ?")) {

            stmt.setString(1, scope);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String keyName = rs.getString("key_name");
                    String json = rs.getString("value_data");
                    Object value = gson.fromJson(json, Object.class);
                    data.put(keyName, value);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting all data for scope {}: {}", scope, e.getMessage());
        }

        return data;
    }

    @Override
    protected boolean doClear(String scope) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM storage_data WHERE scope = ?")) {

            stmt.setString(1, scope);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("Error clearing scope {}: {}", scope, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
        return true;
    }
}
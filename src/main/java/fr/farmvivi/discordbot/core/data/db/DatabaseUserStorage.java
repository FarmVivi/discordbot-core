package fr.farmvivi.discordbot.core.data.db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.farmvivi.discordbot.core.api.data.UserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Implémentation du stockage utilisateur basée sur une base de données.
 */
public class DatabaseUserStorage implements UserStorage {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUserStorage.class);
    private static final Gson gson = new GsonBuilder().create();

    private final DatabaseStorageProvider provider;

    /**
     * Crée un nouveau stockage utilisateur basé sur une base de données.
     *
     * @param provider le fournisseur de stockage
     */
    public DatabaseUserStorage(DatabaseStorageProvider provider) {
        this.provider = provider;
    }

    @Override
    public boolean exists(String key) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        String userId = parts[0];
        String dataKey = parts[1];

        return hasUserData(userId, dataKey);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return Optional.empty();
        }

        String userId = parts[0];
        String dataKey = parts[1];

        return getUserData(userId, dataKey, type);
    }

    @Override
    public <T> boolean set(String key, T value) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        String userId = parts[0];
        String dataKey = parts[1];

        return setUserData(userId, dataKey, value);
    }

    @Override
    public boolean remove(String key) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        String userId = parts[0];
        String dataKey = parts[1];

        return removeUserData(userId, dataKey);
    }

    @Override
    public Set<String> getKeys() {
        Set<String> keys = new HashSet<>();

        try (Connection conn = provider.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT user_id, data_key FROM user_data")) {

            while (rs.next()) {
                String userId = rs.getString("user_id");
                String dataKey = rs.getString("data_key");
                keys.add(userId + ":" + dataKey);
            }

        } catch (SQLException e) {
            logger.error("Error getting all keys", e);
        }

        return keys;
    }

    @Override
    public boolean clear() {
        try (Connection conn = provider.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE FROM user_data");
            return true;

        } catch (SQLException e) {
            logger.error("Error clearing all user data", e);
            return false;
        }
    }

    @Override
    public boolean save() {
        // La sauvegarde est immédiate dans une base de données
        return true;
    }

    @Override
    public <T> Optional<T> getUserData(String userId, String key, Class<T> type) {
        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT data_value FROM user_data WHERE user_id = ? AND data_key = ?")) {

            stmt.setString(1, userId);
            stmt.setString(2, key);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("data_value");
                    T value = gson.fromJson(json, type);
                    return Optional.ofNullable(value);
                }
            }

        } catch (SQLException e) {
            logger.error("Error getting user data for user {} and key {}", userId, key, e);
        }

        return Optional.empty();
    }

    @Override
    public <T> boolean setUserData(String userId, String key, T value) {
        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO user_data (user_id, data_key, data_value) VALUES (?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE data_value = ?")) {

            String json = gson.toJson(value);

            stmt.setString(1, userId);
            stmt.setString(2, key);
            stmt.setString(3, json);
            stmt.setString(4, json);

            int updated = stmt.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            logger.error("Error setting user data for user {} and key {}", userId, key, e);
            return false;
        }
    }

    @Override
    public boolean removeUserData(String userId, String key) {
        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM user_data WHERE user_id = ? AND data_key = ?")) {

            stmt.setString(1, userId);
            stmt.setString(2, key);

            int deleted = stmt.executeUpdate();
            return deleted > 0;

        } catch (SQLException e) {
            logger.error("Error removing user data for user {} and key {}", userId, key, e);
            return false;
        }
    }

    @Override
    public boolean hasUserData(String userId, String key) {
        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM user_data WHERE user_id = ? AND data_key = ?")) {

            stmt.setString(1, userId);
            stmt.setString(2, key);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            logger.error("Error checking if user data exists for user {} and key {}", userId, key, e);
            return false;
        }
    }

    @Override
    public Set<String> getUserKeys(String userId) {
        Set<String> keys = new HashSet<>();

        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT data_key FROM user_data WHERE user_id = ?")) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    keys.add(rs.getString("data_key"));
                }
            }

        } catch (SQLException e) {
            logger.error("Error getting user keys for user {}", userId, e);
        }

        return keys;
    }

    @Override
    public boolean clearUser(String userId) {
        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM user_data WHERE user_id = ?")) {

            stmt.setString(1, userId);

            int deleted = stmt.executeUpdate();
            return deleted > 0;

        } catch (SQLException e) {
            logger.error("Error clearing user data for user {}", userId, e);
            return false;
        }
    }
}
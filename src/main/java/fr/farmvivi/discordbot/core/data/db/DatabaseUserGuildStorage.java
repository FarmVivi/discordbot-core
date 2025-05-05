package fr.farmvivi.discordbot.core.data.db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.farmvivi.discordbot.core.api.data.UserGuildStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Implémentation du stockage utilisateur-guild basée sur une base de données.
 */
public class DatabaseUserGuildStorage implements UserGuildStorage {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUserGuildStorage.class);
    private static final Gson gson = new GsonBuilder().create();

    private final DatabaseStorageProvider provider;

    /**
     * Crée un nouveau stockage utilisateur-guild basé sur une base de données.
     *
     * @param provider le fournisseur de stockage
     */
    public DatabaseUserGuildStorage(DatabaseStorageProvider provider) {
        this.provider = provider;
    }

    @Override
    public boolean exists(String key) {
        String[] parts = key.split(":", 3);
        if (parts.length != 3) {
            return false;
        }

        String userId = parts[0];
        String guildId = parts[1];
        String dataKey = parts[2];

        return hasUserGuildData(userId, guildId, dataKey);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        String[] parts = key.split(":", 3);
        if (parts.length != 3) {
            return Optional.empty();
        }

        String userId = parts[0];
        String guildId = parts[1];
        String dataKey = parts[2];

        return getUserGuildData(userId, guildId, dataKey, type);
    }

    @Override
    public <T> boolean set(String key, T value) {
        String[] parts = key.split(":", 3);
        if (parts.length != 3) {
            return false;
        }

        String userId = parts[0];
        String guildId = parts[1];
        String dataKey = parts[2];

        return setUserGuildData(userId, guildId, dataKey, value);
    }

    @Override
    public boolean remove(String key) {
        String[] parts = key.split(":", 3);
        if (parts.length != 3) {
            return false;
        }

        String userId = parts[0];
        String guildId = parts[1];
        String dataKey = parts[2];

        return removeUserGuildData(userId, guildId, dataKey);
    }

    @Override
    public Set<String> getKeys() {
        Set<String> keys = new HashSet<>();

        try (Connection conn = provider.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT user_id, guild_id, data_key FROM user_guild_data")) {

            while (rs.next()) {
                String userId = rs.getString("user_id");
                String guildId = rs.getString("guild_id");
                String dataKey = rs.getString("data_key");
                keys.add(userId + ":" + guildId + ":" + dataKey);
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

            stmt.executeUpdate("DELETE FROM user_guild_data");
            return true;

        } catch (SQLException e) {
            logger.error("Error clearing all user-guild data", e);
            return false;
        }
    }

    @Override
    public boolean save() {
        // La sauvegarde est immédiate dans une base de données
        return true;
    }

    @Override
    public <T> Optional<T> getUserGuildData(String userId, String guildId, String key, Class<T> type) {
        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT data_value FROM user_guild_data WHERE user_id = ? AND guild_id = ? AND data_key = ?")) {

            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            stmt.setString(3, key);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("data_value");
                    T value = gson.fromJson(json, type);
                    return Optional.ofNullable(value);
                }
            }

        } catch (SQLException e) {
            logger.error("Error getting user-guild data for user {}, guild {} and key {}",
                    userId, guildId, key, e);
        }

        return Optional.empty();
    }

    @Override
    public <T> boolean setUserGuildData(String userId, String guildId, String key, T value) {
        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO user_guild_data (user_id, guild_id, data_key, data_value) VALUES (?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE data_value = ?")) {

            String json = gson.toJson(value);

            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            stmt.setString(3, key);
            stmt.setString(4, json);
            stmt.setString(5, json);

            int updated = stmt.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            logger.error("Error setting user-guild data for user {}, guild {} and key {}",
                    userId, guildId, key, e);
            return false;
        }
    }

    @Override
    public boolean removeUserGuildData(String userId, String guildId, String key) {
        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM user_guild_data WHERE user_id = ? AND guild_id = ? AND data_key = ?")) {

            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            stmt.setString(3, key);

            int deleted = stmt.executeUpdate();
            return deleted > 0;

        } catch (SQLException e) {
            logger.error("Error removing user-guild data for user {}, guild {} and key {}",
                    userId, guildId, key, e);
            return false;
        }
    }

    @Override
    public boolean hasUserGuildData(String userId, String guildId, String key) {
        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM user_guild_data WHERE user_id = ? AND guild_id = ? AND data_key = ?")) {

            stmt.setString(1, userId);
            stmt.setString(2, guildId);
            stmt.setString(3, key);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            logger.error("Error checking if user-guild data exists for user {}, guild {} and key {}",
                    userId, guildId, key, e);
            return false;
        }
    }

    @Override
    public Set<String> getUserGuildKeys(String userId, String guildId) {
        Set<String> keys = new HashSet<>();

        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT data_key FROM user_guild_data WHERE user_id = ? AND guild_id = ?")) {

            stmt.setString(1, userId);
            stmt.setString(2, guildId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    keys.add(rs.getString("data_key"));
                }
            }

        } catch (SQLException e) {
            logger.error("Error getting user-guild keys for user {} and guild {}", userId, guildId, e);
        }

        return keys;
    }

    @Override
    public boolean clearUserGuild(String userId, String guildId) {
        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM user_guild_data WHERE user_id = ? AND guild_id = ?")) {

            stmt.setString(1, userId);
            stmt.setString(2, guildId);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            logger.error("Error clearing user-guild data for user {} and guild {}", userId, guildId, e);
            return false;
        }
    }
}
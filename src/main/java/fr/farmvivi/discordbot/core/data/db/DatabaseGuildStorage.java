package fr.farmvivi.discordbot.core.data.db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.farmvivi.discordbot.core.api.data.GuildStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Implémentation du stockage de guild basée sur une base de données.
 */
public class DatabaseGuildStorage implements GuildStorage {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseGuildStorage.class);
    private static final Gson gson = new GsonBuilder().create();

    private final DatabaseStorageProvider provider;

    /**
     * Crée un nouveau stockage de guild basé sur une base de données.
     *
     * @param provider le fournisseur de stockage
     */
    public DatabaseGuildStorage(DatabaseStorageProvider provider) {
        this.provider = provider;
    }

    @Override
    public boolean exists(String key) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        String guildId = parts[0];
        String dataKey = parts[1];

        return hasGuildData(guildId, dataKey);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return Optional.empty();
        }

        String guildId = parts[0];
        String dataKey = parts[1];

        return getGuildData(guildId, dataKey, type);
    }

    @Override
    public <T> boolean set(String key, T value) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        String guildId = parts[0];
        String dataKey = parts[1];

        return setGuildData(guildId, dataKey, value);
    }

    @Override
    public boolean remove(String key) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        String guildId = parts[0];
        String dataKey = parts[1];

        return removeGuildData(guildId, dataKey);
    }

    @Override
    public Set<String> getKeys() {
        Set<String> keys = new HashSet<>();

        try (Connection conn = provider.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT guild_id, data_key FROM guild_data")) {

            while (rs.next()) {
                String guildId = rs.getString("guild_id");
                String dataKey = rs.getString("data_key");
                keys.add(guildId + ":" + dataKey);
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

            stmt.executeUpdate("DELETE FROM guild_data");
            return true;

        } catch (SQLException e) {
            logger.error("Error clearing all guild data", e);
            return false;
        }
    }

    @Override
    public boolean save() {
        // La sauvegarde est immédiate dans une base de données
        return true;
    }

    @Override
    public <T> Optional<T> getGuildData(String guildId, String key, Class<T> type) {
        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT data_value FROM guild_data WHERE guild_id = ? AND data_key = ?")) {

            stmt.setString(1, guildId);
            stmt.setString(2, key);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("data_value");
                    T value = gson.fromJson(json, type);
                    return Optional.ofNullable(value);
                }
            }

        } catch (SQLException e) {
            logger.error("Error getting guild data for guild {} and key {}", guildId, key, e);
        }

        return Optional.empty();
    }

    @Override
    public <T> boolean setGuildData(String guildId, String key, T value) {
        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO guild_data (guild_id, data_key, data_value) VALUES (?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE data_value = ?")) {

            String json = gson.toJson(value);

            stmt.setString(1, guildId);
            stmt.setString(2, key);
            stmt.setString(3, json);
            stmt.setString(4, json);

            int updated = stmt.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            logger.error("Error setting guild data for guild {} and key {}", guildId, key, e);
            return false;
        }
    }

    @Override
    public boolean removeGuildData(String guildId, String key) {
        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM guild_data WHERE guild_id = ? AND data_key = ?")) {

            stmt.setString(1, guildId);
            stmt.setString(2, key);

            int deleted = stmt.executeUpdate();
            return deleted > 0;

        } catch (SQLException e) {
            logger.error("Error removing guild data for guild {} and key {}", guildId, key, e);
            return false;
        }
    }

    @Override
    public boolean hasGuildData(String guildId, String key) {
        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM guild_data WHERE guild_id = ? AND data_key = ?")) {

            stmt.setString(1, guildId);
            stmt.setString(2, key);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            logger.error("Error checking if guild data exists for guild {} and key {}", guildId, key, e);
            return false;
        }
    }

    @Override
    public Set<String> getGuildKeys(String guildId) {
        Set<String> keys = new HashSet<>();

        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT data_key FROM guild_data WHERE guild_id = ?")) {

            stmt.setString(1, guildId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    keys.add(rs.getString("data_key"));
                }
            }

        } catch (SQLException e) {
            logger.error("Error getting guild keys for guild {}", guildId, e);
        }

        return keys;
    }

    @Override
    public boolean clearGuild(String guildId) {
        try (Connection conn = provider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM guild_data WHERE guild_id = ?")) {

            stmt.setString(1, guildId);

            int deleted = stmt.executeUpdate();
            return deleted > 0;

        } catch (SQLException e) {
            logger.error("Error clearing guild data for guild {}", guildId, e);
            return false;
        }
    }
}
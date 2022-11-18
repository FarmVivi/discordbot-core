package fr.farmvivi.discordbot.module.cnam.database.discorduser;

import fr.farmvivi.discordbot.module.cnam.database.DAO;
import fr.farmvivi.discordbot.module.cnam.database.DatabaseAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DiscordUserDAO extends DAO<DiscordUser, Long> {
    public DiscordUserDAO(DatabaseAccess db) {
        super("discord_user", db);
    }

    @Override
    public DiscordUser create(DiscordUser obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO discord_user (discord_id) VALUES (?)")) {

            long id = obj.getId();

            statement.setLong(1, id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating discordUser failed, no rows affected.");
            }

            return obj;
        }
    }

    @Override
    public List<DiscordUser> selectAll() throws SQLException {
        List<DiscordUser> discordUsers = new ArrayList<>();

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM discord_user")) {

            statement.executeQuery();

            while (statement.getResultSet().next()) {
                long id = statement.getResultSet().getLong("discord_id");

                discordUsers.add(new DiscordUser(id));
            }
        }

        return discordUsers;
    }

    @Override
    public DiscordUser selectById(Long id) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM discord_user WHERE discord_id = ?")) {

            statement.setLong(1, id);

            statement.executeQuery();

            if (statement.getResultSet().next()) {
                long discordId = statement.getResultSet().getLong("discord_id");

                return new DiscordUser(discordId);
            }
        }

        return null;
    }

    @Override
    public boolean update(DiscordUser obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE discord_user SET discord_id = ? WHERE discord_id = ?")) {

            long id = obj.getId();

            statement.setLong(1, id);
            statement.setLong(2, id);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected < 1) {
                logger.warn("DiscordUser with id " + id + " not found");
                return false;
            } else if (rowsAffected > 1) {
                logger.warn("Multiple discordUsers with id " + id + " found");
                return true;
            } else {
                return true;
            }
        }
    }

    @Override
    public boolean delete(DiscordUser obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM discord_user WHERE discord_id = ?")) {

            long id = obj.getId();

            statement.setLong(1, id);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected < 1) {
                logger.warn("DiscordUser with id " + id + " not found");
                return false;
            } else if (rowsAffected > 1) {
                logger.warn("Multiple discordUsers with id " + id + " found");
                return true;
            } else {
                return true;
            }
        }
    }
}

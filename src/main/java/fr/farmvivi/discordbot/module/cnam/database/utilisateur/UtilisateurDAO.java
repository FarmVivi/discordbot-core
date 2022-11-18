package fr.farmvivi.discordbot.module.cnam.database.utilisateur;

import fr.farmvivi.discordbot.module.cnam.database.DAO;
import fr.farmvivi.discordbot.module.cnam.database.DatabaseAccess;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO extends DAO<Utilisateur, Integer> {
    public UtilisateurDAO(DatabaseAccess db) {
        super("utilisateur", db);
    }

    @Override
    public Utilisateur create(Utilisateur obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO utilisateur (code_scolarite, discord_id) VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {

            String codeScolarite = obj.getCodeScolarite();
            Long discordId = obj.getDiscordId();

            if (codeScolarite != null) {
                statement.setString(1, codeScolarite);
            } else {
                statement.setNull(1, Types.VARCHAR);
            }
            if (discordId != null) {
                statement.setLong(2, discordId);
            } else {
                statement.setNull(2, Types.BIGINT);
            }

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating utilisateur failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);

                    return new Utilisateur(id, codeScolarite, discordId);
                } else {
                    throw new SQLException("Creating utilisateur failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public List<Utilisateur> selectAll() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM utilisateur")) {

            statement.executeQuery();

            while (statement.getResultSet().next()) {
                int id = statement.getResultSet().getInt("id_utilisateur");
                String codeScolarite = statement.getResultSet().getString("code_scolarite");
                Long discordId = statement.getResultSet().getLong("discord_id");

                utilisateurs.add(new Utilisateur(id, codeScolarite, discordId));
            }
        }

        return utilisateurs;
    }

    @Override
    public Utilisateur selectById(Integer id) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM utilisateur WHERE id_utilisateur = ?")) {

            statement.setInt(1, id);
            statement.executeQuery();

            if (statement.getResultSet().next()) {
                String codeScolarite = statement.getResultSet().getString("code_scolarite");
                Long discordId = statement.getResultSet().getLong("discord_id");

                return new Utilisateur(id, codeScolarite, discordId);
            }
        }

        return null;
    }

    public Utilisateur selectByDiscordId(Long discordId) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM utilisateur WHERE discord_id = ?")) {

            statement.setLong(1, discordId);
            statement.executeQuery();

            if (statement.getResultSet().next()) {
                int id = statement.getResultSet().getInt("id_utilisateur");
                String codeScolarite = statement.getResultSet().getString("code_scolarite");

                return new Utilisateur(id, codeScolarite, discordId);
            }
        }

        return null;
    }

    @Override
    public boolean update(Utilisateur obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE utilisateur SET code_scolarite = ?, discord_id = ? WHERE id_utilisateur = ?")) {

            String codeScolarite = obj.getCodeScolarite();
            Long discordId = obj.getDiscordId();
            int id = obj.getId();

            statement.setString(1, codeScolarite);
            if (discordId != null) {
                statement.setLong(2, discordId);
            } else {
                statement.setNull(2, Types.BIGINT);
            }
            statement.setInt(3, id);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected < 1) {
                logger.warn("Utilisateur with id " + id + " not found");
                return false;
            } else if (rowsAffected > 1) {
                logger.warn("Multiple utilisateurs with id " + id + " found");
                return true;
            } else {
                return true;
            }
        }
    }

    @Override
    public boolean delete(Utilisateur obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM utilisateur WHERE id_utilisateur = ?")) {

            int id = obj.getId();

            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected < 1) {
                logger.warn("Utilisateur with id " + id + " not found");
                return false;
            } else if (rowsAffected > 1) {
                logger.warn("Multiple utilisateurs with id " + id + " found");
                return true;
            } else {
                return true;
            }
        }
    }
}

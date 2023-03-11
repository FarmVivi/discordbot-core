package fr.farmvivi.discordbot.module.cnam.database.salle;

import fr.farmvivi.discordbot.module.cnam.database.DAO;
import fr.farmvivi.discordbot.module.cnam.database.DatabaseAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalleDAO extends DAO<Salle, Integer> {
    public SalleDAO(DatabaseAccess db) {
        super("salle", db);
    }

    @Override
    public Salle create(Salle obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO salle (nom_salle, adresse_salle) VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {

            String nom = obj.getNom();
            String adresse = obj.getAdresse();

            statement.setString(1, nom);
            statement.setString(2, adresse);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating salle failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);

                    return new Salle(id, nom, adresse);
                } else {
                    throw new SQLException("Creating salle failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public List<Salle> selectAll() throws SQLException {
        List<Salle> salles = new ArrayList<>();

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM salle")) {

            statement.executeQuery();

            while (statement.getResultSet().next()) {
                int id = statement.getResultSet().getInt("id_salle");
                String nom = statement.getResultSet().getString("nom_salle");
                String adresse = statement.getResultSet().getString("adresse_salle");

                salles.add(new Salle(id, nom, adresse));
            }
        }

        return salles;
    }

    @Override
    public Salle selectById(Integer id) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM salle WHERE id_salle = ?")) {

            statement.setInt(1, id);
            statement.executeQuery();

            if (statement.getResultSet().next()) {
                String nom = statement.getResultSet().getString("nom_salle");
                String adresse = statement.getResultSet().getString("adresse_salle");

                return new Salle(id, nom, adresse);
            }
        }

        return null;
    }

    public Salle selectByNom(String nom) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM salle WHERE nom_salle = ?")) {

            statement.setString(1, nom);
            statement.executeQuery();

            if (statement.getResultSet().next()) {
                int id = statement.getResultSet().getInt("id_salle");
                String adresse = statement.getResultSet().getString("adresse_salle");

                return new Salle(id, nom, adresse);
            }
        }

        return null;
    }

    @Override
    public boolean update(Salle obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE salle SET nom_salle = ?, adresse_salle = ? WHERE id_salle = ?")) {

            int id = obj.getId();
            String nom = obj.getNom();
            String adresse = obj.getAdresse();

            statement.setString(1, nom);
            statement.setString(2, adresse);
            statement.setInt(3, id);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected < 1) {
                logger.warn("Salle with id " + id + " not found");
                return false;
            } else if (rowsAffected > 1) {
                logger.warn("Multiple salles with id " + id + " found");
                return true;
            } else {
                return true;
            }
        }
    }

    @Override
    public boolean delete(Salle obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM salle WHERE id_salle = ?")) {

            int id = obj.getId();

            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected < 1) {
                logger.warn("Salle with id " + id + " not found");
                return false;
            } else if (rowsAffected > 1) {
                logger.warn("Multiple salles with id " + id + " found");
                return true;
            } else {
                return true;
            }
        }
    }
}

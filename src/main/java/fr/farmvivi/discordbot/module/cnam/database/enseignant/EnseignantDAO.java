package fr.farmvivi.discordbot.module.cnam.database.enseignant;

import fr.farmvivi.discordbot.module.cnam.database.DAO;
import fr.farmvivi.discordbot.module.cnam.database.DatabaseAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EnseignantDAO extends DAO<Enseignant, Integer> {
    public EnseignantDAO(DatabaseAccess db) {
        super("enseignant", db);
    }

    @Override
    public Enseignant create(Enseignant obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO enseignant (nom_enseignant, prenom_enseignant) VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {

            String nom = obj.getNom();
            String prenom = obj.getPrenom();

            statement.setString(1, nom);
            statement.setString(2, prenom);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating enseignant failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);

                    return new Enseignant(id, nom, prenom);
                } else {
                    throw new SQLException("Creating enseignant failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public List<Enseignant> selectAll() throws SQLException {
        List<Enseignant> enseignants = new ArrayList<>();

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM enseignant")) {

            statement.executeQuery();

            while (statement.getResultSet().next()) {
                int id = statement.getResultSet().getInt("id_enseignant");
                String nom = statement.getResultSet().getString("nom_enseignant");
                String prenom = statement.getResultSet().getString("prenom_enseignant");

                enseignants.add(new Enseignant(id, nom, prenom));
            }
        }

        return enseignants;
    }

    @Override
    public Enseignant selectById(Integer id) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM enseignant WHERE id_enseignant = ?")) {

            statement.setInt(1, id);
            statement.executeQuery();

            if (statement.getResultSet().next()) {
                String nom = statement.getResultSet().getString("nom_enseignant");
                String prenom = statement.getResultSet().getString("prenom_enseignant");

                return new Enseignant(id, nom, prenom);
            }
        }

        return null;
    }

    public Enseignant selectByNomPrenom(String nom, String prenom) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM enseignant WHERE nom_enseignant = ? AND prenom_enseignant = ?")) {

            statement.setString(1, nom);
            statement.setString(2, prenom);
            statement.executeQuery();

            if (statement.getResultSet().next()) {
                int id = statement.getResultSet().getInt("id_enseignant");

                return new Enseignant(id, nom, prenom);
            }
        }

        return null;
    }

    @Override
    public boolean update(Enseignant obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE enseignant SET nom_enseignant = ?, prenom_enseignant = ? WHERE id_enseignant = ?")) {

            int id = obj.getId();
            String nom = obj.getNom();
            String prenom = obj.getPrenom();

            statement.setString(1, nom);
            statement.setString(2, prenom);
            statement.setInt(3, id);
            int affectedRows = statement.executeUpdate();

            if (affectedRows < 1) {
                logger.warn("Enseignant with id " + id + " not found");
                return false;
            } else if (affectedRows > 1) {
                logger.warn("Multiple enseignant with id " + id + " found");
                return false;
            } else {
                return true;
            }
        }
    }

    @Override
    public boolean delete(Enseignant obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM enseignant WHERE id_enseignant = ?")) {

            int id = obj.getId();

            statement.setInt(1, id);
            int affectedRows = statement.executeUpdate();

            if (affectedRows < 1) {
                logger.warn("Enseignant with id " + id + " not found");
                return false;
            } else if (affectedRows > 1) {
                logger.warn("Multiple enseignant with id " + id + " found");
                return true;
            } else {
                return true;
            }
        }
    }
}

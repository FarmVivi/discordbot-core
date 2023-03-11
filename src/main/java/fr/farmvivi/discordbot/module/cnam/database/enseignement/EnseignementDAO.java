package fr.farmvivi.discordbot.module.cnam.database.enseignement;

import fr.farmvivi.discordbot.module.cnam.database.DAO;
import fr.farmvivi.discordbot.module.cnam.database.DatabaseAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EnseignementDAO extends DAO<Enseignement, String> {
    public EnseignementDAO(DatabaseAccess db) {
        super("enseignement", db);
    }

    @Override
    public Enseignement create(Enseignement enseignement) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO enseignement (code_enseignement, nom_enseignement) VALUES (?, ?)")) {

            String code = enseignement.getCode();
            String nom = enseignement.getNom();

            statement.setString(1, code);
            statement.setString(2, nom);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating enseignement failed, no rows affected.");
            }

            return new Enseignement(code, nom);
        }
    }

    @Override
    public List<Enseignement> selectAll() throws SQLException {
        List<Enseignement> enseignements = new ArrayList<>();

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM enseignement")) {

            statement.executeQuery();

            while (statement.getResultSet().next()) {
                String code = statement.getResultSet().getString("code_enseignement");
                String nom = statement.getResultSet().getString("nom_enseignement");

                enseignements.add(new Enseignement(code, nom));
            }
        }

        return enseignements;
    }

    @Override
    public Enseignement selectById(String id) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM enseignement WHERE code_enseignement = ?")) {

            statement.setString(1, id);
            statement.executeQuery();

            if (statement.getResultSet().next()) {
                String nom = statement.getResultSet().getString("nom_enseignement");

                return new Enseignement(id, nom);
            }
        }

        return null;
    }

    @Override
    public boolean update(Enseignement enseignement) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE enseignement SET nom_enseignement = ? WHERE code_enseignement = ?")) {

            String code = enseignement.getCode();
            String nom = enseignement.getNom();

            statement.setString(1, nom);
            statement.setString(2, code);
            int affectedRows = statement.executeUpdate();

            if (affectedRows < 1) {
                logger.warn("Enseignement with code " + code + " not found");
                return false;
            } else if (affectedRows > 1) {
                logger.warn("Multiple enseignements with code " + code + " found");
                return true;
            } else {
                return true;
            }
        }
    }

    @Override
    public boolean delete(Enseignement enseignement) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM enseignement WHERE code_enseignement = ?")) {

            String code = enseignement.getCode();

            statement.setString(1, code);
            int affectedRows = statement.executeUpdate();

            if (affectedRows < 1) {
                logger.warn("Enseignement with code " + code + " not found");
                return false;
            } else if (affectedRows > 1) {
                logger.warn("Multiple enseignements with code " + code + " found");
                return true;
            } else {
                return true;
            }
        }
    }
}

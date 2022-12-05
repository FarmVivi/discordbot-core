package fr.farmvivi.discordbot.module.cnam.database.devoirutilisateur;

import fr.farmvivi.discordbot.module.cnam.database.DAO;
import fr.farmvivi.discordbot.module.cnam.database.DatabaseAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DevoirUtilisateurDAO extends DAO<DevoirUtilisateur, Integer> {
    public DevoirUtilisateurDAO(DatabaseAccess db) {
        super("devoir_utilisateur", db);
    }

    @Override
    public DevoirUtilisateur create(DevoirUtilisateur obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO devoir_utilisateur (id_devoir, id_utilisateur, date_fait) VALUES (?, ?, ?)")) {

            int idDevoir = obj.getIdDevoir();
            int idUtilisateur = obj.getIdUtilisateur();
            LocalDate dateFait = obj.getDateFait();

            statement.setInt(1, idDevoir);
            statement.setInt(2, idUtilisateur);
            statement.setDate(3, java.sql.Date.valueOf(dateFait));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating devoir utilisateur failed, no rows affected.");
            }

            return new DevoirUtilisateur(idDevoir, idUtilisateur, dateFait);
        }
    }

    @Override
    public boolean update(DevoirUtilisateur obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE devoir_utilisateur SET date_fait = ? WHERE id_devoir = ? AND id_utilisateur = ?")) {

            int idDevoir = obj.getIdDevoir();
            int idUtilisateur = obj.getIdUtilisateur();
            LocalDate dateFait = obj.getDateFait();

            statement.setDate(1, java.sql.Date.valueOf(dateFait));
            statement.setInt(2, idDevoir);
            statement.setInt(3, idUtilisateur);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected < 1) {
                logger.warn("Devoir utilisateur with id " + idDevoir + " and " + idUtilisateur + " not found");
                return false;
            } else if (rowsAffected > 1) {
                logger.warn("Multiple devoirs utilisateur with id " + idDevoir + " and " + idUtilisateur + " found");
                return true;
            } else {
                return true;
            }
        }
    }

    @Override
    public boolean delete(DevoirUtilisateur obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM devoir_utilisateur WHERE id_devoir = ? AND id_utilisateur = ?")) {

            int idDevoir = obj.getIdDevoir();
            int idUtilisateur = obj.getIdUtilisateur();

            statement.setInt(1, idDevoir);
            statement.setInt(2, idUtilisateur);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected < 1) {
                logger.warn("Devoir utilisateur with id " + idDevoir + " and " + idUtilisateur + " not found");
                return false;
            } else if (rowsAffected > 1) {
                logger.warn("Multiple devoirs utilisateur with id " + idDevoir + " and " + idUtilisateur + " found");
                return true;
            } else {
                return true;
            }
        }
    }

    @Override
    public List<DevoirUtilisateur> selectAll() throws SQLException {
        List<DevoirUtilisateur> devoirsUtilisateur = new ArrayList<>();

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM devoir_utilisateur");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int idDevoir = resultSet.getInt("id_devoir");
                int idUtilisateur = resultSet.getInt("id_utilisateur");
                LocalDate dateFait = resultSet.getDate("date_fait").toLocalDate();

                devoirsUtilisateur.add(new DevoirUtilisateur(idDevoir, idUtilisateur, dateFait));
            }
        }

        return devoirsUtilisateur;
    }

    @Override
    public DevoirUtilisateur selectById(Integer id) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("DevoirUtilisateur cannot be selected by id, please use selectByIdDevoirAndIdUtilisateur instead !");
    }

    public DevoirUtilisateur selectByIdDevoirAndIdUtilisateur(int idDevoir, int idUtilisateur) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM devoir_utilisateur WHERE id_devoir = ? AND id_utilisateur = ?")) {

            statement.setInt(1, idDevoir);
            statement.setInt(2, idUtilisateur);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    LocalDate dateFait = resultSet.getDate("date_fait").toLocalDate();

                    return new DevoirUtilisateur(idDevoir, idUtilisateur, dateFait);
                } else {
                    return null;
                }
            }
        }
    }

    public List<DevoirUtilisateur> selectByIdUtilisateur(int idUtilisateur) throws SQLException {
        List<DevoirUtilisateur> devoirsUtilisateur = new ArrayList<>();

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM devoir_utilisateur WHERE id_utilisateur = ?")) {

            statement.setInt(1, idUtilisateur);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int idDevoir = resultSet.getInt("id_devoir");
                    LocalDate dateFait = resultSet.getDate("date_fait").toLocalDate();

                    devoirsUtilisateur.add(new DevoirUtilisateur(idDevoir, idUtilisateur, dateFait));
                }
            }
        }

        return devoirsUtilisateur;
    }

    public List<DevoirUtilisateur> selectByIdDevoir(int idDevoir) throws SQLException {
        List<DevoirUtilisateur> devoirsUtilisateur = new ArrayList<>();

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM devoir_utilisateur WHERE id_devoir = ?")) {

            statement.setInt(1, idDevoir);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int idUtilisateur = resultSet.getInt("id_utilisateur");
                    LocalDate dateFait = resultSet.getDate("date_fait").toLocalDate();

                    devoirsUtilisateur.add(new DevoirUtilisateur(idDevoir, idUtilisateur, dateFait));
                }
            }
        }

        return devoirsUtilisateur;
    }
}

package fr.farmvivi.discordbot.module.cnam.database.cours;

import fr.farmvivi.discordbot.module.cnam.database.DAO;
import fr.farmvivi.discordbot.module.cnam.database.DatabaseAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CoursDAO extends DAO<Cours, Integer> {
    public CoursDAO(DatabaseAccess db) {
        super("cours", db);
    }

    @Override
    public Cours create(Cours obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO cours (date_cours, debut_cours, fin_cours, presentiel, id_enseignant, id_salle, code_enseignement) VALUES (?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {

            LocalDate date = obj.getDate();
            LocalTime heureDebut = obj.getHeureDebut();
            LocalTime heureFin = obj.getHeureFin();
            boolean presentiel = obj.isPresentiel();
            int enseignantId = obj.getEnseignantId();
            int salleId = obj.getSalleId();
            String enseignementCode = obj.getEnseignementCode();

            statement.setDate(1, java.sql.Date.valueOf(date));
            statement.setTime(2, java.sql.Time.valueOf(heureDebut));
            statement.setTime(3, java.sql.Time.valueOf(heureFin));
            statement.setBoolean(4, presentiel);
            statement.setInt(5, enseignantId);
            statement.setInt(6, salleId);
            statement.setString(7, enseignementCode);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating cours failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);

                    return new Cours(id, date, heureDebut, heureFin, presentiel, enseignantId, salleId, enseignementCode);
                } else {
                    throw new SQLException("Creating cours failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public List<Cours> selectAll() throws SQLException {
        List<Cours> cours = new ArrayList<>();

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM cours")) {

            statement.executeQuery();

            while (statement.getResultSet().next()) {
                int id = statement.getResultSet().getInt("id_cours");
                LocalDate date = statement.getResultSet().getDate("date_cours").toLocalDate();
                LocalTime heureDebut = statement.getResultSet().getTime("debut_cours").toLocalTime();
                LocalTime heureFin = statement.getResultSet().getTime("fin_cours").toLocalTime();
                boolean presentiel = statement.getResultSet().getBoolean("presentiel");
                int enseignantId = statement.getResultSet().getInt("id_enseignant");
                int salleId = statement.getResultSet().getInt("id_salle");
                String enseignementCode = statement.getResultSet().getString("code_enseignement");

                cours.add(new Cours(id, date, heureDebut, heureFin, presentiel, enseignantId, salleId, enseignementCode));
            }
        }

        return cours;
    }

    @Override
    public Cours selectById(Integer id) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM cours WHERE id_cours = ?")) {

            statement.setInt(1, id);
            statement.executeQuery();

            if (statement.getResultSet().next()) {
                LocalDate date = statement.getResultSet().getDate("date_cours").toLocalDate();
                LocalTime heureDebut = statement.getResultSet().getTime("debut_cours").toLocalTime();
                LocalTime heureFin = statement.getResultSet().getTime("fin_cours").toLocalTime();
                boolean presentiel = statement.getResultSet().getBoolean("presentiel");
                int enseignantId = statement.getResultSet().getInt("id_enseignant");
                int salleId = statement.getResultSet().getInt("id_salle");
                String enseignementCode = statement.getResultSet().getString("code_enseignement");

                return new Cours(id, date, heureDebut, heureFin, presentiel, enseignantId, salleId, enseignementCode);
            }
        }

        return null;
    }

    public Cours selectByDateHeure(LocalDate date, LocalTime heureDebut, LocalTime heureFin) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM cours WHERE date_cours = ? AND debut_cours = ? AND fin_cours = ?")) {

            statement.setDate(1, java.sql.Date.valueOf(date));
            statement.setTime(2, java.sql.Time.valueOf(heureDebut));
            statement.setTime(3, java.sql.Time.valueOf(heureFin));
            statement.executeQuery();

            if (statement.getResultSet().next()) {
                int id = statement.getResultSet().getInt("id_cours");
                boolean presentiel = statement.getResultSet().getBoolean("presentiel");
                int enseignantId = statement.getResultSet().getInt("id_enseignant");
                int salleId = statement.getResultSet().getInt("id_salle");
                String enseignementCode = statement.getResultSet().getString("code_enseignement");

                return new Cours(id, date, heureDebut, heureFin, presentiel, enseignantId, salleId, enseignementCode);
            }
        }

        return null;
    }

    public Cours selectByDateBetweenHeure(LocalDate date, LocalTime heure) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM cours WHERE date_cours = ? AND debut_cours <= ? AND fin_cours >= ?")) {

            statement.setDate(1, java.sql.Date.valueOf(date));
            statement.setTime(2, java.sql.Time.valueOf(heure));
            statement.setTime(3, java.sql.Time.valueOf(heure));
            statement.executeQuery();

            if (statement.getResultSet().next()) {
                int id = statement.getResultSet().getInt("id_cours");
                LocalTime heureDebut = statement.getResultSet().getTime("debut_cours").toLocalTime();
                LocalTime heureFin = statement.getResultSet().getTime("fin_cours").toLocalTime();
                boolean presentiel = statement.getResultSet().getBoolean("presentiel");
                int enseignantId = statement.getResultSet().getInt("id_enseignant");
                int salleId = statement.getResultSet().getInt("id_salle");
                String enseignementCode = statement.getResultSet().getString("code_enseignement");

                return new Cours(id, date, heureDebut, heureFin, presentiel, enseignantId, salleId, enseignementCode);
            }
        }

        return null;
    }

    @Override
    public boolean update(Cours obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE cours SET date_cours = ?, debut_cours = ?, fin_cours = ?, presentiel = ?, id_enseignant = ?, id_salle = ?, code_enseignement = ? WHERE id_cours = ?")) {

            int id = obj.getId();
            LocalDate date = obj.getDate();
            LocalTime heureDebut = obj.getHeureDebut();
            LocalTime heureFin = obj.getHeureFin();
            boolean presentiel = obj.isPresentiel();
            int enseignantId = obj.getEnseignantId();
            int salleId = obj.getSalleId();
            String enseignementCode = obj.getEnseignementCode();

            statement.setDate(1, java.sql.Date.valueOf(date));
            statement.setTime(2, java.sql.Time.valueOf(heureDebut));
            statement.setTime(3, java.sql.Time.valueOf(heureFin));
            statement.setBoolean(4, presentiel);
            statement.setInt(5, enseignantId);
            statement.setInt(6, salleId);
            statement.setString(7, enseignementCode);
            statement.setInt(8, id);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected < 1) {
                logger.warn("Cours with id " + id + " not found");
                return false;
            } else if (rowsAffected > 1) {
                logger.warn("Multiple cours with id " + id + " found");
                return true;
            } else {
                return true;
            }
        }
    }

    @Override
    public boolean delete(Cours obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM cours WHERE id_cours = ?")) {

            int id = obj.getId();

            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected < 1) {
                logger.warn("Cours with id " + id + " not found");
                return false;
            } else if (rowsAffected > 1) {
                logger.warn("Multiple cours with id " + id + " found");
                return true;
            } else {
                return true;
            }
        }
    }
}

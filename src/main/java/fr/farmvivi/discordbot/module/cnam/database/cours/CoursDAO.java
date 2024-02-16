package fr.farmvivi.discordbot.module.cnam.database.cours;

import fr.farmvivi.discordbot.module.cnam.database.DAO;
import fr.farmvivi.discordbot.module.cnam.database.DatabaseAccess;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CoursDAO extends DAO<Cours, Integer> {
    public CoursDAO(DatabaseAccess db) {
        super("cours", db);
    }

    @Override
    public Cours create(Cours obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO cours (debut_cours, fin_cours, presentiel, id_enseignant, id_salle, code_enseignement) VALUES (?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {

            LocalDateTime debutCours = obj.getDebutCours();
            LocalDateTime finCours = obj.getFinCours();
            boolean presentiel = obj.isPresentiel();
            int enseignantId = obj.getEnseignantId();
            int salleId = obj.getSalleId();
            String enseignementCode = obj.getEnseignementCode();

            statement.setObject(1, debutCours);
            statement.setObject(2, finCours);
            statement.setBoolean(3, presentiel);
            statement.setInt(4, enseignantId);
            statement.setInt(5, salleId);
            statement.setString(6, enseignementCode);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating cours failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);

                    return new Cours(id, debutCours, finCours, presentiel, enseignantId, salleId, enseignementCode);
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
                LocalDateTime debutCours = statement.getResultSet().getObject("debut_cours", LocalDateTime.class);
                LocalDateTime finCours = statement.getResultSet().getObject("fin_cours", LocalDateTime.class);
                boolean presentiel = statement.getResultSet().getBoolean("presentiel");
                int enseignantId = statement.getResultSet().getInt("id_enseignant");
                int salleId = statement.getResultSet().getInt("id_salle");
                String enseignementCode = statement.getResultSet().getString("code_enseignement");

                cours.add(new Cours(id, debutCours, finCours, presentiel, enseignantId, salleId, enseignementCode));
            }
        }

        return cours;
    }

    public List<Cours> selectAllByDate(LocalDate date) throws SQLException {
        List<Cours> cours = new ArrayList<>();

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM cours WHERE DATE(debut_cours) <= ? AND DATE(fin_cours) >= ?")) {

            statement.setDate(1, Date.valueOf(date));
            statement.setDate(2, Date.valueOf(date));
            statement.executeQuery();

            while (statement.getResultSet().next()) {
                int id = statement.getResultSet().getInt("id_cours");
                LocalDateTime debutCours = statement.getResultSet().getObject("debut_cours", LocalDateTime.class);
                LocalDateTime finCours = statement.getResultSet().getObject("fin_cours", LocalDateTime.class);
                boolean presentiel = statement.getResultSet().getBoolean("presentiel");
                int enseignantId = statement.getResultSet().getInt("id_enseignant");
                int salleId = statement.getResultSet().getInt("id_salle");
                String enseignementCode = statement.getResultSet().getString("code_enseignement");

                cours.add(new Cours(id, debutCours, finCours, presentiel, enseignantId, salleId, enseignementCode));
            }
        }

        return cours;
    }

    public List<Cours> selectAllByDateTime(LocalDateTime dateTime) throws SQLException {
        List<Cours> cours = new ArrayList<>();

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM cours WHERE ? BETWEEN debut_cours AND fin_cours")) {

            statement.setObject(1, dateTime);
            statement.executeQuery();

            while (statement.getResultSet().next()) {
                int id = statement.getResultSet().getInt("id_cours");
                LocalDateTime debutCours = statement.getResultSet().getObject("debut_cours", LocalDateTime.class);
                LocalDateTime finCours = statement.getResultSet().getObject("fin_cours", LocalDateTime.class);
                boolean presentiel = statement.getResultSet().getBoolean("presentiel");
                int enseignantId = statement.getResultSet().getInt("id_enseignant");
                int salleId = statement.getResultSet().getInt("id_salle");
                String enseignementCode = statement.getResultSet().getString("code_enseignement");

                cours.add(new Cours(id, debutCours, finCours, presentiel, enseignantId, salleId, enseignementCode));
            }
        }

        return cours;
    }

    public List<Cours> selectAllByHoraires(LocalDateTime debut, LocalDateTime fin) throws SQLException {
        List<Cours> cours = new ArrayList<>();

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM cours WHERE debut_cours = ? AND fin_cours = ?")) {

            statement.setObject(1, debut);
            statement.setObject(2, fin);
            statement.executeQuery();

            while (statement.getResultSet().next()) {
                int id = statement.getResultSet().getInt("id_cours");
                LocalDateTime debutCours = statement.getResultSet().getObject("debut_cours", LocalDateTime.class);
                LocalDateTime finCours = statement.getResultSet().getObject("fin_cours", LocalDateTime.class);
                boolean presentiel = statement.getResultSet().getBoolean("presentiel");
                int enseignantId = statement.getResultSet().getInt("id_enseignant");
                int salleId = statement.getResultSet().getInt("id_salle");
                String enseignementCode = statement.getResultSet().getString("code_enseignement");

                cours.add(new Cours(id, debutCours, finCours, presentiel, enseignantId, salleId, enseignementCode));
            }
        }

        return cours;
    }

    public List<Cours> selectAllByEnseignementEnseignant(String enseignementCode, int idEnseignant) throws SQLException {
        List<Cours> cours = new ArrayList<>();

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM cours WHERE code_enseignement = ? AND id_enseignant = ?")) {

            statement.setString(1, enseignementCode);
            statement.setInt(2, idEnseignant);
            statement.executeQuery();

            while (statement.getResultSet().next()) {
                int id = statement.getResultSet().getInt("id_cours");
                LocalDateTime debutCours = statement.getResultSet().getObject("debut_cours", LocalDateTime.class);
                LocalDateTime finCours = statement.getResultSet().getObject("fin_cours", LocalDateTime.class);
                boolean presentiel = statement.getResultSet().getBoolean("presentiel");
                int salleId = statement.getResultSet().getInt("id_salle");

                cours.add(new Cours(id, debutCours, finCours, presentiel, idEnseignant, salleId, enseignementCode));
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
                LocalDateTime debutCours = statement.getResultSet().getObject("debut_cours", LocalDateTime.class);
                LocalDateTime finCours = statement.getResultSet().getObject("fin_cours", LocalDateTime.class);
                boolean presentiel = statement.getResultSet().getBoolean("presentiel");
                int enseignantId = statement.getResultSet().getInt("id_enseignant");
                int salleId = statement.getResultSet().getInt("id_salle");
                String enseignementCode = statement.getResultSet().getString("code_enseignement");

                return new Cours(id, debutCours, finCours, presentiel, enseignantId, salleId, enseignementCode);
            }
        }

        return null;
    }

    @Override
    public boolean update(Cours obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE cours SET debut_cours = ?, fin_cours = ?, presentiel = ?, id_enseignant = ?, id_salle = ?, code_enseignement = ? WHERE id_cours = ?")) {

            int id = obj.getId();
            LocalDateTime debut = obj.getDebutCours();
            LocalDateTime fin = obj.getFinCours();
            boolean presentiel = obj.isPresentiel();
            int enseignantId = obj.getEnseignantId();
            int salleId = obj.getSalleId();
            String enseignementCode = obj.getEnseignementCode();

            statement.setObject(1, debut);
            statement.setObject(2, fin);
            statement.setBoolean(3, presentiel);
            statement.setInt(4, enseignantId);
            statement.setInt(5, salleId);
            statement.setString(6, enseignementCode);
            statement.setInt(7, id);
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

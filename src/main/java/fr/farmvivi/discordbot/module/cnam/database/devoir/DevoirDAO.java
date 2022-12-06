package fr.farmvivi.discordbot.module.cnam.database.devoir;

import fr.farmvivi.discordbot.module.cnam.database.DAO;
import fr.farmvivi.discordbot.module.cnam.database.DatabaseAccess;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DevoirDAO extends DAO<Devoir, Integer> {
    public DevoirDAO(DatabaseAccess db) {
        super("devoir", db);
    }

    @Override
    public Devoir create(Devoir obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO devoir (date_pour, description, optionnel, discord_message_id, id_enseignant, code_enseignement, id_cours_pour, id_cours_donne) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {

            LocalDate datePour = obj.getDatePour();
            String description = obj.getDescription();
            Boolean optionnel = obj.isOptionnel();
            Long discordMessageId = obj.getDiscordMessageId();
            Integer idEnseignant = obj.getIdEnseignant();
            String codeEnseignement = obj.getCodeEnseignement();
            Integer idCoursPour = obj.getIdCoursPour();
            Integer idCoursDonne = obj.getIdCoursDonne();

            statement.setDate(1, java.sql.Date.valueOf(datePour));
            statement.setString(2, description);
            statement.setBoolean(3, optionnel);
            if (discordMessageId != null) {
                statement.setLong(4, discordMessageId);
            } else {
                statement.setNull(4, Types.BIGINT);
            }
            statement.setInt(5, idEnseignant);
            statement.setString(6, codeEnseignement);
            if (idCoursPour != null) {
                statement.setInt(7, idCoursPour);
            } else {
                statement.setNull(7, Types.INTEGER);
            }
            statement.setInt(8, idCoursDonne);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating devoir failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);

                    return new Devoir(id, datePour, description, optionnel, discordMessageId, idEnseignant, codeEnseignement, idCoursPour, idCoursDonne);
                } else {
                    throw new SQLException("Creating devoir failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public boolean update(Devoir obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE devoir SET date_pour = ?, description = ?, optionnel = ?, discord_message_id = ?, id_enseignant = ?, code_enseignement = ?, id_cours_pour = ?, id_cours_donne = ? WHERE id_devoir = ?")) {

            LocalDate datePour = obj.getDatePour();
            String description = obj.getDescription();
            Boolean optionnel = obj.isOptionnel();
            Long discordMessageId = obj.getDiscordMessageId();
            Integer idEnseignant = obj.getIdEnseignant();
            String codeEnseignement = obj.getCodeEnseignement();
            Integer idCoursPour = obj.getIdCoursPour();
            Integer idCoursDonne = obj.getIdCoursDonne();
            int id = obj.getId();

            statement.setDate(1, java.sql.Date.valueOf(datePour));
            statement.setString(2, description);
            statement.setBoolean(3, optionnel);
            if (discordMessageId != null) {
                statement.setLong(4, discordMessageId);
            } else {
                statement.setNull(4, Types.BIGINT);
            }
            statement.setInt(5, idEnseignant);
            statement.setString(6, codeEnseignement);
            if (idCoursPour != null) {
                statement.setInt(7, idCoursPour);
            } else {
                statement.setNull(7, Types.INTEGER);
            }
            statement.setInt(8, idCoursDonne);
            statement.setInt(9, id);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected < 1) {
                logger.warn("Devoir with id " + id + " not found");
                return false;
            } else if (rowsAffected > 1) {
                logger.warn("Multiple devoirs with id " + id + " found");
                return true;
            } else {
                return true;
            }
        }
    }

    @Override
    public boolean delete(Devoir obj) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM devoir WHERE id_devoir = ?")) {

            int id = obj.getId();

            statement.setInt(1, id);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected < 1) {
                logger.warn("Devoir with id " + id + " not found");
                return false;
            } else if (rowsAffected > 1) {
                logger.warn("Multiple devoirs with id " + id + " found");
                return true;
            } else {
                return true;
            }
        }
    }

    @Override
    public List<Devoir> selectAll() throws SQLException {
        List<Devoir> devoirs = new ArrayList<>();

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM devoir");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id_devoir");
                LocalDate datePour = resultSet.getDate("date_pour").toLocalDate();
                String description = resultSet.getString("description");
                Boolean optionnel = resultSet.getBoolean("optionnel");
                Long discordMessageId = resultSet.getLong("discord_message_id");
                Integer idEnseignant = resultSet.getInt("id_enseignant");
                String codeEnseignement = resultSet.getString("code_enseignement");
                Integer idCoursPour = resultSet.getInt("id_cours_pour");
                Integer idCoursDonne = resultSet.getInt("id_cours_donne");

                devoirs.add(new Devoir(id, datePour, description, optionnel, discordMessageId, idEnseignant, codeEnseignement, idCoursPour, idCoursDonne));
            }
        }

        return devoirs;
    }

    @Override
    public Devoir selectById(Integer id) throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM devoir WHERE id_devoir = ?")) {

            statement.setInt(1, id);
            statement.executeQuery();

            if (statement.getResultSet().next()) {
                LocalDate datePour = statement.getResultSet().getDate("date_pour").toLocalDate();
                String description = statement.getResultSet().getString("description");
                Boolean optionnel = statement.getResultSet().getBoolean("optionnel");
                Long discordMessageId = statement.getResultSet().getLong("discord_message_id");
                Integer idEnseignant = statement.getResultSet().getInt("id_enseignant");
                String codeEnseignement = statement.getResultSet().getString("code_enseignement");
                Integer idCoursPour = statement.getResultSet().getInt("id_cours_pour");
                Integer idCoursDonne = statement.getResultSet().getInt("id_cours_donne");

                return new Devoir(id, datePour, description, optionnel, discordMessageId, idEnseignant, codeEnseignement, idCoursPour, idCoursDonne);
            }
        }

        return null;
    }
}

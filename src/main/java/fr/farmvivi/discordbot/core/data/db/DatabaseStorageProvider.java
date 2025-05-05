package fr.farmvivi.discordbot.core.data.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.config.ConfigurationException;
import fr.farmvivi.discordbot.core.api.data.DataStorageProvider;
import fr.farmvivi.discordbot.core.api.data.GuildStorage;
import fr.farmvivi.discordbot.core.api.data.UserGuildStorage;
import fr.farmvivi.discordbot.core.api.data.UserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Implémentation du fournisseur de stockage basée sur une base de données.
 * Utilise HikariCP pour gérer le pool de connexions.
 */
public class DatabaseStorageProvider implements DataStorageProvider {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseStorageProvider.class);

    private final HikariDataSource dataSource;
    private final DatabaseUserStorage userStorage;
    private final DatabaseGuildStorage guildStorage;
    private final DatabaseUserGuildStorage userGuildStorage;

    /**
     * Crée un nouveau fournisseur de stockage basé sur une base de données.
     *
     * @param config la configuration
     */
    public DatabaseStorageProvider(Configuration config) {
        try {
            String jdbcUrl = config.getString("data.storage.db.url");
            String username = config.getString("data.storage.db.username");
            String password = config.getString("data.storage.db.password");

            // Configuration du pool de connexions HikariCP
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(jdbcUrl);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);

            // Configuration optimale pour la plupart des cas
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setIdleTimeout(30000);
            hikariConfig.setMaxLifetime(1800000);
            hikariConfig.setConnectionTimeout(30000);
            hikariConfig.setAutoCommit(true);

            // Options additionnelles de la configuration
            try {
                int maxPoolSize = config.getInt("data.storage.db.max_pool_size");
                hikariConfig.setMaximumPoolSize(maxPoolSize);
            } catch (ConfigurationException e) {
                // Utiliser la valeur par défaut
            }

            try {
                boolean autoCommit = config.getBoolean("data.storage.db.auto_commit");
                hikariConfig.setAutoCommit(autoCommit);
            } catch (ConfigurationException e) {
                // Utiliser la valeur par défaut
            }

            // Création du pool de connexions
            this.dataSource = new HikariDataSource(hikariConfig);

            // Créer les tables si elles n'existent pas
            createTablesIfNotExist();

            // Initialiser les stockages
            this.userStorage = new DatabaseUserStorage(this);
            this.guildStorage = new DatabaseGuildStorage(this);
            this.userGuildStorage = new DatabaseUserGuildStorage(this);

            logger.info("Initialized remote database storage at {}", jdbcUrl);
        } catch (ConfigurationException e) {
            logger.error("Missing or invalid database configuration", e);
            throw new RuntimeException("Missing or invalid database configuration", e);
        } catch (SQLException e) {
            logger.error("Failed to initialize database connection", e);
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    /**
     * Récupère une connexion depuis le pool.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Crée les tables si elles n'existent pas.
     */
    private void createTablesIfNotExist() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Table pour les données utilisateur
            stmt.execute("CREATE TABLE IF NOT EXISTS user_data ("
                    + "user_id VARCHAR(255) NOT NULL, "
                    + "data_key VARCHAR(255) NOT NULL, "
                    + "data_value LONGTEXT, "
                    + "PRIMARY KEY (user_id, data_key))");

            // Table pour les données guild
            stmt.execute("CREATE TABLE IF NOT EXISTS guild_data ("
                    + "guild_id VARCHAR(255) NOT NULL, "
                    + "data_key VARCHAR(255) NOT NULL, "
                    + "data_value LONGTEXT, "
                    + "PRIMARY KEY (guild_id, data_key))");

            // Table pour les données utilisateur-guild
            stmt.execute("CREATE TABLE IF NOT EXISTS user_guild_data ("
                    + "user_id VARCHAR(255) NOT NULL, "
                    + "guild_id VARCHAR(255) NOT NULL, "
                    + "data_key VARCHAR(255) NOT NULL, "
                    + "data_value LONGTEXT, "
                    + "PRIMARY KEY (user_id, guild_id, data_key))");

            // Créer des index pour améliorer les performances
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_user_data_user_id ON user_data (user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_guild_data_guild_id ON guild_data (guild_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_user_guild_data_user_id ON user_guild_data (user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_user_guild_data_guild_id ON user_guild_data (guild_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_user_guild_data_user_guild ON user_guild_data (user_id, guild_id)");
        }
    }

    @Override
    public UserStorage getUserStorage() {
        return userStorage;
    }

    @Override
    public GuildStorage getGuildStorage() {
        return guildStorage;
    }

    @Override
    public UserGuildStorage getUserGuildStorage() {
        return userGuildStorage;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.DB;
    }

    @Override
    public boolean saveAll() {
        // La sauvegarde est immédiate dans une base de données
        return true;
    }

    @Override
    public boolean close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            return true;
        }
        return true;
    }
}
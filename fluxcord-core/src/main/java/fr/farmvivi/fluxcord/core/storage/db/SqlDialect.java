package fr.farmvivi.fluxcord.core.storage.db;

import java.util.Locale;

/**
 * SQL dialects supported by {@link DatabaseDataStorage}.
 * <p>
 * The dialect is detected from the JDBC URL and drives the few database-specific
 * SQL fragments used by the storage: the column type used to store JSON values
 * and the upsert (insert-or-update) statement.
 * <p>
 * Every {@link #upsertStatement()} exposes exactly three positional parameters
 * ({@code scope}, {@code key_name}, {@code value_data}), so callers never have to
 * branch on the dialect when binding parameters.
 */
enum SqlDialect {
    /**
     * MySQL / MariaDB family.
     */
    MYSQL("LONGTEXT",
            "INSERT INTO %s (scope, key_name, value_data) VALUES (?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE value_data = VALUES(value_data)"),

    /**
     * PostgreSQL.
     */
    POSTGRESQL("TEXT",
            "INSERT INTO %s (scope, key_name, value_data) VALUES (?, ?, ?) "
                    + "ON CONFLICT (scope, key_name) DO UPDATE SET value_data = EXCLUDED.value_data");

    private final String textColumnType;
    private final String upsertTemplate;

    SqlDialect(String textColumnType, String upsertTemplate) {
        this.textColumnType = textColumnType;
        this.upsertTemplate = upsertTemplate;
    }

    /**
     * Detects the dialect from a JDBC URL. Unrecognized drivers default to the
     * {@link #MYSQL} family, preserving the historical behavior of the storage.
     *
     * @param jdbcUrl the JDBC connection URL (e.g. {@code jdbc:postgresql://host/db})
     * @return the detected dialect
     */
    static SqlDialect fromJdbcUrl(String jdbcUrl) {
        String url = jdbcUrl == null ? "" : jdbcUrl.toLowerCase(Locale.ROOT);
        if (url.startsWith("jdbc:postgresql:") || url.startsWith("jdbc:postgres:")) {
            return POSTGRESQL;
        }
        return MYSQL;
    }

    /**
     * @return the column type used to store JSON payloads (e.g. {@code LONGTEXT} or {@code TEXT})
     */
    String textColumnType() {
        return textColumnType;
    }

    /**
     * Builds an upsert statement for the given table with three positional parameters:
     * scope, key_name, value_data.
     *
     * @param tableName the (already validated/sanitized) table name to target
     * @return the dialect-specific upsert statement
     */
    String upsertStatement(String tableName) {
        return String.format(upsertTemplate, tableName);
    }
}
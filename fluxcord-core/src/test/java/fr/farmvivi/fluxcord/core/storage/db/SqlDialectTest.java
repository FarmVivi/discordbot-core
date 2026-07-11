package fr.farmvivi.fluxcord.core.storage.db;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link SqlDialect} JDBC URL detection and dialect-specific SQL.
 */
class SqlDialectTest {

    @Test
    void detectsPostgreSql() {
        assertEquals(SqlDialect.POSTGRESQL,
                SqlDialect.fromJdbcUrl("jdbc:postgresql://localhost:5432/fluxcord"));
        assertEquals(SqlDialect.POSTGRESQL,
                SqlDialect.fromJdbcUrl("JDBC:POSTGRESQL://localhost/fluxcord"));
    }

    @Test
    void detectsMySqlAndMariaDb() {
        assertEquals(SqlDialect.MYSQL,
                SqlDialect.fromJdbcUrl("jdbc:mysql://localhost:3306/fluxcord"));
        assertEquals(SqlDialect.MYSQL,
                SqlDialect.fromJdbcUrl("jdbc:mariadb://localhost:3306/fluxcord"));
    }

    @Test
    void defaultsToMySqlForUnknownOrNullUrl() {
        assertEquals(SqlDialect.MYSQL, SqlDialect.fromJdbcUrl("jdbc:h2:mem:test"));
        assertEquals(SqlDialect.MYSQL, SqlDialect.fromJdbcUrl(null));
    }

    @Test
    void upsertStatementsUseThreePositionalParameters() {
        for (SqlDialect dialect : SqlDialect.values()) {
            long placeholders = dialect.upsertStatement("storage_data").chars().filter(c -> c == '?').count();
            assertEquals(3, placeholders,
                    () -> dialect + " upsert must expose exactly three positional parameters");
        }
    }

    @Test
    void dialectsProvideDistinctColumnTypesAndUpserts() {
        assertEquals("LONGTEXT", SqlDialect.MYSQL.textColumnType());
        assertEquals("TEXT", SqlDialect.POSTGRESQL.textColumnType());
        assertTrue(SqlDialect.MYSQL.upsertStatement("storage_data").contains("ON DUPLICATE KEY UPDATE"));
        assertTrue(SqlDialect.POSTGRESQL.upsertStatement("storage_data").contains("ON CONFLICT"));
    }

    @Test
    void upsertStatementUsesGivenTableName() {
        for (SqlDialect dialect : SqlDialect.values()) {
            assertTrue(dialect.upsertStatement("bot1_storage_data").contains("INTO bot1_storage_data "),
                    () -> dialect + " upsert must target the provided table name");
        }
    }
}
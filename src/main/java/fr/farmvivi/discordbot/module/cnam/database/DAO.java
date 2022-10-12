package fr.farmvivi.discordbot.module.cnam.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public abstract class DAO<T, I> {
    protected final String tableName;
    protected final Logger logger;
    protected final DatabaseAccess db;

    public DAO(String tableName, DatabaseAccess db) {
        this.tableName = tableName;
        this.logger = LoggerFactory.getLogger(tableName + "DAO");
        this.db = db;
    }

    public abstract T create(T obj) throws SQLException;

    public abstract List<T> selectAll() throws SQLException;

    public abstract T selectById(I id) throws SQLException;

    public abstract boolean update(T obj) throws SQLException;

    public abstract boolean delete(T obj) throws SQLException;
}

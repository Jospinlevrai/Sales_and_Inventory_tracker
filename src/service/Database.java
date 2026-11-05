package service;

import exception.DataAccessException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Opens connections to PostgreSQL and remembers the connection of the transaction running on this thread. */
public class Database {
    /** A piece of JDBC work that is given a connection. */
    interface Work<T> {
        T apply(Connection connection) throws SQLException;
    }

    private final String url;
    private final String user;
    private final String password;
    private final ThreadLocal<Connection> transaction = new ThreadLocal<>();

    public Database(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /** Reads db.url, db.user and db.password from a properties file. */
    public static Database fromFile(String path) {
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(path)) {
            properties.load(in);
        } catch (IOException e) {
            throw new DataAccessException("Cannot read the database settings from " + path, e);
        }
        String url = properties.getProperty("db.url");
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("db.url is missing in " + path);
        }
        return new Database(url.trim(), properties.getProperty("db.user"), properties.getProperty("db.password"));
    }

    /** Runs the work on the running transaction's connection, or on a short-lived connection when there is none. */
    <T> T withConnection(Work<T> work) {
        Connection running = transaction.get();
        try {
            if (running != null) {
                return work.apply(running);
            }
            try (Connection connection = open()) {
                return work.apply(connection);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error: " + e.getMessage(), e);
        }
    }

    boolean inTransaction() {
        return transaction.get() != null;
    }

    void begin() throws SQLException {
        Connection connection = open();
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
        transaction.set(connection);
    }

    void commit() throws SQLException {
        transaction.get().commit();
    }

    void rollback() {
        Connection connection = transaction.get();
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
                // the connection is closed right after, which also discards the changes
            }
        }
    }

    void end() {
        Connection connection = transaction.get();
        transaction.remove();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // nothing more can be done
            }
        }
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}

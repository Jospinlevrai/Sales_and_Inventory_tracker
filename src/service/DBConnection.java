package service;

import exception.DataAccessException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    /** A piece of JDBC work that is given a connection. */
    interface Work<T> {
        T apply(Connection connection) throws SQLException;
    }

    private final String url;
    private final String user;
    private final String password;
    private final ThreadLocal<Connection> transaction = new ThreadLocal<>();

    /** Uses these default settings. Edit them, or build the object with DBConnection.fromFile("db.properties"). */
    public DBConnection() {
        this("jdbc:postgresql://localhost:5432/Inventory_tracker", "DB_USER", "DB_PASSWORD");
    }

    public DBConnection(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /** Reads db.url, db.user and db.password from a properties file, so passwords stay out of the code. */
    public static DBConnection fromFile(String path) {
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
        return new DBConnection(url.trim(), properties.getProperty("db.user"), properties.getProperty("db.password"));
    }

    /** Opens a new connection; the caller closes it. Throws DataAccessException instead of returning null. */
    public Connection getConnection() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new DataAccessException("PostgreSQL driver not found: add the postgresql jar to the project", e);
        }
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DataAccessException("Database connection failed: " + e.getMessage(), e);
        }
    }

    /** Runs the work on the running transaction's connection, or on a short-lived connection when there is none. */
    <T> T withConnection(Work<T> work) {
        Connection running = transaction.get();
        try {
            if (running != null) {
                return work.apply(running);
            }
            try (Connection connection = getConnection()) {
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
        Connection connection = getConnection();
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
}

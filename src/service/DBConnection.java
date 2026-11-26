package service;

import exception.DataAccessException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    interface Work<T> {
        T apply(Connection connection) throws SQLException;
    }

    private final String url;
    private final String user;
    private final String password;

    private final ThreadLocal<Connection> transaction = new ThreadLocal<>();

    // Default PostgreSQL connection settings
    public DBConnection() {
        this(
            "jdbc:postgresql://localhost:5432/Inventory_tracker",
            "postgres",
            "YOUR_POSTGRES_PASSWORD"
        );
    }

    // Constructor
    public DBConnection(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    // Load database settings from db.properties
    public static DBConnection fromFile(String path) {

        Properties properties = new Properties();
        File file = new File(path);

        try (InputStream in = new FileInputStream(file)) {
            properties.load(in);

        } catch (IOException e) {
            throw new DataAccessException(
                "Cannot read database settings from: "
                + file.getAbsolutePath(), e
            );
        }

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("db.url is missing in " + path);
        }

        if (user == null || user.trim().isEmpty()) {
            throw new IllegalArgumentException("db.user is missing in " + path);
        }

        if (password == null) {
            throw new IllegalArgumentException("db.password is missing in " + path);
        }

        return new DBConnection(
            url.trim(),
            user.trim(),
            password
        );
    }

    // Establish PostgreSQL connection
    public Connection getConnection() {

        try {
            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException e) {
            throw new DataAccessException(
                "PostgreSQL JDBC driver not found. Add the PostgreSQL JAR to your project.", e
            );
        }

        try {
            return DriverManager.getConnection(url, user, password);

        } catch (SQLException e) {
            throw new DataAccessException(
                "Database connection failed: " + e.getMessage(), e
            );
        }
    }

    // Execute database work using an existing or temporary connection
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
            throw new DataAccessException(
                "Database error: " + e.getMessage(), e
            );
        }
    }

    // Check whether a transaction is active
    boolean inTransaction() {
        return transaction.get() != null;
    }

    // Begin transaction
    void begin() throws SQLException {

        Connection connection = getConnection();

        try {
            connection.setAutoCommit(false);
            transaction.set(connection);

        } catch (SQLException e) {
            connection.close();
            throw e;
        }
    }

    // Commit transaction
    void commit() throws SQLException {

        Connection connection = transaction.get();

        if (connection == null) {
            throw new SQLException("No active transaction to commit.");
        }

        connection.commit();
    }

    // Roll back transaction
    void rollback() {

        Connection connection = transaction.get();

        if (connection != null) {
            try {
                connection.rollback();

            } catch (SQLException ignored) {
                // Connection will be closed in end()
            }
        }
    }

    // End transaction and close connection
    void end() {

        Connection connection = transaction.get();

        transaction.remove();

        if (connection != null) {
            try {
                connection.close();

            } catch (SQLException ignored) {
                // Connection already closed or unavailable
            }
        }
    }

    String getUrl() {
        return url;
    }

    String getUser() {
        return user;
    }

    String getPassword() {
        return password;
    }
}
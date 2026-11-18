package service;

import model.User;
import service.DBConnection;
import service.UserRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {
    private final DBConnection database;

    public JdbcUserRepository(DBConnection database) {
        this.database = database;
    }

    @Override
    public Optional<User> findById(int userId) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Users WHERE user_id = ?")) {
                statement.setInt(1, userId);
                return one(statement);
            }
        });
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Users WHERE username = ?")) {
                statement.setString(1, username);
                return one(statement);
            }
        });
    }

    @Override
    public List<User> findAll() {
        return database.withConnection(connection -> {
            List<User> users = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Users ORDER BY username");
                 ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    users.add(map(rows));
                }
            }
            return users;
        });
    }

    @Override
    public long count() {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM Users");
                 ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getLong(1);
            }
        });
    }

    @Override
    public User save(User user) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Users(role_id, username, password_hash, full_name, email, phone_number, is_active, created_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING user_id")) {
                statement.setInt(1, user.getRoleId());
                statement.setString(2, user.getUsername());
                statement.setString(3, user.getPasswordHash());
                statement.setString(4, user.getFullName());
                statement.setString(5, user.getEmail());
                statement.setString(6, user.getPhoneNumber());
                statement.setBoolean(7, user.isActive());
                statement.setObject(8, user.getCreatedAt());
                try (ResultSet generated = statement.executeQuery()) {
                    generated.next();
                    user.setUserId(generated.getInt("user_id"));
                }
                return user;
            }
        });
    }

    @Override
    public void update(User user) {
        database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE Users SET role_id = ?, full_name = ?, email = ?, phone_number = ? WHERE user_id = ?")) {
                statement.setInt(1, user.getRoleId());
                statement.setString(2, user.getFullName());
                statement.setString(3, user.getEmail());
                statement.setString(4, user.getPhoneNumber());
                statement.setInt(5, user.getUserId());
                return statement.executeUpdate();
            }
        });
    }

    @Override
    public void updatePasswordHash(int userId, String passwordHash) {
        database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE Users SET password_hash = ? WHERE user_id = ?")) {
                statement.setString(1, passwordHash);
                statement.setInt(2, userId);
                return statement.executeUpdate();
            }
        });
    }

    @Override
    public void setActive(int userId, boolean active) {
        database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE Users SET is_active = ? WHERE user_id = ?")) {
                statement.setBoolean(1, active);
                statement.setInt(2, userId);
                return statement.executeUpdate();
            }
        });
    }

    @Override
    public boolean emailExists(String email, int excludeUserId) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT 1 FROM Users WHERE LOWER(email) = LOWER(?) AND user_id <> ?")) {
                statement.setString(1, email);
                statement.setInt(2, excludeUserId);
                try (ResultSet rows = statement.executeQuery()) {
                    return rows.next();
                }
            }
        });
    }

    @Override
    public int countActiveWithRole(int roleId) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM Users WHERE role_id = ? AND is_active")) {
                statement.setInt(1, roleId);
                try (ResultSet rows = statement.executeQuery()) {
                    rows.next();
                    return rows.getInt(1);
                }
            }
        });
    }

    private static Optional<User> one(PreparedStatement statement) throws SQLException {
        try (ResultSet rows = statement.executeQuery()) {
            if (rows.next()) {
                return Optional.of(map(rows));
            }
            return Optional.empty();
        }
    }

    private static User map(ResultSet rows) throws SQLException {
        return new User(rows.getInt("user_id"), rows.getInt("role_id"), rows.getString("username"),
                rows.getString("password_hash"), rows.getString("full_name"), rows.getString("email"),
                rows.getString("phone_number"), rows.getBoolean("is_active"),
                rows.getObject("created_at", LocalDateTime.class));
    }
}

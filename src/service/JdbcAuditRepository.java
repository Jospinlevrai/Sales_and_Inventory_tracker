package service;

import model.AuditEntry;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcAuditRepository implements AuditRepository {
    private final DBConnection database;

    public JdbcAuditRepository(DBConnection database) {
        this.database = database;
        ensureTable();
    }

    private void ensureTable() {
        database.withConnection(connection -> {
            try (java.sql.Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS Audit_Log ("
                        + "audit_id BIGSERIAL PRIMARY KEY, user_id INTEGER, username VARCHAR(50), "
                        + "action VARCHAR(100) NOT NULL, entity_type VARCHAR(100), entity_id VARCHAR(100), "
                        + "details VARCHAR(2000), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            }
            return null;
        });
    }

    @Override
    public AuditEntry save(AuditEntry entry) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Audit_Log(user_id, username, action, entity_type, entity_id, details, created_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING audit_id, created_at")) {
                statement.setObject(1, entry.getUserId(), Types.INTEGER);
                statement.setString(2, entry.getUsername());
                statement.setString(3, entry.getAction());
                statement.setString(4, entry.getEntityType());
                statement.setString(5, entry.getEntityId());
                statement.setString(6, entry.getDetails());
                statement.setObject(7, entry.getCreatedAt() == null ? LocalDateTime.now() : entry.getCreatedAt());
                try (ResultSet rows = statement.executeQuery()) {
                    rows.next();
                    entry.setAuditId(rows.getLong("audit_id"));
                    entry.setCreatedAt(rows.getObject("created_at", LocalDateTime.class));
                }
                return entry;
            }
        });
    }

    @Override
    public List<AuditEntry> findRecent(int limit) {
        return database.withConnection(connection -> {
            List<AuditEntry> entries = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM Audit_Log ORDER BY created_at DESC, audit_id DESC LIMIT ?")) {
                statement.setInt(1, limit);
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        entries.add(map(rows));
                    }
                }
            }
            return entries;
        });
    }

    private static AuditEntry map(ResultSet rows) throws SQLException {
        int userId = rows.getInt("user_id");
        Integer optionalUserId = rows.wasNull() ? null : userId;
        return new AuditEntry(rows.getLong("audit_id"), optionalUserId, rows.getString("username"),
                rows.getString("action"), rows.getString("entity_type"), rows.getString("entity_id"),
                rows.getString("details"), rows.getObject("created_at", LocalDateTime.class));
    }
}

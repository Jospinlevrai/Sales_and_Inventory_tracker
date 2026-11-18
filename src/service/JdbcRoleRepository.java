package service;

import model.Role;
import service.DBConnection;
import service.RoleRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcRoleRepository implements RoleRepository {
    private final DBConnection database;

    public JdbcRoleRepository(DBConnection database) {
        this.database = database;
    }

    @Override
    public Optional<Role> findById(int roleId) {
        return findOne("SELECT * FROM Roles WHERE role_id = ?", roleId, null);
    }

    @Override
    public Optional<Role> findByName(String roleName) {
        return findOne("SELECT * FROM Roles WHERE role_name = ?", 0, roleName);
    }

    @Override
    public List<Role> findAll() {
        return database.withConnection(connection -> {
            List<Role> roles = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Roles ORDER BY role_id");
                 ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    roles.add(map(rows));
                }
            }
            return roles;
        });
    }

    @Override
    public Role save(Role role) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Roles(role_name) VALUES (?) RETURNING role_id")) {
                statement.setString(1, role.getRoleName());
                try (ResultSet generated = statement.executeQuery()) {
                    generated.next();
                    role.setRoleId(generated.getInt("role_id"));
                }
                return role;
            }
        });
    }

    private Optional<Role> findOne(String sql, int id, String name) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (name == null) {
                    statement.setInt(1, id);
                } else {
                    statement.setString(1, name);
                }
                try (ResultSet rows = statement.executeQuery()) {
                    if (rows.next()) {
                        return Optional.of(map(rows));
                    }
                    return Optional.empty();
                }
            }
        });
    }

    private static Role map(ResultSet rows) throws SQLException {
        return new Role(rows.getInt("role_id"), rows.getString("role_name"));
    }
}

package service;

import model.Supplier;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSupplierRepository implements SupplierRepository, SupplierCatalogRepository {
    private final DBConnection database;

    public JdbcSupplierRepository(DBConnection database) {
        this.database = database;
    }

    @Override
    public Optional<Supplier> findById(int supplierId) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Suppliers WHERE supplier_id = ?")) {
                statement.setInt(1, supplierId);
                try (ResultSet rows = statement.executeQuery()) {
                    if (rows.next()) {
                        return Optional.of(map(rows));
                    }
                    return Optional.empty();
                }
            }
        });
    }

    @Override
    public List<Supplier> findAll(boolean activeOnly) {
        return database.withConnection(connection -> {
            List<Supplier> suppliers = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM Suppliers WHERE (? = FALSE OR is_active) ORDER BY supplier_name")) {
                statement.setBoolean(1, activeOnly);
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        suppliers.add(map(rows));
                    }
                }
            }
            return suppliers;
        });
    }

    @Override
    public Supplier save(Supplier supplier) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Suppliers(supplier_name, contact_person, phone_number, email, address, is_active, created_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING supplier_id")) {
                statement.setString(1, supplier.getSupplierName());
                statement.setString(2, supplier.getContactPerson());
                statement.setString(3, supplier.getPhoneNumber());
                statement.setString(4, supplier.getEmail());
                statement.setString(5, supplier.getAddress());
                statement.setBoolean(6, supplier.isActive());
                statement.setObject(7, supplier.getCreatedAt());
                try (ResultSet generated = statement.executeQuery()) {
                    generated.next();
                    supplier.setSupplierId(generated.getInt("supplier_id"));
                }
                return supplier;
            }
        });
    }

    @Override
    public void update(Supplier supplier) {
        database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE Suppliers SET supplier_name = ?, contact_person = ?, phone_number = ?, email = ?, address = ? "
                            + "WHERE supplier_id = ?")) {
                statement.setString(1, supplier.getSupplierName());
                statement.setString(2, supplier.getContactPerson());
                statement.setString(3, supplier.getPhoneNumber());
                statement.setString(4, supplier.getEmail());
                statement.setString(5, supplier.getAddress());
                statement.setInt(6, supplier.getSupplierId());
                return statement.executeUpdate();
            }
        });
    }

    @Override
    public void setActive(int supplierId, boolean active) {
        database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE Suppliers SET is_active = ? WHERE supplier_id = ?")) {
                statement.setBoolean(1, active);
                statement.setInt(2, supplierId);
                return statement.executeUpdate();
            }
        });
    }

    @Override
    public boolean nameExists(String name, int excludeSupplierId) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT 1 FROM Suppliers WHERE LOWER(supplier_name) = LOWER(?) AND supplier_id <> ?")) {
                statement.setString(1, name);
                statement.setInt(2, excludeSupplierId);
                try (ResultSet rows = statement.executeQuery()) {
                    return rows.next();
                }
            }
        });
    }

    private static Supplier map(ResultSet rows) throws SQLException {
        return new Supplier(rows.getInt("supplier_id"), rows.getString("supplier_name"),
                rows.getString("contact_person"), rows.getString("phone_number"), rows.getString("email"),
                rows.getString("address"), rows.getBoolean("is_active"),
                rows.getObject("created_at", LocalDateTime.class));
    }
}
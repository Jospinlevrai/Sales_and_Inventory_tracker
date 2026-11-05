package service;

import model.Supplier;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class JdbcSupplierRepository implements SupplierRepository {
    private final Database database;

    public JdbcSupplierRepository(Database database) {
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

    private static Supplier map(ResultSet rows) throws SQLException {
        return new Supplier(rows.getInt("supplier_id"), rows.getString("supplier_name"),
                rows.getString("contact_person"), rows.getString("phone_number"), rows.getString("email"),
                rows.getString("address"), rows.getBoolean("is_active"),
                rows.getObject("created_at", LocalDateTime.class));
    }
}

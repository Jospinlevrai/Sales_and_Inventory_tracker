package service;

import model.Product;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProductRepository implements ProductRepository {
    private final Database database;

    public JdbcProductRepository(Database database) {
        this.database = database;
    }

    @Override
    public Optional<Product> findById(int productId) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Products WHERE product_id = ?")) {
                statement.setInt(1, productId);
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
    public List<Product> findActive() {
        return database.withConnection(connection -> {
            List<Product> products = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM Products WHERE is_active ORDER BY product_name");
                 ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    products.add(map(rows));
                }
            }
            return products;
        });
    }

    @Override
    public boolean changeStock(int productId, int delta) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE Products SET quantity_in_stock = quantity_in_stock + ?, updated_at = CURRENT_TIMESTAMP "
                            + "WHERE product_id = ? AND quantity_in_stock + ? >= 0")) {
                statement.setInt(1, delta);
                statement.setInt(2, productId);
                statement.setInt(3, delta);
                return statement.executeUpdate() == 1;
            }
        });
    }

    @Override
    public void updateCostPrice(int productId, BigDecimal costPrice) {
        database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE Products SET cost_price = ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?")) {
                statement.setBigDecimal(1, costPrice);
                statement.setInt(2, productId);
                return statement.executeUpdate();
            }
        });
    }

    private static Product map(ResultSet rows) throws SQLException {
        int supplierId = rows.getInt("supplier_id");
        Integer supplier = rows.wasNull() ? null : supplierId;
        return new Product(rows.getInt("product_id"), rows.getInt("category_id"), supplier,
                rows.getString("sku"), rows.getString("barcode"), rows.getString("product_name"),
                rows.getString("description"), rows.getString("unit"),
                rows.getBigDecimal("cost_price"), rows.getBigDecimal("selling_price"),
                rows.getInt("quantity_in_stock"), rows.getInt("reorder_level"), rows.getBoolean("is_active"),
                rows.getObject("created_at", LocalDateTime.class), rows.getObject("updated_at", LocalDateTime.class));
    }
}

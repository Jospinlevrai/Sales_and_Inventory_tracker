package service;

import model.Product;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class JdbcProductRepository implements ProductRepository, ProductCatalogRepository {
    private final DBConnection database;

    public JdbcProductRepository(DBConnection database) {
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
    public List<Product> findLowStock() {
        return database.withConnection(connection -> {
            List<Product> products = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM Products WHERE is_active AND quantity_in_stock <= reorder_level "
                            + "ORDER BY (quantity_in_stock = 0) DESC, quantity_in_stock ASC, product_name ASC");
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

    @Override
    public List<Product> findAll() {
        return database.withConnection(connection -> {
            List<Product> products = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Products ORDER BY product_name");
                 ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    products.add(map(rows));
                }
            }
            return products;
        });
    }

    @Override
    public List<Product> search(String text, Integer categoryId) {
        String pattern = "%" + escapeLike(text == null ? "" : text.trim().toLowerCase(Locale.ROOT)) + "%";
        return database.withConnection(connection -> {
            List<Product> products = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM Products WHERE is_active "
                            + "AND (CAST(? AS INTEGER) IS NULL OR category_id = CAST(? AS INTEGER)) "
                            + "AND (LOWER(product_name) LIKE ? ESCAPE '\\' OR LOWER(sku) LIKE ? ESCAPE '\\' "
                            + "OR LOWER(COALESCE(barcode, '')) LIKE ? ESCAPE '\\') ORDER BY product_name")) {
                statement.setObject(1, categoryId, Types.INTEGER);
                statement.setObject(2, categoryId, Types.INTEGER);
                statement.setString(3, pattern);
                statement.setString(4, pattern);
                statement.setString(5, pattern);
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        products.add(map(rows));
                    }
                }
            }
            return products;
        });
    }

    @Override
    public Product save(Product product) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Products(category_id, supplier_id, sku, barcode, product_name, description, unit, cost_price, "
                            + "selling_price, quantity_in_stock, reorder_level, is_active, created_at, updated_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING product_id")) {
                statement.setInt(1, product.getCategoryId());
                statement.setObject(2, product.getSupplierId(), Types.INTEGER);
                statement.setString(3, product.getSku());
                statement.setString(4, product.getBarcode());
                statement.setString(5, product.getProductName());
                statement.setString(6, product.getDescription());
                statement.setString(7, product.getUnit());
                statement.setBigDecimal(8, product.getCostPrice());
                statement.setBigDecimal(9, product.getSellingPrice());
                statement.setInt(10, product.getQuantityInStock());
                statement.setInt(11, product.getReorderLevel());
                statement.setBoolean(12, product.isActive());
                statement.setObject(13, product.getCreatedAt());
                statement.setObject(14, product.getUpdatedAt());
                try (ResultSet generated = statement.executeQuery()) {
                    generated.next();
                    product.setProductId(generated.getInt("product_id"));
                }
                return product;
            }
        });
    }

    @Override
    public void update(Product product) {
        database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE Products SET category_id = ?, supplier_id = ?, sku = ?, barcode = ?, product_name = ?, description = ?, "
                            + "unit = ?, cost_price = ?, selling_price = ?, reorder_level = ?, updated_at = CURRENT_TIMESTAMP "
                            + "WHERE product_id = ?")) {
                statement.setInt(1, product.getCategoryId());
                statement.setObject(2, product.getSupplierId(), Types.INTEGER);
                statement.setString(3, product.getSku());
                statement.setString(4, product.getBarcode());
                statement.setString(5, product.getProductName());
                statement.setString(6, product.getDescription());
                statement.setString(7, product.getUnit());
                statement.setBigDecimal(8, product.getCostPrice());
                statement.setBigDecimal(9, product.getSellingPrice());
                statement.setInt(10, product.getReorderLevel());
                statement.setInt(11, product.getProductId());
                return statement.executeUpdate();
            }
        });
    }

    @Override
    public void setActive(int productId, boolean active) {
        database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE Products SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?")) {
                statement.setBoolean(1, active);
                statement.setInt(2, productId);
                return statement.executeUpdate();
            }
        });
    }

    @Override
    public boolean skuExists(String sku, int excludeProductId) {
        return exists("SELECT 1 FROM Products WHERE LOWER(sku) = LOWER(?) AND product_id <> ?", sku, excludeProductId);
    }

    @Override
    public boolean barcodeExists(String barcode, int excludeProductId) {
        return exists("SELECT 1 FROM Products WHERE LOWER(barcode) = LOWER(?) AND product_id <> ?", barcode, excludeProductId);
    }

    private boolean exists(String sql, String text, int excludeId) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, text);
                statement.setInt(2, excludeId);
                try (ResultSet rows = statement.executeQuery()) {
                    return rows.next();
                }
            }
        });
    }

    private static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
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
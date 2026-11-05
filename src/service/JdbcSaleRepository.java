package service;

import model.Sale;
import model.Sale_item;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSaleRepository implements SaleRepository {
    private final Database database;

    public JdbcSaleRepository(Database database) {
        this.database = database;
    }

    @Override
    public Sale save(Sale sale) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Sales(user_id, sale_date, subtotal, discount, tax_rate, total_amount, payment_method, status, notes) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING sale_id, receipt_no")) {
                statement.setInt(1, sale.getUserId());
                statement.setObject(2, sale.getSaleDate());
                statement.setBigDecimal(3, sale.getSubtotal());
                statement.setBigDecimal(4, sale.getDiscount());
                statement.setBigDecimal(5, sale.getTaxRate());
                statement.setBigDecimal(6, sale.getTotalAmount());
                statement.setString(7, sale.getPaymentMethod().name());
                statement.setString(8, sale.getStatus().name());
                statement.setString(9, sale.getNotes());
                try (ResultSet generated = statement.executeQuery()) {
                    generated.next();
                    sale.setSaleId(generated.getInt("sale_id"));
                    sale.setReceiptNo(generated.getString("receipt_no"));
                }
                return sale;
            }
        });
    }

    @Override
    public SaleItem saveItem(SaleItem item) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Sale_Items(sale_id, product_id, quantity, unit_price, unit_cost) "
                            + "VALUES (?, ?, ?, ?, ?) RETURNING sale_item_id")) {
                statement.setInt(1, item.getSaleId());
                statement.setInt(2, item.getProductId());
                statement.setInt(3, item.getQuantity());
                statement.setBigDecimal(4, item.getUnitPrice());
                statement.setBigDecimal(5, item.getUnitCost());
                try (ResultSet generated = statement.executeQuery()) {
                    generated.next();
                    item.setSaleItemId(generated.getInt("sale_item_id"));
                }
                return item;
            }
        });
    }

    @Override
    public Optional<Sale> findById(int saleId) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Sales WHERE sale_id = ?")) {
                statement.setInt(1, saleId);
                try (ResultSet rows = statement.executeQuery()) {
                    if (rows.next()) {
                        return Optional.of(mapSale(rows));
                    }
                    return Optional.empty();
                }
            }
        });
    }

    @Override
    public List<SaleItem> findItems(int saleId) {
        return database.withConnection(connection -> {
            List<SaleItem> items = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM Sale_Items WHERE sale_id = ? ORDER BY sale_item_id")) {
                statement.setInt(1, saleId);
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        items.add(mapItem(rows));
                    }
                }
            }
            return items;
        });
    }

    @Override
    public void updateStatus(int saleId, Sale.Status status) {
        database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE Sales SET status = ? WHERE sale_id = ?")) {
                statement.setString(1, status.name());
                statement.setInt(2, saleId);
                return statement.executeUpdate();
            }
        });
    }

    private static Sale mapSale(ResultSet rows) throws SQLException {
        return new Sale(rows.getInt("sale_id"), rows.getString("receipt_no"), rows.getInt("user_id"),
                rows.getObject("sale_date", LocalDateTime.class),
                rows.getBigDecimal("subtotal"), rows.getBigDecimal("discount"),
                rows.getBigDecimal("tax_rate"), rows.getBigDecimal("total_amount"),
                Sale.PaymentMethod.valueOf(rows.getString("payment_method")),
                Sale.Status.valueOf(rows.getString("status")), rows.getString("notes"));
    }

    private static SaleItem mapItem(ResultSet rows) throws SQLException {
        return new SaleItem(rows.getInt("sale_item_id"), rows.getInt("sale_id"), rows.getInt("product_id"),
                rows.getInt("quantity"), rows.getBigDecimal("unit_price"), rows.getBigDecimal("unit_cost"));
    }
}

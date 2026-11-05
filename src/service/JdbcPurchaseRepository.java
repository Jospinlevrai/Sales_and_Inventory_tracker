package service;

import model.Purchase;
import model.PurchaseItem;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JdbcPurchaseRepository implements PurchaseRepository {
    private final Database database;

    public JdbcPurchaseRepository(Database database) {
        this.database = database;
    }

    @Override
    public Purchase save(Purchase purchase) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Purchases(supplier_id, user_id, invoice_no, purchase_date, total_cost, notes) "
                            + "VALUES (?, ?, ?, ?, ?, ?) RETURNING purchase_id")) {
                statement.setInt(1, purchase.getSupplierId());
                statement.setInt(2, purchase.getUserId());
                statement.setString(3, purchase.getInvoiceNo());
                statement.setObject(4, purchase.getPurchaseDate());
                statement.setBigDecimal(5, purchase.getTotalCost());
                statement.setString(6, purchase.getNotes());
                try (ResultSet generated = statement.executeQuery()) {
                    generated.next();
                    purchase.setPurchaseId(generated.getInt("purchase_id"));
                }
                return purchase;
            }
        });
    }

    @Override
    public PurchaseItem saveItem(PurchaseItem item) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Purchase_Items(purchase_id, product_id, quantity, unit_cost) "
                            + "VALUES (?, ?, ?, ?) RETURNING purchase_item_id")) {
                statement.setInt(1, item.getPurchaseId());
                statement.setInt(2, item.getProductId());
                statement.setInt(3, item.getQuantity());
                statement.setBigDecimal(4, item.getUnitCost());
                try (ResultSet generated = statement.executeQuery()) {
                    generated.next();
                    item.setPurchaseItemId(generated.getInt("purchase_item_id"));
                }
                return item;
            }
        });
    }

    @Override
    public boolean invoiceExists(int supplierId, String invoiceNo) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT 1 FROM Purchases WHERE supplier_id = ? AND invoice_no = ?")) {
                statement.setInt(1, supplierId);
                statement.setString(2, invoiceNo);
                try (ResultSet rows = statement.executeQuery()) {
                    return rows.next();
                }
            }
        });
    }
}

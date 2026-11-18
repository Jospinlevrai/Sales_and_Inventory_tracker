package service;

import dto.CashierSummary;
import dto.DailySales;
import dto.DashboardSummary;
import dto.MovementEntry;
import dto.PaymentTotal;
import dto.ProductSales;
import dto.RecentSale;
import model.Sale;
import model.Stock_movement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcReportRepository implements ReportRepository {
    private final DBConnection database;

    public JdbcReportRepository(DBConnection database) {
        this.database = database;
    }

    @Override
    public DashboardSummary dashboardSummary(LocalDate day) {
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.plusDays(1).atStartOfDay();
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT "
                            + "(SELECT COALESCE(SUM(total_amount), 0) FROM Sales WHERE status = 'COMPLETED' AND sale_date >= ? AND sale_date < ?) AS revenue, "
                            + "(SELECT COUNT(*) FROM Sales WHERE status = 'COMPLETED' AND sale_date >= ? AND sale_date < ?) AS transactions, "
                            + "(SELECT COALESCE(SUM(si.line_total - si.quantity * si.unit_cost), 0) FROM Sale_Items si "
                            + "   JOIN Sales s ON s.sale_id = si.sale_id "
                            + "   WHERE s.status = 'COMPLETED' AND s.sale_date >= ? AND s.sale_date < ?) "
                            + " - (SELECT COALESCE(SUM(discount), 0) FROM Sales WHERE status = 'COMPLETED' AND sale_date >= ? AND sale_date < ?) AS profit, "
                            + "(SELECT COALESCE(SUM(quantity_in_stock * cost_price), 0) FROM Products WHERE is_active) AS stock_value, "
                            + "(SELECT COUNT(*) FROM Products WHERE is_active AND quantity_in_stock <= reorder_level) AS needs_reorder, "
                            + "(SELECT COUNT(*) FROM Products WHERE is_active AND quantity_in_stock = 0) AS out_of_stock")) {
                for (int i = 1; i <= 8; i++) {
                    statement.setObject(i, i % 2 == 1 ? start : end);
                }
                try (ResultSet rows = statement.executeQuery()) {
                    rows.next();
                    return new DashboardSummary(rows.getBigDecimal("revenue"), rows.getInt("transactions"),
                            rows.getBigDecimal("profit"), rows.getBigDecimal("stock_value"),
                            rows.getInt("needs_reorder"), rows.getInt("out_of_stock"));
                }
            }
        });
    }

    @Override
    public List<DailySales> dailySales(LocalDate from, LocalDate to) {
        return database.withConnection(connection -> {
            List<DailySales> days = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT CAST(sale_date AS DATE) AS day, COUNT(*) AS transactions, SUM(total_amount) AS revenue "
                            + "FROM Sales WHERE status = 'COMPLETED' AND sale_date >= ? AND sale_date < ? "
                            + "GROUP BY CAST(sale_date AS DATE) ORDER BY day")) {
                statement.setObject(1, from.atStartOfDay());
                statement.setObject(2, to.plusDays(1).atStartOfDay());
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        days.add(new DailySales(rows.getObject("day", LocalDate.class), rows.getInt("transactions"),
                                rows.getBigDecimal("revenue")));
                    }
                }
            }
            return days;
        });
    }

    @Override
    public List<PaymentTotal> paymentTotals(LocalDate from, LocalDate to) {
        return database.withConnection(connection -> {
            List<PaymentTotal> totals = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT payment_method, COUNT(*) AS transactions, SUM(total_amount) AS amount "
                            + "FROM Sales WHERE status = 'COMPLETED' AND sale_date >= ? AND sale_date < ? "
                            + "GROUP BY payment_method ORDER BY amount DESC")) {
                statement.setObject(1, from.atStartOfDay());
                statement.setObject(2, to.plusDays(1).atStartOfDay());
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        totals.add(new PaymentTotal(Sale.PaymentMethod.valueOf(rows.getString("payment_method")),
                                rows.getInt("transactions"), rows.getBigDecimal("amount")));
                    }
                }
            }
            return totals;
        });
    }

    @Override
    public List<ProductSales> topProducts(LocalDate from, LocalDate to, int limit) {
        return database.withConnection(connection -> {
            List<ProductSales> products = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT p.product_id, p.sku, p.product_name, SUM(si.quantity) AS units, SUM(si.line_total) AS revenue, "
                            + "SUM(si.line_total - si.quantity * si.unit_cost) AS profit "
                            + "FROM Sale_Items si "
                            + "JOIN Sales s ON s.sale_id = si.sale_id AND s.status = 'COMPLETED' AND s.sale_date >= ? AND s.sale_date < ? "
                            + "JOIN Products p ON p.product_id = si.product_id "
                            + "GROUP BY p.product_id, p.sku, p.product_name ORDER BY revenue DESC, p.product_name LIMIT ?")) {
                statement.setObject(1, from.atStartOfDay());
                statement.setObject(2, to.plusDays(1).atStartOfDay());
                statement.setInt(3, limit);
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        products.add(new ProductSales(rows.getInt("product_id"), rows.getString("sku"),
                                rows.getString("product_name"), rows.getInt("units"),
                                rows.getBigDecimal("revenue"), rows.getBigDecimal("profit")));
                    }
                }
            }
            return products;
        });
    }

    @Override
    public List<RecentSale> recentSales(int limit) {
        return database.withConnection(connection -> {
            List<RecentSale> sales = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT s.sale_id, s.receipt_no, s.sale_date, u.full_name, s.payment_method, s.total_amount, s.status "
                            + "FROM Sales s JOIN Users u ON u.user_id = s.user_id "
                            + "ORDER BY s.sale_date DESC, s.sale_id DESC LIMIT ?")) {
                statement.setInt(1, limit);
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        sales.add(new RecentSale(rows.getInt("sale_id"), rows.getString("receipt_no"),
                                rows.getObject("sale_date", LocalDateTime.class), rows.getString("full_name"),
                                Sale.PaymentMethod.valueOf(rows.getString("payment_method")),
                                rows.getBigDecimal("total_amount"), Sale.Status.valueOf(rows.getString("status"))));
                    }
                }
            }
            return sales;
        });
    }

    @Override
    public List<MovementEntry> recentMovements(int limit) {
        return database.withConnection(connection -> {
            List<MovementEntry> movements = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT m.movement_id, m.created_at, p.product_name, m.movement_type, m.quantity_change, u.full_name, m.note "
                            + "FROM Stock_Movements m JOIN Products p ON p.product_id = m.product_id "
                            + "JOIN Users u ON u.user_id = m.user_id "
                            + "ORDER BY m.created_at DESC, m.movement_id DESC LIMIT ?")) {
                statement.setInt(1, limit);
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        movements.add(new MovementEntry(rows.getLong("movement_id"),
                                rows.getObject("created_at", LocalDateTime.class), rows.getString("product_name"),
                                Stock_movement.MovementType.valueOf(rows.getString("movement_type")),
                                rows.getInt("quantity_change"), rows.getString("full_name"), rows.getString("note")));
                    }
                }
            }
            return movements;
        });
    }

    @Override
    public CashierSummary cashierSummary(int userId, LocalDate day) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) AS sales_count, COALESCE(SUM(total_amount), 0) AS takings, "
                            + "COALESCE(SUM(total_amount) FILTER (WHERE payment_method = 'CASH'), 0) AS cash, "
                            + "COALESCE(SUM(total_amount) FILTER (WHERE payment_method = 'MPESA'), 0) AS mpesa, "
                            + "COALESCE(SUM(total_amount) FILTER (WHERE payment_method = 'CARD'), 0) AS card "
                            + "FROM Sales WHERE user_id = ? AND status = 'COMPLETED' AND sale_date >= ? AND sale_date < ?")) {
                statement.setInt(1, userId);
                statement.setObject(2, day.atStartOfDay());
                statement.setObject(3, day.plusDays(1).atStartOfDay());
                try (ResultSet rows = statement.executeQuery()) {
                    rows.next();
                    return new CashierSummary(userId, rows.getInt("sales_count"), rows.getBigDecimal("takings"),
                            rows.getBigDecimal("cash"), rows.getBigDecimal("mpesa"), rows.getBigDecimal("card"));
                }
            }
        });
    }
}

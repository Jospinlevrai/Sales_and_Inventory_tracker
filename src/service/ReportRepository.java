package service;

import dto.CashierSummary;
import dto.DailySales;
import dto.DashboardSummary;
import dto.MovementEntry;
import dto.PaymentTotal;
import dto.ProductSales;
import dto.RecentSale;

import java.time.LocalDate;
import java.util.List;

/** Read-only queries behind the dashboards. Voided sales never count. */
public interface ReportRepository {
    /** Sales figures for the given day, plus today's stock figures. */
    DashboardSummary dashboardSummary(LocalDate day);

    /** One row per day that had sales between the two dates (inclusive), oldest first. */
    List<DailySales> dailySales(LocalDate from, LocalDate to);

    List<PaymentTotal> paymentTotals(LocalDate from, LocalDate to);

    /** Best sellers by revenue between the two dates (inclusive). */
    List<ProductSales> topProducts(LocalDate from, LocalDate to, int limit);

    List<RecentSale> recentSales(int limit);

    List<MovementEntry> recentMovements(int limit);

    CashierSummary cashierSummary(int userId, LocalDate day);
}

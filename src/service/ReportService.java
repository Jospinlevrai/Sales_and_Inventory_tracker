package service;

import dto.CashierSummary;
import dto.DailySales;
import dto.DashboardSummary;
import dto.MovementEntry;
import dto.PaymentTotal;
import dto.ProductSales;
import dto.RecentSale;

import java.util.List;

public interface ReportService {
    /** Administrators and managers: the KPI cards of the admin dashboard. */
    DashboardSummary getDashboardSummary(int userId);

    /** Administrators and managers: revenue for each of the last N days, today last, days without sales included as zero. */
    List<DailySales> getRevenueByDay(int userId, int days);

    /** Administrators and managers. */
    List<PaymentTotal> getPaymentSplitToday(int userId);

    /** Administrators and managers: best sellers by revenue over the last N days. */
    List<ProductSales> getTopProducts(int userId, int days, int limit);

    /** Administrators and managers. */
    List<RecentSale> getRecentSales(int userId, int limit);

    /** Administrators and managers. */
    List<MovementEntry> getRecentMovements(int userId, int limit);

    /** Any active user: the user's own sales today. */
    CashierSummary getMyShiftToday(int userId);
}
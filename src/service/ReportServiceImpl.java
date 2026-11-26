package service;

import dto.CashierSummary;
import dto.DailySales;
import dto.DashboardSummary;
import dto.MovementEntry;
import dto.PaymentTotal;
import dto.ProductSales;
import dto.RecentSale;
import exception.BusinessRuleException;
import model.Role;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportServiceImpl implements ReportService {
    private final ReportRepository reports;
    private final AccessControl access;
    private final Clock clock;

    public ReportServiceImpl(ReportRepository reports, UserRepository users, RoleRepository roles) {
        this(reports, users, roles, Clock.systemDefaultZone());
    }

    /** The clock can be fixed in tests so "today" is known. */
    public ReportServiceImpl(ReportRepository reports, UserRepository users, RoleRepository roles, Clock clock) {
        this.reports = reports;
        this.access = new AccessControl(users, roles);
        this.clock = clock;
    }

    @Override
    public DashboardSummary getDashboardSummary(int userId) {
        access.require(userId, Role.ADMIN, Role.MANAGER);
        return reports.dashboardSummary(today());
    }

    @Override
    public List<DailySales> getRevenueByDay(int userId, int days) {
        access.require(userId, Role.ADMIN, Role.MANAGER);
        checkDays(days);
        LocalDate to = today();
        LocalDate from = to.minusDays(days - 1L);
        Map<LocalDate, DailySales> withSales = new HashMap<>();
        for (DailySales day : reports.dailySales(from, to)) {
            withSales.put(day.getDay(), day);
        }
        List<DailySales> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = from.plusDays(i);
            DailySales day = withSales.get(date);
            result.add(day != null ? day : new DailySales(date, 0, BigDecimal.ZERO));
        }
        return result;
    }

    @Override
    public List<PaymentTotal> getPaymentSplitToday(int userId) {
        access.require(userId, Role.ADMIN, Role.MANAGER);
        return reports.paymentTotals(today(), today());
    }

    @Override
    public List<ProductSales> getTopProducts(int userId, int days, int limit) {
        access.require(userId, Role.ADMIN, Role.MANAGER);
        checkDays(days);
        checkLimit(limit);
        LocalDate to = today();
        return reports.topProducts(to.minusDays(days - 1L), to, limit);
    }

    @Override
    public List<RecentSale> getRecentSales(int userId, int limit) {
        access.require(userId, Role.ADMIN, Role.MANAGER);
        checkLimit(limit);
        return reports.recentSales(limit);
    }

    @Override
    public List<MovementEntry> getRecentMovements(int userId, int limit) {
        access.require(userId, Role.ADMIN, Role.MANAGER);
        checkLimit(limit);
        return reports.recentMovements(limit);
    }

    @Override
    public CashierSummary getMyShiftToday(int userId) {
        access.requireActive(userId);
        return reports.cashierSummary(userId, today());
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private static void checkDays(int days) {
        if (days < 1 || days > 366) {
            throw new BusinessRuleException("Choose between 1 and 366 days");
        }
    }

    private static void checkLimit(int limit) {
        if (limit < 1 || limit > 100) {
            throw new BusinessRuleException("Choose between 1 and 100 rows");
        }
    }
}
package dto;

import java.math.BigDecimal;

public class DashboardSummary {
    private final BigDecimal revenueToday;
    private final int transactionsToday;
    private final BigDecimal grossProfitToday;
    private final BigDecimal stockValue;
    private final int needsReorder;
    private final int outOfStock;

    public DashboardSummary(BigDecimal revenueToday,
                            int transactionsToday,
                            BigDecimal grossProfitToday,
                            BigDecimal stockValue,
                            int needsReorder,
                            int outOfStock) {
        this.revenueToday = revenueToday;
        this.transactionsToday = transactionsToday;
        this.grossProfitToday = grossProfitToday;
        this.stockValue = stockValue;
        this.needsReorder = needsReorder;
        this.outOfStock = outOfStock;
    }

    public BigDecimal getRevenueToday() { return revenueToday; }
    public int getTransactionsToday() { return transactionsToday; }
    public BigDecimal getGrossProfitToday() { return grossProfitToday; }
    public BigDecimal getStockValue() { return stockValue; }
    public int getNeedsReorder() { return needsReorder; }
    public int getOutOfStock() { return outOfStock; }
}

package dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailySales {
    private final LocalDate day;
    private final int transactions;
    private final BigDecimal revenue;

    public DailySales(LocalDate day, int transactions, BigDecimal revenue) {
        this.day = day;
        this.transactions = transactions;
        this.revenue = revenue;
    }

    public LocalDate getDay() { return day; }
    public int getTransactions() { return transactions; }
    public BigDecimal getRevenue() { return revenue; }
}

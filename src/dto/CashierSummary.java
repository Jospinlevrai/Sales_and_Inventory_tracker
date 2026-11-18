package dto;

import java.math.BigDecimal;

public class CashierSummary {
    private final int userId;
    private final int salesCount;
    private final BigDecimal takings;
    private final BigDecimal cash;
    private final BigDecimal mpesa;
    private final BigDecimal card;

    public CashierSummary(int userId,
                          int salesCount,
                          BigDecimal takings,
                          BigDecimal cash,
                          BigDecimal mpesa,
                          BigDecimal card) {
        this.userId = userId;
        this.salesCount = salesCount;
        this.takings = takings;
        this.cash = cash;
        this.mpesa = mpesa;
        this.card = card;
    }

    public int getUserId() { return userId; }
    public int getSalesCount() { return salesCount; }
    public BigDecimal getTakings() { return takings; }
    public BigDecimal getCash() { return cash; }
    public BigDecimal getMpesa() { return mpesa; }
    public BigDecimal getCard() { return card; }
}

package dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class CashierSalesStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int completedSalesCount;
    private final int voidedSalesCount;
    private final BigDecimal totalRevenue;
    private final BigDecimal cashTotal;
    private final BigDecimal mpesaTotal;
    private final BigDecimal cardTotal;
    private final BigDecimal averageBasketValue;
    private final List<CashierSaleRow> recentSales;

    public CashierSalesStats(int completedSalesCount, int voidedSalesCount,
                             BigDecimal totalRevenue, BigDecimal cashTotal,
                             BigDecimal mpesaTotal, BigDecimal cardTotal,
                             BigDecimal averageBasketValue, List<CashierSaleRow> recentSales) {
        this.completedSalesCount = completedSalesCount;
        this.voidedSalesCount = voidedSalesCount;
        this.totalRevenue = totalRevenue;
        this.cashTotal = cashTotal;
        this.mpesaTotal = mpesaTotal;
        this.cardTotal = cardTotal;
        this.averageBasketValue = averageBasketValue;
        this.recentSales = recentSales;
    }

    public int getCompletedSalesCount() { return completedSalesCount; }
    public int getVoidedSalesCount() { return voidedSalesCount; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public BigDecimal getCashTotal() { return cashTotal; }
    public BigDecimal getMpesaTotal() { return mpesaTotal; }
    public BigDecimal getCardTotal() { return cardTotal; }
    public BigDecimal getAverageBasketValue() { return averageBasketValue; }
    public List<CashierSaleRow> getRecentSales() { return recentSales; }
}

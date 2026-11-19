package dto;

import java.math.BigDecimal;
import java.io.Serializable;

public class ProductSales  implements Serializable{
    
    private static final long serialVersionUID = 1L;
    private final int productId;
    private final String sku;
    private final String productName;
    private final int unitsSold;
    private final BigDecimal revenue;
    private final BigDecimal grossProfit;

    public ProductSales(int productId,
                        String sku,
                        String productName,
                        int unitsSold,
                        BigDecimal revenue,
                        BigDecimal grossProfit) {
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.unitsSold = unitsSold;
        this.revenue = revenue;
        this.grossProfit = grossProfit;
    }

    public int getProductId() { return productId; }
    public String getSku() { return sku; }
    public String getProductName() { return productName; }
    public int getUnitsSold() { return unitsSold; }
    public BigDecimal getRevenue() { return revenue; }
    public BigDecimal getGrossProfit() { return grossProfit; }
}

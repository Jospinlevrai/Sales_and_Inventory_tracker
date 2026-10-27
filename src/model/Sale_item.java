package model;

import java.math.BigDecimal;

public class Sale_item {
    private int saleItemId;
    private int saleId;
    private int productId;
    private int quantity;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal unitCost = BigDecimal.ZERO;

    public Sale_item() {
    }

    public Sale_item(int saleItemId, int saleId, int productId, int quantity, BigDecimal unitPrice,
                     BigDecimal unitCost) {
        this.saleItemId = saleItemId;
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unitCost = unitCost;
    }

    public Sale_item(int productId, int quantity, BigDecimal unitPrice, BigDecimal unitCost) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unitCost = unitCost;
    }

    public int getSaleItemId() { return saleItemId; }
    public void setSaleItemId(int saleItemId) { this.saleItemId = saleItemId; }

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return "SaleItem{saleItemId=" + saleItemId + ", saleId=" + saleId + ", productId="
                + productId + ", quantity=" + quantity + ", unitPrice=" + unitPrice + ", lineTotal="
                + getLineTotal() + "}";
    }
}
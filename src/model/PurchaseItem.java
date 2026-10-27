package model;

import java.math.BigDecimal;

public class PurchaseItem {
    private int purchaseItemId;
    private int purchaseId;
    private int productId;
    private int quantity;
    private BigDecimal unitCost = BigDecimal.ZERO;

    public PurchaseItem() {
    }

    public PurchaseItem(int purchaseItemId, int purchaseId, int productId, int quantity,
                        BigDecimal unitCost) {
        this.purchaseItemId = purchaseItemId;
        this.purchaseId = purchaseId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitCost = unitCost;
    }

    public PurchaseItem(int productId, int quantity, BigDecimal unitCost) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitCost = unitCost;
    }

    public int getPurchaseItemId() { return purchaseItemId; }
    public void setPurchaseItemId(int purchaseItemId) { this.purchaseItemId = purchaseItemId; }

    public int getPurchaseId() { return purchaseId; }
    public void setPurchaseId(int purchaseId) { this.purchaseId = purchaseId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getLineTotal() {
        return unitCost.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return "PurchaseItem{purchaseItemId=" + purchaseItemId + ", purchaseId=" + purchaseId
                + ", productId=" + productId + ", quantity=" + quantity + ", unitCost=" + unitCost
                + ", lineTotal=" + getLineTotal() + "}";
    }
}
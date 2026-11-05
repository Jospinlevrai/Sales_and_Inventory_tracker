package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private int productId;
    private int categoryId;
    private Integer supplierId;
    private String sku;
    private String barcode;
    private String productName;
    private String description;
    private String unit = "pcs";
    private BigDecimal costPrice = BigDecimal.ZERO;
    private BigDecimal sellingPrice = BigDecimal.ZERO;
    private int quantityInStock;
    private int reorderLevel = 5;
    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Product() {
    }

    public Product(int productId, int categoryId, Integer supplierId, String sku, String barcode,
                   String productName, String description, String unit, BigDecimal costPrice,
                   BigDecimal sellingPrice, int quantityInStock, int reorderLevel, boolean active,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.sku = sku;
        this.barcode = barcode;
        this.productName = productName;
        this.description = description;
        this.unit = unit;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.quantityInStock = quantityInStock;
        this.reorderLevel = reorderLevel;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public int getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isLowStock() {
        return quantityInStock <= reorderLevel;
    }

    public boolean isOutOfStock() {
        return quantityInStock == 0;
    }

    @Override
    public String toString() {
        return "Product{productId=" + productId + ", sku=" + sku + ", productName=" + productName
                + ", sellingPrice=" + sellingPrice + ", quantityInStock=" + quantityInStock + "}";
    }
}
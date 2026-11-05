package model;

import java.time.LocalDateTime;

public class Stock_movement {
    public enum MovementType {
        PURCHASE, SALE, SALE_VOID, ADJUSTMENT
    }

    private long movementId;
    private int productId;
    private int userId;
    private MovementType movementType;
    private int quantityChange;
    private Integer saleId;
    private Integer purchaseId;
    private String note;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Stock_movement() {
    }

    public Stock_movement(long movementId, int productId, int userId, MovementType movementType,
                         int quantityChange, Integer saleId, Integer purchaseId, String note,
                         LocalDateTime createdAt) {
        this.movementId = movementId;
        this.productId = productId;
        this.userId = userId;
        this.movementType = movementType;
        this.quantityChange = quantityChange;
        this.saleId = saleId;
        this.purchaseId = purchaseId;
        this.note = note;
        this.createdAt = createdAt;
    }

    public long getMovementId() { return movementId; }
    public void setMovementId(long movementId) { this.movementId = movementId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public MovementType getMovementType() { return movementType; }
    public void setMovementType(MovementType movementType) { this.movementType = movementType; }

    public int getQuantityChange() { return quantityChange; }
    public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }

    public Integer getSaleId() { return saleId; }
    public void setSaleId(Integer saleId) { this.saleId = saleId; }

    public Integer getPurchaseId() { return purchaseId; }
    public void setPurchaseId(Integer purchaseId) { this.purchaseId = purchaseId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "StockMovement{movementId=" + movementId + ", productId=" + productId
                + ", movementType=" + movementType + ", quantityChange=" + quantityChange + "}";
    }
}
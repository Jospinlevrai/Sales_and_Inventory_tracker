package dto;

import java.time.LocalDateTime;
import model.Stock_movement;

public class MovementEntry {
    private final long movementId;
    private final LocalDateTime createdAt;
    private final String productName;
    private final Stock_movement.MovementType movementType;
    private final int quantityChange;
    private final String doneBy;
    private final String note;

    public MovementEntry(long movementId,
                         LocalDateTime createdAt,
                         String productName,
                         Stock_movement.MovementType movementType,
                         int quantityChange,
                         String doneBy,
                         String note) {
        this.movementId = movementId;
        this.createdAt = createdAt;
        this.productName = productName;
        this.movementType = movementType;
        this.quantityChange = quantityChange;
        this.doneBy = doneBy;
        this.note = note;
    }

    public long getMovementId() { return movementId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getProductName() { return productName; }
    public Stock_movement.MovementType getMovementType() { return movementType; }
    public int getQuantityChange() { return quantityChange; }
    public String getDoneBy() { return doneBy; }
    public String getNote() { return note; }
}
